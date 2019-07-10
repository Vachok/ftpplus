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
import ru.vachok.networker.abstr.Pinger;
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
    
    
    private DataConnectTo dataConnectTo = new RegRuMysql();
    
    private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    
    private long timeInCounting;
    
    private long timeoutStamp;
    
    private String dateFromDB = "07-01-1984";
    
    private long timeIn;
    
    private long timeinStamp;
    
    public long getTimeInCounting() {
        return timeInCounting;
    }
    
    @Test(enabled = false, timeOut = 20000)
    public void testRun() {
        Do0213Monitor monitor213 = new Do0213Monitor();
        monitor213.run();
        System.out.println("monitor213 = " + monitor213.getTimeToEndStr());
    }
    
    @Test
    public void trueRun() {
        Assert.assertTrue(getTimeToEndStr().contains("left official"));
    }
    
    @Test
    public void toStringTest() {
        System.out.println(new Do0213Monitor());
    }
    
    @Test(enabled = false)
    public void logicRun() {
        downloadLastDatabaseSavedConditions();
        uploadLastPingToDB();
        System.out.println("timeInCounting = " + timeInCounting);
    }
    
    @Test(enabled = false)
    public void openInet() { //fixme 09.07.2019 (11:19)
        TemporaryFullInternet temporaryFullInternet = new TemporaryFullInternet("10.200.213.85", 9, "add");
        String resultOfOpen = temporaryFullInternet.call();
        Assert.assertTrue(resultOfOpen.contains("10.200.213.85"), resultOfOpen);
    }
    
    @Test
    public void testCurrentDBVariant() {
        downloadLastDatabaseSavedConditions();
        
        boolean isTimestampBiggerThatZero = timeoutStamp > 0;
        boolean isDateEqualsToday = dateCheck(this.dateFromDB);
        boolean isDO0213Online = isReach("10.200.213.85");
        
        whatHappensUnderTheseConditions(isTimestampBiggerThatZero, isDateEqualsToday, isDO0213Online);
    }
    
    @Test
    public void testEightPMCondition() {
        downloadLastDatabaseSavedConditions();
    
        try {
            Date eightPM = dateFormat.parse(dateFromDB);
            long eightPMLong = eightPM.getTime() + (LocalTime.parse("20:00").toSecondOfDay() * 1000);
            this.dateFromDB = dateFormat.format(new Date(eightPMLong));
        }
        catch (ParseException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @SuppressWarnings("ConstantConditions")
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
    
    @Override public String getPingResultStr() {
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
    
    @Override public String getTimeToEndStr() {
        return TimeUnit.SECONDS.toMinutes(LocalTime.parse("17:30").toSecondOfDay() - LocalTime.now().toSecondOfDay()) + Do0213Monitor.MIN_LEFT_OFFICIAL;
    }
    
    @Override public boolean isReach(String inetAddrStr) {
        boolean retBool = false;
        try {
            InetAddress inetAddress = InetAddress.getByName(inetAddrStr);
            Assert.assertTrue(pingDevice(inetAddress));
            retBool = inetAddress.isReachable(ConstantsFor.TIMEOUT_650);
        }
        catch (IOException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        return retBool;
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