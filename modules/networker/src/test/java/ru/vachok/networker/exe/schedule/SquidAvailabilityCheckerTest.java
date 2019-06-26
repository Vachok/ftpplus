// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.TestConfigure;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @since 20.06.2019 (9:54) */
@SuppressWarnings("ALL") public class SquidAvailabilityCheckerTest {
    
    
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
    public void testRun() {
        SquidAvailabilityChecker squidAvailabilityChecker = new SquidAvailabilityChecker();
        try {
            squidAvailabilityChecker.call();
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        Assert.assertTrue(new File("SquidAvailabilityChecker.log").lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
    }
    
    @Test(timeOut = 60000)
    public void testCall() {
        SquidAvailabilityChecker squidAvailabilityChecker = new SquidAvailabilityChecker();
        try {
            String call = squidAvailabilityChecker.call();
            Assert.assertTrue(call.contains("/usr/local/etc/squid/squid.conf (squid)"));
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
}