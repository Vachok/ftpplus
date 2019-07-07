// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.Pinger;
import ru.vachok.networker.componentsrepo.InvokeEmptyMethodException;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.OtherKnownDevices;

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
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertNull;


/**
 @see Do0213Monitor
 @since 07.07.2019 (9:08) */
public class Do0213MonitorTest implements Pinger {
    
    
    @Test
    public void testRun() {
        try {
            new Do0213Monitor().run();
        }
        catch (InvokeEmptyMethodException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void logicRun() {
        Assert.assertFalse(isReach(OtherKnownDevices.DO0213_KUDR));
        getLastPingFromDB();
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
            if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
                Assert.assertFalse(pingDevice(inetAddress));
            }
            else {
                Assert.assertTrue(pingDevice(inetAddress));
            }
            retBool = inetAddress.isReachable(ConstantsFor.TIMEOUT_650);
        }
        catch (IOException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        System.out.println("getPingResultStr() = " + getPingResultStr());
        return retBool;
    }
    
    private void getLastPingFromDB() {
        final String sql = "select * from worktime";
        DataConnectTo dataConnectTo = new RegRuMysql();
        MysqlDataSource mySqlDataSource = dataConnectTo.getDataSource();
        mySqlDataSource.setUser("u0466446_kudr");
        mySqlDataSource.setPassword("36e42yoak8");
        mySqlDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_LIFERPG);
        try (Connection connection = mySqlDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                System.out.println("in = " + resultSet.getLong("Timein"));
                System.out.println("out = " + resultSet.getLong("Timeout"));
            }
        }
        catch (SQLException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
}