// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 @see NetScannerSvc
 @since 24.06.2019 (11:11) */
public class NetScannerSvcTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    
    /**
     @see NetScannerSvc#toString()
     */
    @Test
    public void testToString1() {
        NetScannerSvc netScannerSvc = NetScannerSvc.getInst();
        Assert.assertTrue(netScannerSvc.toString().contains(String.valueOf(netScannerSvc.hashCode())));
    }
    
    /**
     @see NetScannerSvc#theSETOfPcNames()
     */
    @Test
    public void testTheSETOfPcNames() {
        NetScannerSvc netScannerSvc = NetScannerSvc.getInst();
        Set<String> setOfPcNames = netScannerSvc.theSETOfPcNames();
        Assert.assertNotNull(setOfPcNames);
    }
    
    /**
     @see NetScannerSvc#theSETOfPCNamesPref(String)
     */
    @Test
    public void testTheSETOfPCNamesPref() {
        NetScannerSvc netScannerSvc = NetScannerSvc.getInst();
        Set<String> namesPref = netScannerSvc.theSETOfPCNamesPref("no");
        Assert.assertTrue(namesPref.size() > 3);
        Assert.assertTrue(namesPref.toArray()[new Random().nextInt(3)].toString().contains("<b"));
        DataConnectTo dataConnectTo = new RegRuMysql();
        final String sql = "SELECT * FROM `pcuserauto` ORDER BY `whenQueried` DESC LIMIT 1";
        try (Connection defaultConnection = dataConnectTo.getDefaultConnection("u0466446_velkom");
             PreparedStatement preparedStatement = defaultConnection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery();
        ) {
            while (resultSet.next()) {
                long stampLong = resultSet.getLong("stamp");
                Assert.assertTrue(stampLong > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)), new Date(stampLong).toString());
            }
            try (PreparedStatement ps2 = defaultConnection.prepareStatement("SELECT * FROM `velkompc` ORDER BY `velkompc`.`TimeNow` DESC LIMIT 1");
                 ResultSet resultSet1 = ps2.executeQuery()
            ) {
                while (resultSet1.next()) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String resultSet1String = resultSet1.getString(ConstantsFor.DBFIELD_TIMENOW);
                    Date parsedDate = dateFormat.parse(resultSet1String);
                    Assert.assertTrue(parsedDate.getTime() > (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2)), parsedDate.toString());
                }
            }
        }
        catch (SQLException | ParseException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    /**
     @see NetScannerSvc#theInfoFromDBGetter()
     */
    @Test
    public void testTheInfoFromDBGetter() {
        NetScannerSvc netScannerSvc = NetScannerSvc.getInst();
        String fromDBGetterResult = netScannerSvc.theInfoFromDBGetter();
        Assert.assertEquals(fromDBGetterResult, "ok");
        Assert.assertTrue(netScannerSvc.getMemoryInfo().contains("HeapMemoryUsage"), netScannerSvc.getMemoryInfo());
        netScannerSvc.setThePc("do0045");
        fromDBGetterResult = netScannerSvc.theInfoFromDBGetter();
        Assert.assertTrue(netScannerSvc.toString().contains("do0045.eatmeat.ru/10.200.213.200"), netScannerSvc.toString());
        Assert.assertEquals(fromDBGetterResult, "ok");
    }
}