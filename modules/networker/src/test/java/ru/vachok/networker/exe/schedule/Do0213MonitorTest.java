// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.Pinger;
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
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertNull;


/**
 @see Do0213Monitor
 @since 07.07.2019 (9:08) */
public class Do0213MonitorTest implements Pinger {
    
    
    private DataConnectTo dataConnectTo = new RegRuMysql();
    
    private MysqlDataSource mySqlDataSource = dataConnectTo.getDataSource();
    
    private long timeInCounting;
    
    public long getTimeInCounting() {
        return timeInCounting;
    }
    
    @BeforeMethod
    public void setDSParams() {
        mySqlDataSource.setUser("u0466446_kudr");
        mySqlDataSource.setPassword("36e42yoak8");
        mySqlDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_LIFERPG);
    }
    
    @Test(enabled = false)
    public void testRun() {
        Pinger monitor213 = Do0213Monitor.getI();
        new Thread((Runnable) monitor213).start();
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
            Thread.currentThread().interrupt();
        }
        System.out.println("monitor213 = " + monitor213.getTimeToEndStr());
    }
    
    @Test
    public void trueRun() {
        Assert.assertTrue(getTimeToEndStr().contains("left official"));
    }
    
    @Test
    public void toStringTest() {
        System.out.println(Do0213Monitor.getI().toString());
    }
    
    @Test(enabled = false)
    public void logicRun() {
        downloadLastPingFromDB();
        uploadLastPingToDB();
        System.out.println("timeInCounting = " + timeInCounting);
    }
    
    @Override public String getPingResultStr() {
        String fileResultsName = getClass().getSimpleName() + ".res";
        try (OutputStream outputStream = new FileOutputStream(fileResultsName, true);
             PrintStream printStream = new PrintStream(outputStream, true, "UTF-8")) {
            printStream.println(getTimeToEndStr() + " " + LocalTime.now());
        }
        catch (IOException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        return FileSystemWorker.readFile(fileResultsName);
    }
    
    @Override public String getTimeToEndStr() {
        return TimeUnit.SECONDS.toMinutes(LocalTime.parse("17:30").toSecondOfDay() - LocalTime.now().toSecondOfDay()) + " minutes left official";
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
        System.out.println("getPingResultStr() = " + getPingResultStr());
        return retBool;
    }
    
    private void downloadLastPingFromDB() {
        final String sql = "select * from worktime";
        try (Connection connection = mySqlDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                parseRS(resultSet);
            }
        }
        catch (SQLException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    private void parseRS(ResultSet rsFromDB) throws SQLException {
        String dateFromDB = rsFromDB.getString("Date");
        long timeinStamp = rsFromDB.getLong(ConstantsFor.DBFIELD_TIMEIN);
        long timeoutStamp = rsFromDB.getLong(ConstantsFor.DBFIELD_TIMEOUT);
        if (timeoutStamp > 0) {
            uploadLastPingToDB();
        }
        else {
            monitorDO213(timeinStamp);
        }
    }
    
    private void monitorDO213(final long timeinStamp) {
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
    
    private void uploadLastPingToDB() {
        final String sql = "INSERT INTO `u0466446_liferpg`.`worktime` (`Date`, `Timein`, `Timeout`) VALUES (?, ?, ?);";
        try (Connection connection = mySqlDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
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
}