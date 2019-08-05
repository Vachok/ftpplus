// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.Deque;
import java.util.Random;


/**
 @see ScanFilesWorker
 @since 24.07.2019 (0:05) */
public class ScanFilesWorkerTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
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
    public void testGetOnlineDevicesInetAddress() {
        testMakeFilesMap();
        File lanFile = NetKeeper.getCurrentScanFiles().get(new Random().nextInt(8));
        try (OutputStream outputStream = new FileOutputStream(lanFile);
             PrintStream printStream = new PrintStream(outputStream, true)) {
            printStream.println("test");
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Deque<InetAddress> addressOnline = new ScanFilesWorker().getDequeOfOnlineDev();
        Assert.assertTrue(addressOnline.size() == 1, MessageFormat
            .format("Size of files Array: {0}\n{1}", addressOnline.size(), new TForms().fromArray(addressOnline)));
        Assert.assertEquals(addressOnline.getFirst(), InetAddress.getLoopbackAddress());
    }
    
    @Test
    public void testMakeFilesMap() {
        ScanFilesWorker.makeFilesMap();
        Assert.assertTrue(NetKeeper.getScanFiles().size() == 9);
    }
    
    @Test
    public void testToString1() {
        String toStr = new ScanFilesWorker().toString();
        Assert.assertTrue(toStr.contains("ScanFilesWorker[\nNetPinger{"));
    }
}