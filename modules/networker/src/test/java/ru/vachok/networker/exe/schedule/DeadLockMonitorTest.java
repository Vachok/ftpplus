package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.Test;


/**
 @see DeadLockMonitor
 @since 20.06.2019 (14:21) */
public class DeadLockMonitorTest {
    
    
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