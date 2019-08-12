// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.OtherKnownDevices;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.restapi.internetuse.InternetUse;


/**
 @see InetIPUser
 @since 09.06.2019 (21:24) */
@SuppressWarnings("ALL") public class InetIPUserTest {
    
    
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
    @Test
    public void testShowLog() {
        ru.vachok.stats.SaveLogsToDB logsToDB = SaveLogsToDB.getI();
        Assert.assertNotNull(logsToDB.startScheduled());
        Assert.assertTrue(logsToDB.toString().contains("LOGS_TO_DB_EXT.showInfo"));
    }
}