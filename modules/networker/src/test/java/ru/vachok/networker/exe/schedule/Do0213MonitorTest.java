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
import ru.vachok.networker.abstr.monitors.PingerService;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.exe.runnabletasks.TemporaryFullInternet;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
 @see Do0213
 @since 07.07.2019 (9:08) */
public class Do0213MonitorTest {
    
    
    private static final String MONITORED_HOST = "10.200.214.80";
    
    private DataConnectTo dataConnectTo = new RegRuMysql();
    
    private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    
    private long timeInCounting;
    
    private long timeoutStamp;
    
    private String dateFromDB = "07-01-1984";
    
    private long timeIn;
    
    private long timeinStamp;
    
    private final PingerService do213MonFin = new Do0213();
    
    private PingerService do213MonitorNotFin = do213MonFin;
    
    public long getTimeInCounting() {
        return timeInCounting;
    }
    
    @Test
    public void testRun() {
        throw new InvokeEmptyMethodException("18.07.2019 (16:43)");
    }
    
    @Test
    public void trueRun() {
        Assert.assertTrue(getExecution().contains("left official"));
        try {
            getI().run();
        }
        catch (InvokeIllegalException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void toStrTest() {
        String toString = getI().toString();
        Assert.assertTrue(toString.contains("Do0213["));
    }
    
    @Test
    public void toStringTest() {
        System.out.println("getI = " + do213MonitorNotFin);
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
        throw new InvokeEmptyMethodException("18.07.2019 (16:49)");
    }
    
    
    public Runnable getMonitoringRunnable() {
        throw new InvokeEmptyMethodException("18.07.2019 (16:54)");
    }
    
    public String getStatistics() {
        return toString();
    }
    
    public boolean isReach(String inetAddrStr) {
        boolean isPCOnline = false;
        try {
            InetAddress inetAddress = InetAddress.getByName(inetAddrStr);
            isPCOnline = inetAddress.isReachable(ConstantsFor.TIMEOUT_650);
    
        }
        catch (IOException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        return isPCOnline;
    }
    
    public String writeLogToFile() {
        throw new InvokeEmptyMethodException("12.07.2019 (16:42)");
    }
    
    public String getPingResultStr() {
        String fileResultsName = getClass().getSimpleName() + ".res";
        try (OutputStream outputStream = new FileOutputStream(fileResultsName, true);
             PrintStream printStream = new PrintStream(outputStream, true, "UTF-8")
        ) {
            printStream.println(getExecution() + " " + LocalTime.now());
        }
        catch (IOException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        return FileSystemWorker.readFile(fileResultsName);
    }
    
    public String getExecution() {
        return TimeUnit.SECONDS.toMinutes(LocalTime.parse("17:30").toSecondOfDay() - LocalTime.now().toSecondOfDay()) + Do0213.MIN_LEFT_OFFICIAL;
    }
    
    private Runnable getI() {
        Do0213 do0213Monitor = new Do0213();
        do0213Monitor.setHostName(Do0213MonitorTest.MONITORED_HOST);
        return do0213Monitor;
    }
    
    private void syncIsPcOnline() throws UnknownHostException {
        byte[] addressBytes = InetAddress.getByName("10.200.214.80").getAddress();
        synchronized(do213MonFin) {
            while (!do213MonFin.isReach(InetAddress.getByAddress(addressBytes))) {
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
            isDO0213Online = isReach(ConstantsNet.LOCALHOST);
            
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
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_TESTING);
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