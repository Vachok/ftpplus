package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TestConfigure;


/**
 @see DeadLockMonitor
 @since 20.06.2019 (14:21) */
public class DeadLockMonitorTest {
    
    
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
    
    
    /**
     @see DeadLockMonitor#call()
     */
    @Test
    public void testCall() {
        DeadLockMonitor deadLockMonitor = new DeadLockMonitor();
        String call = deadLockMonitor.call();
        Assert.assertEquals(call, "java.lang.NullPointerException: No deadlocks, good!");
    }
}