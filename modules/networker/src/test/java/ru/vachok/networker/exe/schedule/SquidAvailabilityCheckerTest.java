// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @since 20.06.2019 (9:54) */
@SuppressWarnings("ALL") public class SquidAvailabilityCheckerTest {
    
    
    @Test
    public void testRun() {
        SquidAvailabilityChecker squidAvailabilityChecker = new SquidAvailabilityChecker();
        squidAvailabilityChecker.run();
        Assert.assertTrue(new File("SquidAvailabilityChecker.log").lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
    }
    
    @Test(timeOut = 12000)
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