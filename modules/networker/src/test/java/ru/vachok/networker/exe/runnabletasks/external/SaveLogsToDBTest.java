// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks.external;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @since 14.06.2019 (16:55) */
@SuppressWarnings("ALL") public class SaveLogsToDBTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private SaveLogsToDB db = new SaveLogsToDB();
    
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
    public void testRun() {
        db.startScheduled();
    }
    
    @Test
    public void testShowInfo() {
        db.showInfo();
    }
    
    @Test
    public void testGetI() {
        String toStr = SaveLogsToDB.getI().toString();
        Assert.assertTrue(toStr.contains("ru.vachok.stats.SaveLogsToDB"), toStr);
    }
    
    @Test
    public void testStartScheduled() {
        String startSched = db.startScheduled();
        Assert.assertTrue(startSched.contains("_access.log"), startSched);
    }
    
    @Test
    public void testTestToString() {
        String toStr = db.toString();
        System.out.println("toStr = " + toStr);
    }
}