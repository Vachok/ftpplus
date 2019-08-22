// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.exe.runnabletasks.SpeedChecker;

import java.io.File;
import java.util.Date;
import java.util.concurrent.*;


/**
 @see SpeedChecker */
public class SpeedCheckerTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testCall() {
        try {
            Future<Long> aLongFuture = Executors.newSingleThreadExecutor().submit(new SpeedChecker());
            Long aLong = new SpeedChecker().call();
            Assert.assertTrue(aLong + TimeUnit.DAYS.toMillis(3) > System.currentTimeMillis(), new Date(aLong).toString());
        }
        catch (RuntimeException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test
    public void testRun() {
        new SpeedChecker().runMe();
        File chkMailFile = new File("ChkMailAndUpdateDB.chechMail");
        Assert.assertTrue(chkMailFile.exists());
        Assert.assertTrue(chkMailFile.lastModified() > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3));
    }
}