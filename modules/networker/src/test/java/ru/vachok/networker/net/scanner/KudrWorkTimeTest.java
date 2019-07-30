// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.abstr.monitors.NetScanService;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.text.MessageFormat;
import java.time.LocalTime;
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
    
    private List<String> execList = NetKeeper.getKudrWorkTime();
    
    private NetScanService kudrService = new KudrWorkTime(true);
    
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
        catch (InterruptedException | ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
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
    
    @Test(enabled = false)
    public void testGetExecution() {
        try {
            String execution = kudrService.getExecution();
            Thread.sleep(150);
            Assert.assertTrue(execution.contains("Starting monitor!"), execution);
        }
        catch (InvokeEmptyMethodException | InterruptedException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetPingResultStr() {
        try {
            String resultStr = kudrService.getPingResultStr();
            System.out.println("kudrService.getPingResultStr() = " + resultStr);
            Assert.assertTrue(resultStr.contains("starting"));
        }
        catch (InvokeEmptyMethodException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testWriteLog() {
        try {
            Future<String> future = Executors.newSingleThreadExecutor().submit(kudrService::writeLog);
            future.get(1, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
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
    
    @Test(enabled = false)
    public void getExecution$$COPY() {
        int secondOfDay = LocalTime.now().toSecondOfDay();
        int officialStart = LocalTime.parse("08:30").toSecondOfDay();
        int officialEnd = LocalTime.parse("17:30").toSecondOfDay();
        execList.add(MessageFormat.format(KudrWorkTime.STARTING, LocalTime.now()));
        while (true) {
            if (!kudrService.isReach(InetAddress.getLoopbackAddress())) {
                break;
            }
            secondOfDay = LocalTime.now().toSecondOfDay();
            try {
                Thread.sleep(1001);
            }
            catch (InterruptedException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
        }
        System.out.println("secondOfDay = " + secondOfDay);
    }
}