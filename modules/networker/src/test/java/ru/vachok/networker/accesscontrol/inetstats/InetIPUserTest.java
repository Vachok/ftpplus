// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.abstr.InternetUse;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.net.enums.OtherKnownDevices;


/**
 @since 09.06.2019 (21:24) */
public class InetIPUserTest {
    
    
    @Test
    public void testGetUsage() {
        InternetUse internetUse = new InetIPUser();
        String usageInet = internetUse.getUsage(OtherKnownDevices.DO0213_KUDR);
        Assert.assertTrue(usageInet.contains("DENIED SITES:"), usageInet);
    }
    
    @Test(enabled = false)
    public void testShowLog() {
        SaveLogsToDB dbSaver = new AppComponents().saveLogsToDB();
        String showLog = SaveLogsToDB.showInfo();
        Assert.assertNotNull(showLog);
        Assert.assertTrue(showLog.contains("LOGS_TO_DB_EXT.showInfo"));
    }
}