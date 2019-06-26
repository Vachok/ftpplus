// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks.external;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.TestConfigure;


/**
 @since 14.06.2019 (16:55) */
@SuppressWarnings("ALL") public class SaveLogsToDBTest {
    
    
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
    public void testGetI() {
        SaveLogsToDB saveLogsToDB = new SaveLogsToDB();
        try {
            Assert.assertTrue(saveLogsToDB.getI() instanceof ru.vachok.stats.SaveLogsToDB);
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    @Test
    public void testStartScheduled() {
        try {
            SaveLogsToDB.startScheduled();
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    @Test
    public void testShowInfo() {
        try {
            String showInfoStr = SaveLogsToDB.showInfo();
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    @Test(enabled = false)
    public void testRun() {
        SaveLogsToDB saveLogsToDB = new SaveLogsToDB();
        saveLogsToDB.run();
    }
}