// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.exe.runnabletasks.NetMonitorPTV;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 @see NetScanService
 @since 28.07.2019 (16:04) */
public class NetScanServiceTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private NetScanService netScanService = new NetMonitorPTV();
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
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
    public void testGetExecution() {
        String execution = netScanService.getExecution();
        Assert.assertTrue(execution.contains("ptv1.eatmeat.ru/10.200.214.104"));
    }
    
    @Test
    public void testGetPingResultStr() {
        String pingRes = netScanService.getPingResultStr();
        Assert.assertEquals(pingRes, "No pings yet.");
    }
    
    @Test
    public void testPingDevices() {
        Map<InetAddress, String> inetAddressStringMap = new HashMap<>();
        inetAddressStringMap.put(InetAddress.getLoopbackAddress(), "localhost");
        List<String> pingedList = netScanService.pingDevices(inetAddressStringMap);
        String pingedStr = new TForms().fromArray(pingedList);
        Assert.assertTrue(pingedStr.contains("<font color=\"#00ff69\">localhost = localhost/127.0.0.1 is true</font>"));
    }
    
    @Test
    public void testIsReach() {
        boolean isReach = netScanService.isReach(InetAddress.getLoopbackAddress());
        Assert.assertTrue(isReach);
    }
    
    @Test
    public void testWriteLogToFile() {
        
        try {
            String writeLog = netScanService.writeLogToFile();
            System.out.println("writeLog = " + writeLog);
        }
        catch (NullPointerException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetMonitoringRunnable() {
        Runnable runnable = netScanService.getMonitoringRunnable();
        Assert.assertTrue(runnable.toString().contains("NetMonitorPTV{pingResultLast"));
    }
    
    @Test
    public void testGetStatistics() {
        String statistics = netScanService.getStatistics();
        Assert.assertNull(statistics);
    }
}