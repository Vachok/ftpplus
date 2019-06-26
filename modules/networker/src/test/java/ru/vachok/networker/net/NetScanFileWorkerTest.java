// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TestConfigure;

import java.util.Deque;


/**
 @since 15.06.2019 (21:11) */
public class NetScanFileWorkerTest {
    
    
    private final TestConfigure testConfigure = new TestConfigure(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigure.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigure.afterClass();
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