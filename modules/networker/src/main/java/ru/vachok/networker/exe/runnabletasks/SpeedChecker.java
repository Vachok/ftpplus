// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import javax.mail.*;
import java.net.MalformedURLException;
import java.sql.*;
import java.time.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;


/**
 @see ru.vachok.networker.exe.runnabletasks.SpeedCheckerTest */
public class SpeedChecker implements Callable<Long> {
    
    private static final Properties APP_PR = InitProperties.getTheProps();
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, SpeedChecker.class.getTypeName());
    
    private static boolean isWeekEnd = (LocalDate.now().getDayOfWeek().equals(SUNDAY) || LocalDate.now().getDayOfWeek().equals(SATURDAY));
    
    private Long rtLong = Long.valueOf(APP_PR.getProperty(PropertiesNames.LASTWORKSTART, "2"));
    
    Long getRtLong() {
        return rtLong;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", SpeedChecker.class.getSimpleName() + "[\n", "\n]")
                .add("APP_PR = " + APP_PR)
                .add("rtLong = " + rtLong)
                .toString();
    }
    
    @Override
    public Long call() {
        runMe();
        return rtLong;
    }
    
    private void runMe() {
        long l = rtLong + TimeUnit.HOURS.toMillis(20);
        boolean is20HRSSpend = System.currentTimeMillis() > l;
        if (is20HRSSpend || !isWeekEnd) {
            setRtLong();
            APP_PR.setProperty(PropertiesNames.LASTWORKSTART, String.valueOf(rtLong));
            InitProperties.getInstance(InitProperties.DB_MEMTABLE).setProps(APP_PR);
        }
        else {
            this.rtLong = Long.valueOf(APP_PR.getProperty(PropertiesNames.LASTWORKSTART));
        }
    }
    
    private void setRtLong() {
        this.rtLong = (long) -666;
        try {
            getFromMail();
            if (this.rtLong == -666) {
                getFromDB();
            }
        }
        catch (MessagingException | MalformedURLException e) {
            messageToUser.error("SpeedChecker.getFromMail", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
    }
    
    private void getFromMail() throws MalformedURLException, MessagingException {
        Properties mailPr = InitProperties.getMAilPr();
        Session session = Session.getInstance(mailPr);
        Transport sessionTransport = session.getTransport();
        sessionTransport.connect(ConstantsFor.MAIL_SERVERREGRU, mailPr.getProperty("user"), mailPr.getProperty(PropertiesNames.PASSWORD));
        Store imapsStore = session.getStore("imaps");
        imapsStore.connect(ConstantsFor.MAIL_SERVERREGRU, mailPr.getProperty("user"), mailPr.getProperty(PropertiesNames.PASSWORD));
        if (imapsStore.isConnected()) {
            Folder inboxFolder = getInboxFolder(imapsStore);
            for (Message folderMessage : inboxFolder.getMessages()) {
                String subject = folderMessage.getSubject();
                if (subject.toLowerCase().contains("speed:")) {
                    this.rtLong = folderMessage.getSentDate().getTime() + TimeUnit.MINUTES.toMillis(2);
                    if (writeToDB(folderMessage.getSubject())) {
                        folderMessage.setFlag(Flags.Flag.DELETED, true);
                    }
                }
                else if (folderMessage.getSubject().contains("Mail delivery failed:")) {
                    folderMessage.setFlag(Flags.Flag.DELETED, true);
                }
            }
            inboxFolder.close(true);
            imapsStore.close();
            sessionTransport.close();
        }
        else {
            throw new InvokeIllegalException(imapsStore.toString());
        }
    }
    
    private void getFromDB() {
        final String sql = "select * from liferpg.speed order by idspeed desc limit 1";
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_LIFERPGSPEED);
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                this.rtLong = resultSet.getTimestamp(ConstantsFor.DBFIELD_TIMESTAMP).getTime();
            }
        }
        catch (SQLException e) {
            messageToUser.error("SpeedChecker.getFromDB", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            this.rtLong = (long) -666;
        }
    }
    
    private @NotNull Folder getInboxFolder(@NotNull Store imapsStore) throws MessagingException, MalformedURLException {
        Folder defaultFolder = imapsStore.getDefaultFolder();
        for (Folder folder : defaultFolder.list()) {
            if (folder.getName().equalsIgnoreCase("INBOX")) {
                defaultFolder = folder;
            }
        }
        defaultFolder.open(Folder.READ_WRITE);
        return defaultFolder;
    }
    
    private boolean writeToDB(String subject) {
        subject = subject.split(":")[1];
        double speed = Double.parseDouble(subject.split(" ")[0]);
        int road = Integer.parseInt(subject.split(" ")[1]);
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_LIFERPGSPEED);
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO speed ('Speed', 'Road', 'WeekDay', 'TimeStamp') values (?, ?, ?, ?)")) {
            preparedStatement.setDouble(1, speed);
            preparedStatement.setInt(2, road);
            LocalDateTime time = LocalDateTime.ofEpochSecond(rtLong / 1000, 0, ZoneOffset.ofHours(3));
            preparedStatement.setInt(3, time.getDayOfWeek().getValue());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(time));
            return preparedStatement.executeUpdate() > 0;
        }
        catch (SQLException e) {
            messageToUser.error("SpeedChecker.writeToDB", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            return false;
        }
    }
    
    private void connectToDB(Connection connection) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement(ConstantsFor.DBQUERY_SELECTFROMSPEED)) {
            p.setQueryTimeout((int) ConstantsFor.DELAY);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    if (r.last()) {
                        double timeSpend = r.getDouble(ConstantsFor.DBFIELD_TIMESPEND);
                        long timeStamp = r.getTimestamp(ConstantsFor.DBFIELD_TIMESTAMP).getTime();
                        String msg = timeSpend + " time spend;\n" + new Date(timeStamp);
                        this.rtLong = timeStamp + TimeUnit.SECONDS.toMillis((long) (ConstantsFor.ONE_HOUR_IN_MIN * 2));
                        APP_PR.setProperty(PropertiesNames.LASTWORKSTART, String.valueOf(rtLong));
                        messageToUser.info(this.getClass().getSimpleName(), "connectToDB", msg);
                    }
                }
            }
        }
    }
}
