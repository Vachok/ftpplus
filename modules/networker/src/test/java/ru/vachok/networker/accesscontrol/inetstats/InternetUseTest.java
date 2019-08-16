package ru.vachok.networker.accesscontrol.inetstats;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;


/**
 @see InternetUse
 @since 13.08.2019 (8:46) */
public class InternetUseTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(InternetUse.class.getSimpleName(), System.nanoTime());
    
    private InformationFactory internetUse = InformationFactory.getInstance(InformationFactory.TYPE_WEEKLYINETSTATS);
    
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
        String inetUsage = internetUse.getInfoAbout("do0001");
        Assert.assertTrue(inetUsage.contains("TCP_TUNNEL/200 CONNECT"), inetUsage);
        internetUse = new InetIPUser();
        inetUsage = internetUse.getInfoAbout("do0001");
        System.out.println("inetUsage = " + inetUsage);
    }
    
    @Test
    public void testShowLog() {
        internetUse.getInfo();
        testCleanTrash();
    }
    
    private void testCleanTrash() {
        int cleanTrash = InternetUse.getCleanedRows();
        Assert.assertFalse(cleanTrash > 0, String.valueOf(cleanTrash));
    }
    
    @Test
    public void testGetResponseTime() {
        String userCred = "do0001";
        String responseTime = ((InternetUse) internetUse).getConnectStatistics();
        System.out.println("responseTime = " + responseTime);
    }
}