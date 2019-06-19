// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks.external;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;


/**
 @since 14.06.2019 (16:55) */
@SuppressWarnings("ALL") public class SaveLogsToDBTest {
    
    
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
    
    @Test
    public void testRun() {
        SaveLogsToDB saveLogsToDB = new SaveLogsToDB();
        saveLogsToDB.run();
    }
}