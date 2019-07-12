// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.monitors.AbstractMonitorFactory;
import ru.vachok.networker.abstr.monitors.NetMonitorFactory;
import ru.vachok.networker.abstr.monitors.Pinger;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.exe.runnabletasks.TemporaryFullInternet;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertNull;


/**
 @see Do0213Monitor
 @since 07.07.2019 (9:08) */
public class Do0213MonitorTest implements Pinger {
    
    
    private static final String MONITORED_HOST = "10.200.214.80";
    
    private DataConnectTo dataConnectTo = new RegRuMysql();
    
    private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    
    private long timeInCounting;
    
    private long timeoutStamp;
    
    private String dateFromDB = "07-01-1984";
    
    private long timeIn;
    
    private long timeinStamp;
    
    private final Do0213Monitor do213MonFin = new Do0213Monitor();
    
    private Do0213Monitor do213MonitorNotFin = do213MonFin;
    
    public long getTimeInCounting() {
        return timeInCounting;
    }
    
    @Test
    public void testRun() {
        NetMonitorFactory nmFactory = AbstractMonitorFactory.createNetMonitorFactory("10.200.213.85");
        nmFactory.setLaunchTimeOut(30);
        boolean isGalaxy7Reach = nmFactory.isReach(MONITORED_HOST);
        Assert.assertTrue(isGalaxy7Reach, nmFactory.toString());
    }
    
    @Test
    public void trueRun() {
        Assert.assertTrue(getTimeToEndStr().contains("left official"));
        try {
            getI(MONITORED_HOST).run();
        }
        catch (InvokeIllegalException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void toStrTest() {
        String toString = getI(MONITORED_HOST).toString();
        Assert.assertTrue(toString.contains("Do0213Monitor["));
    }
    
    @Test
    public void toStringTest() {
        System.out.println("getI = " + do213MonitorNotFin);
    }
    
    @Test
    public void testRunDownloadLastPingFromDBDirectly() {
        String launchSchedule = do213MonitorNotFin.getStatistics();
        Assert.assertTrue(launchSchedule.contains("TASK SCHEDULER"));
    }
    
    @Test
    public void testEightPMCondition() {
        downloadLastDatabaseSavedConditions();
    
        try {
            Date eightPM = dateFormat.parse(dateFromDB);
            long eightPMLong = eightPM.getTime() + (LocalTime.parse("20:00").toSecondOfDay() * 1000);
            Date date8PM = new Date(eightPMLong);
            this.dateFromDB = dateFormat.format(date8PM);
            this.timeoutStamp = date8PM.getTime() - TimeUnit.HOURS.toMillis(2);
        }
        catch (ParseException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test(enabled = false)
    public void openInet() { //fixme 09.07.2019 (11:19)
        TemporaryFullInternet temporaryFullInternet = new TemporaryFullInternet("10.200.213.85", 9, "add");
        String resultOfOpen = temporaryFullInternet.call();
        Assert.assertTrue(resultOfOpen.contains("10.200.213.85"), resultOfOpen);
    }
    
    @Test(enabled = false)
    public void waitNotifyTest() {
        
        try {
            synchronized(do213MonFin) {
                System.out.println("waiting...\n" + do213MonFin);
                do213MonFin.wait(do213MonFin.getTimeoutForPingSeconds() + 10);
                syncIsPcOnline();
            }
        }
        catch (InterruptedException e) {
            System.err.println(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public boolean isReach(String inetAddrStr) {
        boolean isPCOnline = false;
        try {
            InetAddress inetAddress = InetAddress.getByName(inetAddrStr);
            Assert.assertTrue(pingDevice(inetAddress));
            isPCOnline = inetAddress.isReachable(ConstantsFor.TIMEOUT_650);
        }
        catch (IOException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        return isPCOnline;
    }
    
    @Override
    public String writeLogToFile() {
        throw new InvokeEmptyMethodException("12.07.2019 (16:42)");
    }
    
    @Override
    public String getPingResultStr() {
        String fileResultsName = getClass().getSimpleName() + ".res";
        try (OutputStream outputStream = new FileOutputStream(fileResultsName, true);
             PrintStream printStream = new PrintStream(outputStream, true, "UTF-8")
        ) {
            printStream.println(getTimeToEndStr() + " " + LocalTime.now());
        }
        catch (IOException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        return FileSystemWorker.readFile(fileResultsName);
    }
    
    @Override
    public String getTimeToEndStr() {
        return TimeUnit.SECONDS.toMinutes(LocalTime.parse("17:30").toSecondOfDay() - LocalTime.now().toSecondOfDay()) + Do0213Monitor.MIN_LEFT_OFFICIAL;
    }
    
    private Runnable getI(String host) {
        Do0213Monitor do0213Monitor = new Do0213Monitor();
        do0213Monitor.setHostName(host);
        return do0213Monitor;
    }
    
    private void syncIsPcOnline() {
        synchronized(do213MonFin) {
            while (!do213MonFin.isReach("10.200.214.80")) {
                do213MonFin.notifyAll();
            }
        }
    }
    
    private void whatHappensUnderTheseConditions(boolean isTimestampBiggerThatZero, boolean isDateEqualsToday, boolean isDO0213Online) {
        boolean basicCondition = isTimestampBiggerThatZero && (!isDateEqualsToday && isDO0213Online);
        
        if (basicCondition) {
            System.out.println("Set timeIn");
            System.out.println(TemporaryFullInternet.class.getSimpleName() + " on 9 hours");
            System.out.println("timeInUploadToDB");
            System.out.println("runMonitoringPC");
        }
        else {
            System.out.println("runMonitoringPC");
        }
    }
    
    private void testPossibleDBVariants(int variantNum) {
        boolean isTimestampBiggerThatZero = false;
        boolean isDateEqualsToday = false;
        boolean isDO0213Online = false;
        if (variantNum == 1) {
            checkCurrentDBStateCondition();
            
            downloadLastDatabaseSavedConditions();
            isTimestampBiggerThatZero = timeoutStamp > 0;
            isDateEqualsToday = dateCheck(this.dateFromDB);
            isDO0213Online = isReach("localhost");
            
        }
        
        whatHappensUnderTheseConditions(isTimestampBiggerThatZero, isDateEqualsToday, isDO0213Online);
    }
    
    private void checkCurrentDBStateCondition() {
        throw new InvokeEmptyMethodException(getClass().getTypeName());
    }
    
    private boolean dateCheck(@NotNull String dateFromDB) {
        return dateFromDB.equals(dateFormat.format(new Date()));
    }
    
    private void parseRS(@NotNull ResultSet rsFromDB) throws SQLException {
        this.dateFromDB = rsFromDB.getString("Date");
        this.timeinStamp = rsFromDB.getLong(ConstantsFor.DBFIELD_TIMEIN);
        this.timeoutStamp = rsFromDB.getLong(ConstantsFor.DBFIELD_TIMEOUT);
    }
    
    private void uploadLastPingToDB() {
        final String sql = "INSERT INTO `u0466446_testing`.`worktime` (`Date`, `Timein`, `Timeout`) VALUES (?, ?, ?);";
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_LIFERPG);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatement.setString(1, new Date().toString());
            preparedStatement.setLong(2, ConstantsFor.getAtomicTime());
            preparedStatement.setLong(3, 0);
            int rowsUpdate = preparedStatement.executeUpdate();
            Assert.assertTrue(rowsUpdate > 0);
        }
        catch (SQLException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    private void monitorDO213() {
        while (true) {
            boolean is213Reach = isReach(ConstantsFor.HOSTNAME_DO213);
            if (!is213Reach) {
                break;
            }
            this.timeInCounting = System.currentTimeMillis() - timeinStamp;
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
            }
        }
    }
    
    private void downloadLastDatabaseSavedConditions() {
        
        final String sql = "SELECT * FROM `worktime` ORDER BY `worktime`.`recid` DESC LIMIT 1 ";
        
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            while (resultSet.next()) {
                if (resultSet.last()) {
                    parseRS(resultSet);
                }
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".downloadLastDatabaseSavedConditions");
        }
    }
    
    
}