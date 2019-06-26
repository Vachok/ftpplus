// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TestConfigure;
import ru.vachok.networker.abstr.InternetUse;
import ru.vachok.networker.net.enums.OtherKnownDevices;


/**
 @since 09.06.2019 (21:30) */
@SuppressWarnings("ALL") public class InetUserPCNameTest {
    
    
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
    public void testGetUsage() {
        InternetUse internetUse = new InetUserPCName();
        String usageInet = internetUse.getUsage(OtherKnownDevices.DO0213_KUDR);
        Assert.assertTrue(usageInet.contains("DENIED SITES:"), usageInet);
    }
    
    @Test
    public void testShowLog() {
        InternetUse internetUse = new InetUserPCName();
        internetUse.showLog();
        Assert.assertTrue(internetUse instanceof InetUserPCName);
    }
}