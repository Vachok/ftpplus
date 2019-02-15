package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.EMailAndDB.MailMessages;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.systray.ActionDefault;
import ru.vachok.networker.systray.MessageToTray;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.sql.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;


/**
 @since 22.08.2018 (9:36) */
public class SpeedChecker implements Callable<Long>, Runnable {

    /**
     {@link RegRuMysql}
     */
    private static final DataConnectTo DATA_CONNECT_TO = new RegRuMysql();

    /**
     Логер. {@link LoggerFactory}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SpeedChecker.class.getSimpleName());

    /**
     Time as long
     <p>
     Время из Базы
     */
    private Long rtLong = Long.valueOf(ConstantsFor.getProps().getProperty("lastworkstart", "1550207880000"));

    /**
     {@link ThreadConfig#getTaskScheduler()}
     */
    private ThreadPoolTaskScheduler taskScheduler = ThreadConfig.getI().getTaskScheduler();

    /**
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
        Thread.currentThread().setName(classMeth);
        final long stArt = System.currentTimeMillis();
        String sql = ConstantsFor.SELECT_FROM_SPEED;
        Connection c = DATA_CONNECT_TO.getDefaultConnection(ConstantsFor.DB_PREFIX + "liferpg");
        try (PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                if (r.last()) {
                    double timeSpend = r.getDouble(ConstantsFor.TIME_SPEND);
                    long timeStamp = r.getTimestamp(ConstantsFor.COL_SQL_NAME_TIMESTAMP).getTime();
                    String msg = timeSpend + " time spend;\n" + timeStamp;
                    this.rtLong = timeStamp + TimeUnit.MINUTES.toMillis(2);
                    LOGGER.info(msg);
                }
            }
        } catch (SQLException e) {
            new MessageCons().errorAlert("SpeedChecker", "chkForLast", e.getMessage());
            FileSystemWorker.error("SpeedChecker.chkForLast", e);
        }
        methMetr(stArt);
    }

    @Override
    public void run() {
        DayOfWeek now = LocalDate.now().getDayOfWeek();
        if (!(now.equals(SUNDAY) || now.equals(SATURDAY))) {
            taskScheduler.execute(this::setRtLong);
        } else {
            LOGGER.warn(now.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()));
        }
    }

    @Override
    public Long call() {
        Instant instant = new Calendar.Builder().setDate(
            Year.now().getValue(),
            LocalDate.now().getMonth().getValue(), LocalDate.now().getDayOfMonth())
            .setTimeOfDay(8, 15, 0).build().toInstant();

        Duration duration = Duration.ofHours(3);
        Runnable r = this;
        boolean noRAndTimeAfter = !taskScheduler.getScheduledThreadPoolExecutor().getQueue().contains(r) &&
            LocalTime.now().isAfter(LocalTime.of(8, 15)) && LocalTime.now().isBefore(LocalTime.of(11, 15));

        if (noRAndTimeAfter) {
            taskScheduler.scheduleWithFixedDelay(r, instant, duration);
            String msg = true + " starting  taskScheduler";
            LOGGER.info(msg);
        }
        new MessageLocal().info(getClass().getSimpleName(), instant.toString() + " " + duration.toString(), rtLong + " returned");
        ConstantsFor.getProps().setProperty("lastworkstart", rtLong + "");
        return rtLong;
    }

    /**
     Актуализировать БД.
     <p>
     Проверяет почту. Обновляет базу.

     @since 21.01.2019 (12:11)
     */
    public static final class ChkMailAndUpdateDB implements Runnable {

        /**
         ChkMailAndUpdateDB
         */
        private static final String CLASS_NAME = "ChkMailAndUpdateDB";

        /**
         {@link DataConnectTo#getDefaultConnection(java.lang.String)} - {@link ConstantsFor#U_0466446_LIFERPG}
         */
        private static final Connection DEF_CONNECTION = new RegRuMysql().getDefaultConnection(ConstantsFor.U_0466446_LIFERPG);

        /**
         * {@link MailMessages}
         */
        private MailMessages mailMessages = new MailMessages();

        /**
         Получение информации о текущем дне недели.
         <p>
         <b>{@link SQLException}:</b> <br>
         1. {@link MessageLocal#errorAlert(java.lang.String, java.lang.String, java.lang.String)} сообщение об ошибке. <br> 2. {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)}
         запишем в файл. <br><br>
         <b>Далее:</b><br>
         3. {@link MessageLocal#infoNoTitles(java.lang.String)} покажем пользователю.

         @return инфо о средней скорости и времени в текущий день недели.
         */
        static String todayInfo() {
            StringBuilder stringBuilder = new StringBuilder();
            String sql = "select * from speed where WeekDay = ?";
            Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.U_0466446_LIFERPG);
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setInt(1, (LocalDate.now().getDayOfWeek().getValue() + 1));
                try (ResultSet r = p.executeQuery()) {
                    List<Double> speedList = new ArrayList<>();
                    List<Float> timeList = new ArrayList<>();
                    while (r.next()) {
                        speedList.add(r.getDouble("Speed"));
                        timeList.add(r.getFloat("TimeSpend"));
                    }
                    double avSpeed = 0.0;
                    for (Double aDouble : speedList) {
                        avSpeed = avSpeed + aDouble;
                    }
                    avSpeed = avSpeed / speedList.size();
                    double avTime = 0.0;
                    for (Float aFloat : timeList) {
                        avTime = avTime + aFloat;
                    }
                    avTime = avTime / timeList.size();
                    stringBuilder.append("Today is ").append(LocalDate.now().getDayOfWeek()).append("\n");
                    stringBuilder.append("AV speed at this day: ").append(avSpeed).append("\n");
                    stringBuilder.append("AV time: ").append(avTime);
                }
            } catch (SQLException e) {
                new MessageLocal().errorAlert(CLASS_NAME, "todayInfo", e.getMessage());
                FileSystemWorker.error("ChkMailAndUpdateDB.todayInfo", e);
            }
            new MessageLocal().infoNoTitles(stringBuilder.toString());
            return stringBuilder.toString();
        }

        /**
         Сверяет почту и базу.
         <p>
         1. {@link #checkDB()} преобразуем в строку 2. {@link TForms#fromArray(java.util.Map, boolean)}. <br> 3. {@link FileSystemWorker#recFile(java.lang.String, java.util.List)} запишем в файл. <br>
         4. {@link #parseMsg(javax.mail.Message, java.lang.String)} сверка наличия.

         @return строку из {@link #checkDB()} .
         */
        private String chechMail() {
            Message[] messagesBot = mailMessages.call(); // TODO: 13.02.2019 java.sql.Connection.close in ru.vachok.mysqlandprops.props.DBRegProperties.getProps
            String chDB = new TForms().fromArray(checkDB(), false);
            FileSystemWorker.recFile(this.getClass().getSimpleName() + ".chechMail", Collections.singletonList(chDB));
            for (Message m : messagesBot) {
                parseMsg(m, chDB);
            }
            return chDB;
        }

        private void parseMsg(Message m, String chDB) {
            MessageToUser eSender = new ESender(ConstantsFor.GMAIL_COM);
            try {
                String subjMail = m.getSubject();
                if (subjMail.toLowerCase().contains("speed:")) {
                    Date dateSent = m.getSentDate();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(dateSent);
                    LocalDate of = LocalDate.of(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH));
                    int dayOfWeek = of.getDayOfWeek().getValue();
                    long timeSt = calendar.getTimeInMillis();
                    if (writeDB(m.getSubject().toLowerCase().split("speed:")[1], dayOfWeek, timeSt)) {
                        delMessage(m);
                    }
                    String todayInfoStr = todayInfo();
                    eSender.info(ChkMailAndUpdateDB.class.getSimpleName() + " " + ConstantsFor.thisPC(), true + " sending to base", todayInfoStr + "\n" + chDB);
                } else {
                    new MessageToTray(new ActionDefault(ConstantsFor.HTTP_LOCALHOST_8880_SLASH)).infoNoTitles("No new messages");
                }
            } catch (MessagingException e) {
                eSender.errorAlert(
                    this.getClass().getSimpleName(),
                    LocalDateTime.now() + " " + e.getMessage(),
                    new TForms().fromArray(e, false));
            }
        }

        /**
         Проверяет базу данных.
         <p>
         DB name - {@link ConstantsFor#U_0466446_LIFERPG} speed.
         <p>
         <b>{@link SQLException}:</b> <br>
         {@link TForms#fromArray(java.lang.Exception, boolean)} запишем исключение в файл. <br><br>
         <b>Далее:</b><br>
         {@link #todayInfo()} вывод через {@link #LOGGER} <br>

         @return {@link Map}. {@link ConstantsFor#COL_SQL_NAME_TIMESTAMP} - значения.
         */
        private Map<String, String> checkDB() {
            Map<String, String> retMap = new HashMap<>();
            String sql = ConstantsFor.SELECT_FROM_SPEED;
            try (PreparedStatement p = DEF_CONNECTION.prepareStatement(sql);
                 ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    String valueS = r.getInt("Road") +
                        " road, " +
                        r.getString("Speed") +
                        " speed, " + r.getString(ConstantsFor.TIME_SPEND) + " time in min, " +
                        DayOfWeek.of(r.getInt("WeekDay") - 1);
                    retMap.put(r.getTimestamp(ConstantsFor.COL_SQL_NAME_TIMESTAMP).toString(), valueS);
                }
                retMap.put(LocalDateTime.now().toString(), "okok");
            } catch (SQLException e) {
                retMap.put(e.getMessage(), new TForms().fromArray(e, false));
            }
            return retMap;
        }

        private boolean writeDB(String s, int dayOfWeek, long timeSt) {
            double timeSpend;
            double speedFromStr = Double.parseDouble(s.split(" ")[0]);
            int roadFromStr = Integer.parseInt(s.split(" ")[1]);
            if (roadFromStr == 0) timeSpend = (21.6 / speedFromStr) * 60;
            else timeSpend = (31.2 / speedFromStr) * 60;
            Timestamp timestamp = new Timestamp(timeSt);
            String sql = "insert into speed (Speed, Road, WeekDay, TimeSpend, TimeStamp) values (?,?,?,?,?)";
            try (Connection c = new RegRuMysql().getDefaultConnection("u0466446_liferpg");
                 PreparedStatement p = c.prepareStatement(sql)) {
                p.setDouble(1, speedFromStr);
                p.setInt(2, roadFromStr);
                p.setInt(3, dayOfWeek + 1);
                p.setFloat(4, (float) timeSpend);
                p.setTimestamp(5, timestamp);
                p.executeUpdate();
                new MessageToTray().info("DB updated", "Today is " + DayOfWeek.of(dayOfWeek), " Time spend " + timeSpend);
                return true;
            } catch (SQLException e) {
                new MessageCons().errorAlert(CLASS_NAME, "writeDB", e.getMessage());
                FileSystemWorker.error("ChkMailAndUpdateDB.writeDB", e);
                return false;
            }
        }

        private void delMessage(Message m) {
            Folder inboxFolder = mailMessages.getInbox();
            try {
                inboxFolder.getMessage(m.getMessageNumber()).setFlag(Flags.Flag.DELETED, true);
                inboxFolder.close(true);
            } catch (MessagingException e) {
                new MessageCons().errorAlert(CLASS_NAME, "delMessage", e.getMessage());
                FileSystemWorker.error("ChkMailAndUpdateDB.delMessage", e);
            }
        }

        @Override
        public void run() {
            String msg = chechMail();
            LOGGER.info(msg);
        }
    }
}
