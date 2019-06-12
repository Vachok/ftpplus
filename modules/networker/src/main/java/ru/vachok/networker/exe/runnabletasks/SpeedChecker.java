// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.EMailAndDB.MailMessages;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.actions.ActionOnAppStart;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;


/**
 Обновление инфо о скорости и дороге.

 @see ru.vachok.networker.controller.ServiceInfoCtrl#infoMapping(Model, HttpServletRequest, HttpServletResponse)
 @see ActionOnAppStart#actionPerformed(ActionEvent)
 @since 22.08.2018 (9:36) */
public class SpeedChecker implements Callable<Long>, Runnable {
    
    
    /**
     Логер. {@link LoggerFactory}
     */
    private static final MessageToUser LOGGER = new MessageLocal(SpeedChecker.class.getSimpleName());

    /**
     Выходной день
     */
    private static boolean isWeekEnd = (LocalDate.now().getDayOfWeek().equals(SUNDAY) || LocalDate.now().getDayOfWeek().equals(SATURDAY));
    
    private final Properties APP_PR = AppComponents.getProps();
    
    /**
     Time as long
     <p>
     Время из Базы. Берется из {@link AppComponents#getProps()}
     */
    private Long rtLong = Long.valueOf(APP_PR.getProperty(ConstantsFor.PR_LASTWORKSTART, "2"));
    
    /**
     Запуск.
     <p>
     Если прошло 20 часов, с момента {@link #rtLong} или не {@link #isWeekEnd}, запуск {@link #setRtLong()}.
     Иначе {@link #rtLong} = {@link AppComponents#getProps()}
     */
    @Override
    public void run() {
        long l = rtLong + TimeUnit.HOURS.toMillis(20);
        boolean is20HRSSpend = System.currentTimeMillis() > l;
        if (is20HRSSpend || !isWeekEnd) {
            AppComponents.threadConfig().execByThreadConfig(this::setRtLong);
        }
        else {
            this.rtLong = Long.valueOf(APP_PR.getProperty(ConstantsFor.PR_LASTWORKSTART));
        }
    }
    
    /**
     @return {@link #rtLong}
     */
    @Override
    public Long call() {
        return rtLong;
    }
    
    /**
     Метрика метода.
     <p>
     
     @param stArt время начала отсчёта.
     */
    private static void methMetr(long stArt) {
        float f = (float) (System.currentTimeMillis() - stArt) / 1000;
        String msgTimeSp = new StringBuilder()
            .append("SpeedChecker.chkForLast: ")
            .append(f)
            .append(ConstantsFor.STR_SEC_SPEND)
            .toString();
        LOGGER.info(msgTimeSp);
    }
    
    /**
     Время прихода на работу.
     <p>
     Для рассчёта в {@link ru.vachok.networker.controller.ServiceInfoCtrl}.
     <p>
     this.{@link #rtLong} = таймстэм, полученый этим методом из БД.
     <b>{@link SQLException}:</b><br>
     {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)} в файл. <br><br>
     <b>Далее:</b><br>
     {@link #methMetr(long)}. Метрика метода.
     */
    private void setRtLong() {
        String classMeth = "SpeedChecker.chkForLast";
        final String sql = ConstantsFor.DBQUERY_SELECTFROMSPEED;
        Properties properties = APP_PR;
        final long stArt = System.currentTimeMillis();
        new SpeedChecker.ChkMailAndUpdateDB().run();
    
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBPREFIX + "liferpg");
             PreparedStatement p = connection.prepareStatement(sql);
             ResultSet r = p.executeQuery()
        ) {
            while (r.next()) {
                if (r.last()) {
                    double timeSpend = r.getDouble(ConstantsFor.DBFIELD_TIMESPEND);
                    long timeStamp = r.getTimestamp(ConstantsFor.DBFIELD_TIMESTAMP).getTime();
                    String msg = timeSpend + " time spend;\n" + timeStamp;
                    this.rtLong = timeStamp + TimeUnit.SECONDS.toMillis(90);
                    properties.setProperty(ConstantsFor.PR_LASTWORKSTART, rtLong + "");
                    LOGGER.info(msg);
                }
            }
        }
        catch (SQLException | IOException e) {
            FileSystemWorker.error(classMeth, e);
        }
        methMetr(stArt);
    }
    
    /**
     Проверка и обновление БД, при необходимости.
     
     @see SpeedChecker
     @since 21.01.2019 (14:20)
     */
    public class ChkMailAndUpdateDB implements Runnable {
        
        
        /**
         ChkMailAndUpdateDB
         */
        private static final String CLASS_NAME = "ChkMailAndUpdateDB";
        
        /**
         {@link MailMessages}
         */
        private MailMessages mailMessages = new MailMessages();
        
        private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
        
        /**
         {@link String} {@code msg}
         <p>
         msg = {@link #chechMail()} + new {@link Date}({@link #rtLong}).
         */
        @Override
        public void run() {
            String msg = "NO MSG";
            try {
                msg = chechMail();
            }
            catch (IllegalStateException | NullPointerException e) {
                msg = e.getMessage();
            }
            msg = msg + "\n" + new Date(rtLong);
            LOGGER.info(msg);
        }
        
        /**
         Получение информации о текущем дне недели.
         <p>
         <b>{@link SQLException}:</b> <br>
         1. {@link MessageLocal#errorAlert(java.lang.String, java.lang.String, java.lang.String)} сообщение об ошибке. <br> 2.
         {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)}
         запишем в файл. <br><br>
         <b>Далее:</b><br>
         3. {@link MessageLocal#infoNoTitles(java.lang.String)} покажем пользователю.
         
         @return инфо о средней скорости и времени в текущий день недели.
         */
        String todayInfo() {
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
                LOGGER.errorAlert(CLASS_NAME, "todayInfo", e.getMessage());
                FileSystemWorker.error("ChkMailAndUpdateDB.todayInfo", e);
            }
            LOGGER.infoNoTitles(stringBuilder.toString());
            return stringBuilder.toString();
        }
        
        /**
         Сверяет почту и базу.
         <p>
         1. {@link #checkDB()} преобразуем в строку. 2. {@link TForms#fromArray(java.util.Map, boolean)}. <br>
         3. {@link FileSystemWorker#writeFile(java.lang.String, java.util.List)} запишем в файл. <br>
         4. {@link #parseMsg(javax.mail.Message, java.lang.String)} сверка наличия.
         
         @return строку из {@link #checkDB()} .
         
         @see #run()
         */
        private String chechMail() {
            Message[] messagesBot = mailMessages.call();
            String chDB = new TForms().fromArray(checkDB(), false);
            FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".chechMail", Collections.singletonList(chDB));
            for (Message m : messagesBot) {
                parseMsg(m, chDB);
            }
            return chDB;
        }
        
        /**
         Проверяет базу данных.
         <p>
         DB name - {@link ConstantsFor#DBBASENAME_U0466446_LIFERPG} speed.
         <p>
         <b>{@link SQLException}:</b> <br>
         {@link TForms#fromArray(java.lang.Exception, boolean)} запишем исключение в файл. <br><br>
         <b>Далее:</b><br>
         {@link #todayInfo()} вывод через {@link #LOGGER} <br>
         
         @return {@link Map}. {@link ConstantsFor#DBFIELD_TIMESTAMP} - значения.
         
         @see #chechMail()
         */
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
        
        /**
         Парсинг сообщений от бота.
         <p>
         {@link ESender} ({@link ConstantsFor#MAILADDR_143500GMAILCOM}). <br>
         Если тема сообщения содержит {@code speed:}, берётся дата отправки {@link Message#getSentDate()}.
         <p>
         Если {@link #writeDB(String, int, long)}, удалим сообщение {@link #delMessage(Message)}.
         <p>
         Отправить почту - {@link #todayInfo()}.
         
         @param m {@link Message}
         @param chDB инфо из БД
         @see #chechMail()
         */
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
                    messageToUser.info(SpeedChecker.ChkMailAndUpdateDB.class.getSimpleName() + " " + ConstantsFor.thisPC(), true + " sending to base",
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
}
