// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.util.Deque;


/**
 @see ScanFilesWorker
 @since 24.07.2019 (0:05) */
public class ScanFilesWorkerTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private NetKeeper scanFiles = new ScanFilesWorker();
    
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
        Deque<InetAddress> address = scanFiles.getOnlineDevicesInetAddress();
        Assert.assertTrue(address.size() > 1);
    }
    
    @Test
    public void testGetCurrentScanLists() {
    }
    
    @Test
    public void testGetCurrentScanFiles() {
    }
    
    @Test
    public void testGetScanFiles() {
    }
    
    @Test
    public void testGetRunMin() {
    }
}