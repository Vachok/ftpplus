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
    public void testShowInfo() {
        int info = db.showInfo();
        Assert.assertTrue(info > 100);
    }
    
    @Test
    public void testTestToString() {
        String toStr = db.toString();
        Assert.assertTrue(toStr.contains("SaveLogsToDB["));
    }
    
    @Test
    public void testGetDBInfo() {
        int info = db.getDBInfo();
        Assert.assertTrue(info > 100);
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = db.getInfoAbout("40");
        System.out.println("infoAbout = " + infoAbout);
    }
}