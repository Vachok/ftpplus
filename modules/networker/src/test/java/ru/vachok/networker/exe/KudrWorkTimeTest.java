// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.monitors.NetScanService;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.scanner.KudrWorkTime;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


/**
 @see KudrWorkTime
 @since 12.07.2019 (0:46) */
public class KudrWorkTimeTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private NetScanService kudrService = new KudrWorkTime();
    
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
    public void kudrMonitorTest() {
        Runnable runnable = kudrService.getMonitoringRunnable();
        Future<?> submit = Executors.newSingleThreadExecutor().submit(runnable);
        try {
            Object monitorResult = submit.get(1000, TimeUnit.MILLISECONDS);
            Assert.assertNull(monitorResult);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetMapOfConditionsTypeNameTypeCondition() {
        Map<String, Object> condition = ((KudrWorkTime) kudrService).getMapOfConditionsTypeNameTypeCondition();
        Assert.assertNotNull(condition);
        Assert.assertTrue(condition.isEmpty());
    }
    
    @Test
    public void testPingDevices() {
        Map<InetAddress, String> devToPing = new HashMap<>();
        devToPing.put(InetAddress.getLoopbackAddress(), "local");
        List<String> pingedDevs = kudrService.pingDevices(devToPing);
        Assert.assertTrue(new TForms().fromArray(pingedDevs).contains("Pinging local, with timeout 24 seconds - true"));
    }
    
    @Test
    public void testIsReach() {
        boolean isReachIP = kudrService.isReach(InetAddress.getLoopbackAddress());
        Assert.assertTrue(isReachIP);
    }
    
    @Test
    public void testGetExecution() {
        try {
            String execution = kudrService.getExecution();
            
            System.out.println("execution = " + execution);
        }
        catch (InvokeEmptyMethodException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetPingResultStr() {
        try {
            System.out.println("kudrService.getPingResultStr() = " + kudrService.getPingResultStr());
        }
        catch (InvokeEmptyMethodException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testWriteLogToFile() {
        try {
            System.out.println("kudrService.writeLogToFile() = " + kudrService.writeLogToFile());
        }
        catch (InvokeEmptyMethodException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetStatistics() {
        String statistics = kudrService.getStatistics();
        Assert.assertTrue(statistics.isEmpty(), statistics);
    }
    
    @Test
    public void testTestToString() {
        String toStr = kudrService.toString();
        Assert.assertTrue(toStr.contains("Kudr{mapOfConditionsTypeNameTypeCondition"), toStr);
    }
}