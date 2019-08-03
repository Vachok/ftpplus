// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.sysinfo.ServiceInfoCtrl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.*;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;


public class SpeedChecker implements Callable<Long>, Runnable {
    
    private final Properties APP_PR = AppComponents.getProps();
    
    /**
     Выходной день
     */
    private static boolean isWeekEnd = (LocalDate.now().getDayOfWeek().equals(SUNDAY) || LocalDate.now().getDayOfWeek().equals(SATURDAY));
    
    private static MessageToUser messageToUser = new MessageLocal(SpeedChecker.class.getSimpleName());
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", SpeedChecker.class.getSimpleName() + "[\n", "\n]")
            .add("APP_PR = " + APP_PR)
            .add("rtLong = " + rtLong)
            .toString();
    }
    
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
            setRtLong();
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
        run();
        return rtLong;
    }
    
    Long getRtLong() {
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
        messageToUser.info(msgTimeSp);
    }
    
    /**
     Время прихода на работу.
     <p>
     Для рассчёта в {@link ServiceInfoCtrl}.
     <p>
     this.{@link #rtLong} = таймстэм, полученый этим методом из БД.
     <b>{@link SQLException}:</b><br>
     {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)} в файл. <br><br>
     <b>Далее:</b><br>
     {@link #methMetr(long)}. Метрика метода.
     */
    private void setRtLong() {
        String classMeth = "SpeedChecker.chkForLast";
        final long stArt = System.currentTimeMillis();
    
        Runnable chkMail = new ChkMailAndUpdateDB(this);
        Future<?> submit = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(chkMail);
        try {
            submit.get(ConstantsFor.DELAY * 2, TimeUnit.SECONDS);
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            messageToUser
                .error(MessageFormat.format("SpeedChecker.setRtLong {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBPREFIX + "liferpg")) {
            connectToDB(connection);
        }
        catch (SQLException e) {
            FileSystemWorker.error(classMeth, e);
        }
        methMetr(stArt);
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
                        APP_PR.setProperty(ConstantsFor.PR_LASTWORKSTART, String.valueOf(rtLong));
                        messageToUser.info(msg);
                    }
                }
            }
        }
    }
}
