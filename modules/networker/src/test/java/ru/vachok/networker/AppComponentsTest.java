// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.Contract;
import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.services.SimpleCalculator;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.net.monitor.DiapazonScan;
import ru.vachok.networker.net.ssh.PfLists;
import ru.vachok.networker.net.ssh.SshActs;
import ru.vachok.networker.restapi.props.InitProperties;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @see AppComponents */
@SuppressWarnings("ALL")
public class AppComponentsTest {


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

    @Test
    public void testGetMailProps() {
        Properties props = AppComponents.getMailProps();
        Assert.assertTrue(props.size() > 3);
        Assert.assertEquals(props.getProperty("host"), ConstantsFor.SRV_MAIL3);
    }

    @Test
    public void testGetProps() {
        Properties appProps = InitProperties.getTheProps();
        Assert.assertTrue(appProps.size() > 12, "AppProps size = " + appProps.size());
        Assert.assertTrue(appProps.getProperty("server.port").equals("8880"));
        Assert.assertTrue(appProps.getProperty("application.name").equals("ru.vachok.networker-"));
    }

    @Test
    public void testIpFlushDNS() {
        try {
            String cp866 = new String(UsefulUtilities.ipFlushDNS().getBytes(), "CP866");
            Assert.assertTrue(cp866.contains("DNS"), cp866);

        }
        catch (UnsupportedEncodingException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }

    @Test
    public void testConnection() {
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_TESTING)) {
            boolean connectionValid = connection.isValid(10);
            Assert.assertTrue(connectionValid);
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }

    @Test
    public void testVisitor() {
        HttpServletRequest request = new MockHttpServletRequest();
        Visitor visitor = new Visitor(request);
        Map<Long, Visitor> map = ExitApp.getVisitsMap();
        long timeSession = request.getSession().getCreationTime();
        map.put(timeSession, visitor);
        Assert.assertNotNull(map.get(timeSession));
    }

    @Test
    public void testDiapazonedScanInfo() {
        try {
            DiapazonScan instance = DiapazonScan.getInstance();
            String diapazonInfo = instance.getExecution();
            Assert.assertTrue(diapazonInfo.contains("a href=\"/showalldev\""), diapazonInfo);
            Assert.assertTrue(instance.getStatistics().contains("13 SpecVersion"), instance.getStatistics());
        }
        catch (TaskRejectedException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }

    @Test
    public void testGetUserPref() {
        Preferences pref = InitProperties.getUserPref();
        try {
            Assert.assertNotNull(pref.keys());
            Assert.assertTrue(pref.keys().length > 2);
        }
        catch (BackingStoreException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }

    @Contract(" -> fail")
    public static AbstractBeanFactoryBasedTargetSource configurableApplicationContext() {
        throw new IllegalComponentStateException("Moved to: " + IntoApplication.class.getSimpleName());
    }

    @Test
    public void testSimpleCalculator() {
        SimpleCalculator simpleCalculator = new AppComponents().simpleCalculator();
        String stampFromDate = simpleCalculator.getStampFromDate("07-01-1984-02-00");
        Assert.assertEquals(stampFromDate, "442278000000");
    }

    @Test
    public void testSshActs() {
        SshActs acts = new AppComponents().sshActs();
        String actsInet = acts.getInet();
        Assert.assertNull(actsInet);
    }

    @Test
    public void testThreadConfig() {
        ThreadConfig threadConfig = AppComponents.threadConfig();
        String toStr = threadConfig.toString();
        Assert.assertTrue(toStr.contains("ThreadConfig{java.util.concurrent.ThreadPoolExecutor"), toStr);
    }

    @Test
    public void testAdSrv() {
        ADSrv adSrv = AppComponents.adSrv();
        String toStr = adSrv.toString();
        Assert.assertTrue(toStr.contains("ADSrv{"), toStr);
    }

    @Test
    public void testScanOnline() {
        NetScanService scanOnline = NetScanService.getInstance("ScanOnline");
        boolean condition = NetScanService.isReach(InetAddress.getLoopbackAddress().getHostAddress());
        Assert.assertTrue(condition, "getLoopbackAddress " + false);
        try {
            condition = NetScanService.isReach(InetAddress.getByAddress(InetAddress.getByName("10.200.213.1").getAddress()).getHostAddress());
            Assert.assertTrue(condition, "10.200.213.1 " + false);
            condition = NetScanService.isReach(InetAddress.getByAddress(InetAddress.getByName("8.8.8.8").getAddress()).getHostAddress());
            Assert.assertTrue(condition, "8.8.8.8 " + false);
        }
        catch (UnknownHostException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }

    }

    @Test
    public void testGetPFLists() {
        PfLists pfLists = new AppComponents().getPFLists();
        String toStr = pfLists.toString();
        Assert.assertTrue(toStr.contains("{\"vipNet\":"), toStr);
    }

    @Test
    public void testTestToString() {
        String toStr = new AppComponents().toString();
        Assert.assertTrue(toStr.contains("Nothing to show..."), toStr);
    }
}