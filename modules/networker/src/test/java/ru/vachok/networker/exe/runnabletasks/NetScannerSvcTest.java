// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.springframework.context.ConfigurableApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.UsefulUtilites;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.MessageFormat;
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
    
    private NetScannerSvc inst = NetScannerSvc.getInst();
    
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
     @see NetScannerSvc#theSETOfPcNames()
     */
    @Test
    public void testTheSETOfPcNames() {
        Set<String> setOfPcNames = inst.theSETOfPcNames();
        Assert.assertNotNull(setOfPcNames);
        try (ConfigurableApplicationContext context = IntoApplication.getConfigurableApplicationContext()) {
            context.stop();
        }
        catch (RuntimeException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
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
        try (Connection defaultConnection = dataConnectTo.getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
             PreparedStatement preparedStatement = defaultConnection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery();
        ) {
            while (resultSet.next()) {
                String dateStr = resultSet.getString(ConstantsNet.DB_FIELD_WHENQUERIED);
                Date parsedDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(dateStr);
                long stampLong = parsedDate.getTime();
                Assert.assertTrue(stampLong > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)), new Date(stampLong).toString());
            }
            try (PreparedStatement ps2 = defaultConnection.prepareStatement("SELECT * FROM `velkompc` ORDER BY `velkompc`.`idvelkompc` DESC LIMIT 1");
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
        netScannerSvc.setThePc("do0045");
        fromDBGetterResult = netScannerSvc.theInfoFromDBGetter();
        Assert.assertTrue(netScannerSvc.toString().contains("do0045.eatmeat.ru/10.200.213.200"), netScannerSvc.toString());
        Assert.assertEquals(fromDBGetterResult, "ok");
    }
    
    @Test
    public void testGetInst() {
        Assert.assertTrue(inst.toString().contains("NetScannerSvc{"), inst.toString());
    }
    
    @Test
    public void testGetInputWithInfoFromDB() {
        String infoFromDB = inst.getInputWithInfoFromDB();
        Assert.assertEquals(infoFromDB, NetScannerSvc.class.getSimpleName());
    }
    
    @Test
    public void testSetInputWithInfoFromDB() {
        NetScannerSvc.setInputWithInfoFromDB(this.getClass().getSimpleName());
        Assert.assertEquals(inst.getInputWithInfoFromDB(), this.getClass().getSimpleName());
    }
    
    @Test
    public void testGetThePc() {
        String instThePc = inst.getThePc();
        Assert.assertEquals(instThePc, "PC");
    }
    
    @Test
    public void testSetThePc() {
        inst.setThePc(UsefulUtilites.thisPC());
        Assert.assertEquals(inst.getThePc(), UsefulUtilites.thisPC());
    }
    
    @Test
    public void testGetOnLinePCsNum() {
        int pCsNum = inst.getOnLinePCsNum();
        Assert.assertTrue((pCsNum == 0), MessageFormat.format("inst.getOnLinePCsNum({0})", pCsNum));
    }
    
    @Test
    public void testSetOnLinePCsNum() {
        inst.setOnLinePCsNum(150);
        int pCsNum = inst.getOnLinePCsNum();
        Assert.assertTrue((pCsNum == 150), MessageFormat.format("inst.getOnLinePCsNum({0})", pCsNum));
    }
    
    @Test
    public void testTestToString() {
        Assert.assertTrue(inst.toString().contains("NetScannerSvc{"), inst.toString());
    }
}