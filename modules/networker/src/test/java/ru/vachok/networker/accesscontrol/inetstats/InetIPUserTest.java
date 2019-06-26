// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.abstr.InternetUse;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.net.enums.OtherKnownDevices;


/**
 @see InetIPUser
 @since 09.06.2019 (21:24) */
@SuppressWarnings("ALL") public class InetIPUserTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    /**
     @see InetIPUser#getUsage(String)
     */
    @Test
    public void testGetUsage() {
        InternetUse internetUse = new InetIPUser();
        String usageInet = internetUse.getUsage(OtherKnownDevices.DO0213_KUDR);
        Assert.assertTrue(usageInet.contains("DENIED SITES:"), usageInet);
    }
    
    /**
     @see InetIPUser#showLog()
     */
    @Test(enabled = false)
    public void testShowLog() {
        SaveLogsToDB dbSaver = new AppComponents().saveLogsToDB();
        String showLog = SaveLogsToDB.showInfo();
        Assert.assertNotNull(showLog);
        Assert.assertTrue(showLog.contains("LOGS_TO_DB_EXT.showInfo"));
    }
}