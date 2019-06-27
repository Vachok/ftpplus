// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see DeadLockMonitor
 @since 20.06.2019 (14:21) */
public class DeadLockMonitorTest {
    
    
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
    
    
    /**
     @see DeadLockMonitor#call()
     */
    @Test
    public void testCall() {
        DeadLockMonitor deadLockMonitor = new DeadLockMonitor();
        String call = deadLockMonitor.call();
        Assert.assertTrue(call == null || call.contains("java.lang.NullPointerException: No deadlocks, good!"));
    }
}