// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.*;
import java.util.concurrent.*;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;


/**
 @see ru.vachok.networker.exe.runnabletasks.SpeedCheckerTest */
public class SpeedChecker implements Callable<Long> {
    
    
    private static final Properties APP_PR = AppComponents.getProps();
    
    private static boolean isWeekEnd = (LocalDate.now().getDayOfWeek().equals(SUNDAY) || LocalDate.now().getDayOfWeek().equals(SATURDAY));
    
    private static MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, SpeedChecker.class.getTypeName());
    
    
    private Long rtLong = Long.valueOf(APP_PR.getProperty(PropertiesNames.PR_LASTWORKSTART, "2"));
    
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
    
    public void runMe() {
        long l = rtLong + TimeUnit.HOURS.toMillis(20);
        boolean is20HRSSpend = System.currentTimeMillis() > l;
        if (is20HRSSpend || !isWeekEnd) {
            setRtLong();
        }
        else {
            this.rtLong = Long.valueOf(APP_PR.getProperty(PropertiesNames.PR_LASTWORKSTART));
        }
    }
    
    private void setRtLong() {
        Thread.currentThread().setName("SpeedChecker.setRtLong");
        
        Future<Long> submit = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(new ChkMailAndUpdateDB(this));
        try {
            this.rtLong = submit.get(ConstantsFor.DELAY * 2, TimeUnit.SECONDS);
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            messageToUser
                .error(MessageFormat.format("SpeedChecker.setRtLong {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ArrayIndexOutOfBoundsException ignore) {
            //
        }
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBPREFIX + "liferpg")) {
            connectToDB(connection);
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
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
                        APP_PR.setProperty(PropertiesNames.PR_LASTWORKSTART, String.valueOf(rtLong));
                        messageToUser.info(msg);
                    }
                }
            }
        }
    }
    
    Long getRtLong() {
        return rtLong;
    }
}
