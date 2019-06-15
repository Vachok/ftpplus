// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;

import java.awt.*;


/**
 @since 09.06.2019 (21:24) */
public class InetIPUserTest {
    
    
    @Test
    public void testGetUsage() {
        throw new IllegalComponentStateException("15.06.2019 (17:36)");
    }
    
    @Test(enabled = false)
    public void testShowLog() {
        SaveLogsToDB dbSaver = new AppComponents().saveLogsToDB();
        String showLog = SaveLogsToDB.showInfo();
        Assert.assertNotNull(showLog);
        Assert.assertTrue(showLog.contains("LOGS_TO_DB_EXT.showInfo"));
    }
}