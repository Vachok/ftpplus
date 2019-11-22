// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.EMailAndDB.MailMessages;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.File;
import java.sql.*;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.*;
import java.util.concurrent.*;


/**
 @see ru.vachok.networker.exe.runnabletasks.ChkMailAndUpdateDBTest
 @since 21.01.2019 (14:20) */
class ChkMailAndUpdateDB implements Callable<Long> {
    
    
    private static final String SPEED = "speed:";
    
    private static final String MSG = ".parseMsg";
    
    private static final String IS_ = "Today is ";
    
    private SpeedChecker checker;
    
    private MailMessages mailMessages = new MailMessages();
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.NULL, ChkMailAndUpdateDB.class.getSimpleName());
    
    private long timeStamp = 1;
    
    ChkMailAndUpdateDB(SpeedChecker checker) {
        this.checker = checker;
    }
    
    @Override
    public Long call() {
        runCheck();
        return timeStamp;
    }
    
    private void runCheck() {
        String msg;
        try {
            msg = chechMail();
        }
        catch (IllegalStateException | NullPointerException e) {
            msg = e.getMessage();
        }
        msg = msg + "\n" + new Date(checker.getRtLong());
        File chkMailFile = new File("ChkMailAndUpdateDB.chechMail");
        if (chkMailFile.exists()) {
            messageToUser.info(msg + " see: " + chkMailFile.getAbsolutePath());
        }
    }
    
    private @NotNull String chechMail() {
        Future<Message[]> submit = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(mailMessages);
        Message[] messagesBot = new Message[(int) ConstantsFor.DELAY];
        try {
            messagesBot = submit.get(ConstantsFor.TIMEOUT_650 / 3, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            messageToUser.error(MessageFormat.format("ChkMailAndUpdateDB.chechMail {0} - {1}", e.getClass().getTypeName(), e.getMessage()));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        String chDB = new TForms().fromArray(checkDB(), false);
        boolean isWriteFile = FileSystemWorker.writeFile(FileNames.SPEED_CHECHMAIL, Collections.singletonList(chDB));
        for (Message m : messagesBot) {
            parseMsg(m, chDB);
        }
        return chDB + " file written - " + isWriteFile;
    }
    
    private @NotNull Map<String, String> checkDB() {
        Map<String, String> retMap = new TreeMap<>();
        final String sql = ConstantsFor.DBQUERY_SELECTFROMSPEED;
        try (Connection defConnection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_LIFERPG);
             PreparedStatement p = defConnection.prepareStatement(sql);
             ResultSet r = p.executeQuery()
        ) {
            while (r.next()) {
                String valueS = r.getInt("Road") +
                    " road, " +
                    r.getString(ConstantsFor.DBFIELD_SPEED) +
                    " speed, " + r.getString(ConstantsFor.DBFIELD_TIMESPEND) + " time in min, " +
                    DayOfWeek.of(r.getInt("WeekDay") - 1);
                retMap.put(r.getTimestamp(ConstantsFor.DBFIELD_TIMESTAMP).toString(), valueS);
                
            }
            retMap.put(LocalDateTime.now().toString(), "okok");
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
        }
        return retMap;
    }
    
    private void parseMsg(@NotNull Message mailMessage, String chDB) {
        try {
            String subjMail = mailMessage.getSubject().toLowerCase();
            if (subjMail.toLowerCase().contains(SPEED) || subjMail.toLowerCase().contains(SPEED + " ")) {
                Date dateSent = mailMessage.getSentDate();
                Calendar calendar = Calendar.getInstance();
                LocalDate of = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                calendar.setTime(dateSent);
                
                int dayOfWeek = of.getDayOfWeek().getValue();
                long timeSt = calendar.getTimeInMillis();
                
                String subject = checkSubject(mailMessage);
                
                if (writeDB(subject, dayOfWeek, timeSt)) {
                    delMessage(mailMessage);
                }
            }
            else {
                messageToUser.info(getClass().getSimpleName() + MSG, "mailMessages", " = " + mailMessages.getInbox().getMessageCount());
            }
        }
        catch (MessagingException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + MSG, e));
        }
        finally {
            messageToUser.info(todayInfo());
        }
    }
    
    private String checkSubject(@NotNull Message m) throws MessagingException {
        String retStr;
        try {
            retStr = m.getSubject().toLowerCase();
            return retStr.split(SPEED)[1];
        }
        catch (IndexOutOfBoundsException e) {
            return delMessage(m);
        }
    }
    
    private boolean writeDB(@NotNull String speedAndRoad, int dayOfWeek, long timeSt) {
        double timeSpend;
        int roadFromStr = 0;
        double speedFromStr = 0;
        try {
            speedFromStr = Double.parseDouble(speedAndRoad.split(" ")[0]);
            roadFromStr = Integer.parseInt(speedAndRoad.split(" ")[1]);
        }
        catch (ArrayIndexOutOfBoundsException ignore) {
            //22.08.2019 (22:04)
        }
        if (roadFromStr == 0) {
            timeSpend = (ConstantsFor.KM_A107 / speedFromStr) * ConstantsFor.ONE_HOUR_IN_MIN;
        }
        else {
            timeSpend = (ConstantsFor.KM_M9 / speedFromStr) * ConstantsFor.ONE_HOUR_IN_MIN;
        }
        Timestamp timestamp = new Timestamp(timeSt);
        final String sql = "insert into speed (Speed, Road, WeekDay, TimeSpend, TimeStamp) values (?,?,?,?,?)";
        try (Connection c = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_LIFERPG);
             PreparedStatement p = c.prepareStatement(sql)
        ) {
            p.setDouble(1, speedFromStr);
            p.setInt(2, roadFromStr);
            p.setInt(3, dayOfWeek + 1);
            p.setFloat(4, (float) timeSpend);
            p.setTimestamp(5, timestamp);
            
            int rowsUpdate = p.executeUpdate();
            MessageToUser.getInstance(MessageToUser.EMAIL, this.getClass().getSimpleName())
                .info("DB updated: " + rowsUpdate + "\n", IS_ + DayOfWeek.of(dayOfWeek), " Time spend " + timeSpend);
            this.timeStamp = timeSt;
            return rowsUpdate > 0;
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("ChkMailAndUpdateDB.writeDB: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return false;
        }
    }
    
    private @NotNull String delMessage(@NotNull Message m) {
        Folder inboxFolder = mailMessages.getInbox();
        try {
            inboxFolder.getMessage(m.getMessageNumber()).setFlag(Flags.Flag.DELETED, true);
            inboxFolder.close(true);
        }
        catch (MessagingException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".delMessage");
        }
        return "Speed:0 0";
    }
    
    private @NotNull String todayInfo() {
        final String sql = "select * from speed where WeekDay = ?";
        StringBuilder stringBuilder = new StringBuilder();
        try (Connection c = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_LIFERPG);
             PreparedStatement p = c.prepareStatement(sql)
        ) {
            p.setInt(1, (LocalDate.now().getDayOfWeek().getValue() + 1));
            try (ResultSet r = p.executeQuery()) {
                stringBuilder.append(parseResultSet(r)).append("\n");
            }
        }
        catch (SQLException e) {
            messageToUser.warn(ChkMailAndUpdateDB.class.getSimpleName(), e.getMessage(), " see line: 221 ***");
        }
        return stringBuilder.toString();
    }
    
    private @NotNull String parseResultSet(@NotNull ResultSet r) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        List<Double> speedList = new ArrayList<>();
        List<Float> timeList = new ArrayList<>();
        while (r.next()) {
            speedList.add(r.getDouble(ConstantsFor.DBFIELD_SPEED));
            timeList.add(r.getFloat(ConstantsFor.DBFIELD_TIMESPEND));
        }
        double avSpeed = 0.0;
        for (Double aDouble : speedList) {
            avSpeed += aDouble;
        }
        avSpeed /= speedList.size();
        double avTime = 0.0;
        for (Float aFloat : timeList) {
            avTime += aFloat;
        }
        avTime /= timeList.size();
        stringBuilder.append(IS_).append(LocalDate.now().getDayOfWeek()).append("\n");
        stringBuilder.append("AV speed at this day: ").append(avSpeed).append("\n");
        stringBuilder.append("AV time: ").append(avTime);
        return stringBuilder.toString();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChkMailAndUpdateDB{");
        sb.append("checker=").append(checker.getClass().getTypeName());
        sb.append(", mailMessages=").append(mailMessages.getClass().getTypeName());
        sb.append(", messageToUser=").append(messageToUser.getClass().getTypeName());
        sb.append('}');
        return sb.toString();
    }
}
