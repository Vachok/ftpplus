// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.exe.runnabletasks.SpeedChecker;

import java.util.concurrent.TimeUnit;


public class SpeedCheckerTest {
    
    
    @Test
    public void testCall() {
        try {
            Long aLong = new SpeedChecker().call();
            Assert.assertTrue(aLong + TimeUnit.DAYS.toMillis(14) > System.currentTimeMillis());
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test
    public void testRun() {
        new SpeedChecker().run();
    }
}