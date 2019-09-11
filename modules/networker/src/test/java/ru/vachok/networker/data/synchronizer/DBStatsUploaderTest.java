package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see DBStatsUploader
 @since 08.09.2019 (10:16) */
public class DBStatsUploaderTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DBStatsUploader.class.getSimpleName(), System.nanoTime());
    
    private DBStatsUploader dbStatsUploader;
    
    private String aboutWhat = "10.200.210.64";
    
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
    public void initClass() {
        this.dbStatsUploader = new DBStatsUploader();
    }
    
    @Test
    public void testTestToString() {
        String toStr = dbStatsUploader.toString();
        Assert.assertTrue(toStr.contains("syncTable='velkompc'"), toStr);
    }
    
    @Test
    public void testSyncData() {
        try {
            String syncDt = dbStatsUploader.syncData();
        }
        catch (IllegalArgumentException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        dbStatsUploader.setOption(aboutWhat);
        dbStatsUploader.setOption(new String[]{aboutWhat});
        String toStr = dbStatsUploader.toString();
        Assert.assertTrue(toStr.contains("DBStatsUploader{"), toStr);
        Assert.assertTrue(toStr.contains("syncTable='10.200.210.64'"), toStr);
    }
}