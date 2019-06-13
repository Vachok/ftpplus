// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.exe.runnabletasks.SpeedChecker;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


public class SpeedCheckerTest {
    
    
    @Test
    public void rutClass() {
        Callable<Long> speedChecker = new SpeedChecker();
        try {
            Long aLong = speedChecker.call();
            Assert.assertTrue(aLong + TimeUnit.DAYS.toMillis(14) > System.currentTimeMillis());
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
}