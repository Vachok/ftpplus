package ru.vachok.networker.info;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.net.InetAddress;


public class NetScanServiceTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetScanService.class.getSimpleName(), System
            .nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testIsReach() {
        boolean isReachGateway = NetScanService.isReach("10.200.200.1");
        Assert.assertTrue(isReachGateway);
    }
    
    @Test
    public void testGetByName() {
        InetAddress do0001 = NetScanService.getByName("do0001");
        Assert.assertEquals(do0001.getHostAddress(), "10.200.214.53");
    }
    
    @Test
    public void testGetI() {
        NetScanService scanOnline = NetScanService.getInstance("do0045");
        String toStr = scanOnline.toString();
        Assert.assertTrue(toStr.contains("last ExecScan:"), toStr);
        toStr = NetScanService.getInstance(NetScanService.PTV).toString();
        Assert.assertTrue(toStr.contains("NetMonitorPTV{"), toStr);
    }
}