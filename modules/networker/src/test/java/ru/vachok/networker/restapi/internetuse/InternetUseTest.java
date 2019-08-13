package ru.vachok.networker.restapi.internetuse;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.accesscontrol.inetstats.InetIPUser;
import ru.vachok.networker.accesscontrol.inetstats.InetUserPCName;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see InternetUse
 @since 13.08.2019 (8:46) */
public class InternetUseTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(InternetUse.class.getSimpleName(), System.nanoTime());
    
    private InternetUse internetUse = new InetUserPCName();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testGetUsage() {
        String inetUsage = internetUse.getUsage("do0001");
        Assert.assertTrue(inetUsage.contains("TCP_TUNNEL/200 CONNECT"), inetUsage);
        internetUse = new InetIPUser();
        inetUsage = internetUse.getUsage("do0001");
        System.out.println("inetUsage = " + inetUsage);
    }
    
    @Test
    public void testShowLog() {
        internetUse.showLog();
        testCleanTrash();
    }
    
    @Test
    public void testGetResponseTime() {
        String userCred = "do0001";
        String responseTime = internetUse.getConnectStatistics(userCred);
        System.out.println("responseTime = " + responseTime);
    }
    
    private void testCleanTrash() {
        int cleanTrash = internetUse.cleanTrash();
        Assert.assertFalse(cleanTrash > 0, String.valueOf(cleanTrash));
    }
}