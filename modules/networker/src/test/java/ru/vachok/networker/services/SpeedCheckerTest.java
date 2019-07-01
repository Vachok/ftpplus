// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.exe.runnabletasks.SpeedChecker;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class SpeedCheckerTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
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