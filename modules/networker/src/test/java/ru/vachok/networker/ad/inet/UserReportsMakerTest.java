package ru.vachok.networker.ad.inet;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see UserReportsMaker
 @since 14.10.2019 (11:58) */
public class UserReportsMakerTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(UserReportsMaker.class
            .getSimpleName(), System.nanoTime());
    
    private UserReportsMaker userReportsMaker;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @BeforeMethod
    public void initReporter() {
        this.userReportsMaker = new UserReportsMaker("10.200.202.55");
    }
    
    @Test
    public void testGetInfoAbout() {
        String makerInfoAbout = userReportsMaker.getInfoAbout("asemenov.csv");
        Assert.assertTrue(makerInfoAbout.contains("asemenov.csv"), makerInfoAbout);
    
        this.userReportsMaker = new UserReportsMaker("10.200.213.190");
        makerInfoAbout = userReportsMaker.getInfoAbout("evyrodova.csv");
        Assert.assertTrue(makerInfoAbout.contains("evyrodova.csv"), makerInfoAbout);
    
        this.userReportsMaker = new UserReportsMaker("10.200.214.53");
        makerInfoAbout = userReportsMaker.getInfoAbout("trofimenkov.csv");
        Assert.assertTrue(makerInfoAbout.contains("trofim"), makerInfoAbout);
    }
    
    @Test
    public void testGetInfo() {
        String userReportsMakerInfo = userReportsMaker.getInfo();
        Assert.assertTrue(userReportsMakerInfo.contains("microsoft.com:443"), userReportsMakerInfo);
    }
    
    @Test
    public void testTestToString() {
        String toString = userReportsMaker.toString();
        Assert.assertEquals(toString, "UserReportsMaker{userCred='10.200.202.55'}");
    }
}