// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.EMailAndDB.MailMessages;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 Проверка и обновление БД, при необходимости.
 
 @see SpeedChecker
 @since 21.01.2019 (14:20) */
class ChkMailAndUpdateDB {
    
    
    /**
     ChkMailAndUpdateDB
     */
    private static final String CLASS_NAME = "ChkMailAndUpdateDB";
    
    private SpeedChecker checker;
    
    /**
     {@link MailMessages}
     */
    private MailMessages mailMessages = new MailMessages();
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    ChkMailAndUpdateDB(SpeedChecker checker) {
        this.checker = checker;
    }
    
    void runCheck() {
        String msg = "NO MSG";
        try {
            msg = chechMail();
        }
        catch (IllegalStateException | NullPointerException e) {
            msg = e.getMessage();
        }
        msg = msg + "\n" + new Date(checker.getRtLong());
        File chkMailFile = new File("ChkMailAndUpdateDB.chechMail");
        if (chkMailFile.exists()) {
            msg = msg + " see: " + chkMailFile.getAbsolutePath();
        }
        System.out.println(msg);
    }
    
    /**
     Получение информации о текущем дне недели.
     <p>
     <b>{@link SQLException}:</b> <br>
     1. {@link MessageLocal#errorAlert(String, String, String)} сообщение об ошибке. <br> 2.
     {@link FileSystemWorker#error(String, Exception)}
     запишем в файл. <br><br>
     <b>Далее:</b><br>
     3. {@link MessageLocal#infoNoTitles(String)} покажем пользователю.
     
     @return инфо о средней скорости и времени в текущий день недели.
     */
    private String todayInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        final String sql = "select * from speed where WeekDay = ?";
        
        try (Connection c = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_LIFERPG);
             PreparedStatement p = c.prepareStatement(sql)
        ) {
            p.setInt(1, (LocalDate.now().getDayOfWeek().getValue() + 1));
            try (ResultSet r = p.executeQuery()) {
                List<Double> speedList = new ArrayList<>();
                List<Float> timeList = new ArrayList<>();
                while (r.next()) {
                    speedList.add(r.getDouble("Speed"));
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
                stringBuilder.append("Today is ").append(LocalDate.now().getDayOfWeek()).append("\n");
                stringBuilder.append("AV speed at this day: ").append(avSpeed).append("\n");
                stringBuilder.append("AV time: ").append(avTime);
            }
        }
        catch (SQLException | IOException e) {
            messageToUser.error(e.getMessage());
        }
        messageToUser.infoNoTitles(stringBuilder.toString());
        return stringBuilder.toString();
    }
    
    private String chechMail() {
        Message[] messagesBot = mailMessages.call();
        String chDB = new TForms().fromArray(checkDB(), false);
        boolean isWriteFile = FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".chechMail", Collections.singletonList(chDB));
        for (Message m : messagesBot) {
            parseMsg(m, chDB);
        }
        return chDB + " file written - " + isWriteFile;
    }
    
    private Map<String, String> checkDB() {
        Map<String, String> retMap = new HashMap<>();
        final String sql = ConstantsFor.DBQUERY_SELECTFROMSPEED;
        try (Connection defConnection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_LIFERPG);
             PreparedStatement p = defConnection.prepareStatement(sql);
             ResultSet r = p.executeQuery()
        ) {
            while (r.next()) {
                String valueS = r.getInt("Road") +
                    " road, " +
                    r.getString("Speed") +
                    " speed, " + r.getString(ConstantsFor.DBFIELD_TIMESPEND) + " time in min, " +
                    DayOfWeek.of(r.getInt("WeekDay") - 1);
                retMap.put(r.getTimestamp(ConstantsFor.DBFIELD_TIMESTAMP).toString(), valueS);
            }
            retMap.put(LocalDateTime.now().toString(), "okok");
        }
        catch (SQLException | IOException e) {
            retMap.put(e.getMessage(), new TForms().fromArray(e, false));
        }
        return retMap;
    }
    
    private void parseMsg(Message m, String chDB) {
        try {
            String subjMail = m.getSubject();
            if (subjMail.toLowerCase().contains("speed:")) {
                Date dateSent = m.getSentDate();
                Calendar calendar = Calendar.getInstance();
                LocalDate of = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                calendar.setTime(dateSent);
                
                int dayOfWeek = of.getDayOfWeek().getValue();
                long timeSt = calendar.getTimeInMillis();
                AppComponents.threadConfig().execByThreadConfig(()->new TemporaryFullInternet(timeSt + TimeUnit.HOURS.toMillis(9)));
                
                if (writeDB(m.getSubject().toLowerCase().split("speed:")[1], dayOfWeek, timeSt)) {
                    delMessage(m);
                }
                String todayInfoStr = todayInfo();
                messageToUser.info(ChkMailAndUpdateDB.class.getSimpleName() + " " + ConstantsFor.thisPC(), true + " sending to base",
                    todayInfoStr + "\n" + chDB);
            }
            else {
                messageToUser.info(getClass().getSimpleName() + ".parseMsg", "mailMessages", " = " + mailMessages.getInbox().getMessageCount());
            }
        }
        catch (MessagingException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".parseMsg", e));
        }
    }
    
    /**
     Запись в БД.
     
     @param speedAndRoad скорость и дорога из письма.
     @param dayOfWeek день недели
     @param timeSt дата отправки письма
     @return добавлена запись в БД
     
     @see #parseMsg(Message, String)
     */
    private boolean writeDB(String speedAndRoad, int dayOfWeek, long timeSt) {
        double timeSpend;
        double speedFromStr = Double.parseDouble(speedAndRoad.split(" ")[0]);
        int roadFromStr = Integer.parseInt(speedAndRoad.split(" ")[1]);
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
            p.executeUpdate();
            messageToUser.info("DB updated", "Today is " + DayOfWeek.of(dayOfWeek), " Time spend " + timeSpend);
            return true;
        }
        catch (SQLException | IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".writeDB", e));
            return false;
        }
    }
    
    /**
     Удаление сообщения.
     
     @param m {@link Message}
     @see #parseMsg(Message, String)
     */
    private void delMessage(Message m) {
        Folder inboxFolder = mailMessages.getInbox();
        try {
            inboxFolder.getMessage(m.getMessageNumber()).setFlag(Flags.Flag.DELETED, true);
            inboxFolder.close(true);
        }
        catch (MessagingException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".delMessage", e));
        }
    }
}
