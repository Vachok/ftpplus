// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.util.Deque;


/**
 @since 15.06.2019 (21:11) */
public class NetScanFileWorkerTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    @Test
    public void testGetDequeOfOnlineDev() {
        try {
            Deque<String> devOnline = NetScanFileWorker.getDequeOfOnlineDev();
        }
        catch (Exception e) {
            Assert.assertNull(e);
        }
    }
}