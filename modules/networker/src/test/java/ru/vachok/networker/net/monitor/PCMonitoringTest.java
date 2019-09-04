// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.testng.Assert.assertNull;


/**
 @see PCMonitoring
 @since 01.08.2019 (9:03) */
public class PCMonitoringTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private String inetAddrStr = "127.0.0.1";
    
    private NetScanService pcMonitor = new PCMonitoring(inetAddrStr, 7);
    
    private List<String> results = new ArrayList<>();
    
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
        String monitorExecution = pcMonitor.getExecution();
        Assert.assertFalse(monitorExecution.isEmpty());
    }
    
    @Test
    public void testGetPingResultStr() {
        try {
            pcMonitor.getPingResultStr();
        }
        catch (IndexOutOfBoundsException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void exp$$WriteLog() {
        File logFile = new File(inetAddrStr + ".res.test");
        try (OutputStream outputStream = new FileOutputStream(logFile, true);
             PrintStream printStream = new PrintStream(outputStream, true)) {
            printStream.println(new TForms().fromArray(results));
        }
        catch (IOException e) {
            assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetMonitoringRunnable() {
        Assert.assertEquals(pcMonitor, pcMonitor.getMonitoringRunnable());
    }
    
    @Test
    public void testGetStatistics() {
        String monitorStatistics = pcMonitor.getStatistics();
        Assert.assertFalse(monitorStatistics.isEmpty());
    }
    
    @Test
    public void testRun() {
        Future<?> submit = Executors.newSingleThreadExecutor().submit(pcMonitor);
        try {
            Assert.assertNull(submit.get(8, TimeUnit.SECONDS));
        }
        catch (TimeoutException | ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (InterruptedException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    public void testWriteLog() {
        Assert.assertTrue(new File(inetAddrStr + ".res.test").exists());
        Assert.assertTrue(new File(inetAddrStr + ".res.test").lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(5)));
    }
}