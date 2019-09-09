package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see DBDownloader
 @since 08.09.2019 (17:46) */
public class DBDownloaderTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DBDownloader.class.getSimpleName(), System.nanoTime());
    
    private DBDownloader dbDownloader = new DBDownloader(new String[]{"inetstats", "idrec"});
    
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
    public void testSyncData() {
        String data = dbDownloader.syncData();
        Assert.assertTrue(data.contains("inetstats, RegRuMysqlLoc["), data);
    }
    
    @Test
    public void testToString() {
        String s = dbDownloader.toString();
        Assert.assertTrue(s.contains("DBSyncronizer["), s);
    }
}