// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.exe.runnabletasks.SpeedChecker;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class SpeedCheckerTest {
    
    
    @Test
    public void testCall() {
        try {
            Long aLong = new SpeedChecker().call();
            Assert.assertTrue(aLong + TimeUnit.DAYS.toMillis(3) > System.currentTimeMillis(), new Date(aLong).toString());
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test
    public void testRun() {
        new SpeedChecker().run();
        File chkMailFile = new File("ChkMailAndUpdateDB.chechMail");
        Assert.assertTrue(chkMailFile.exists());
        Assert.assertTrue(chkMailFile.lastModified() > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3));
    }
}