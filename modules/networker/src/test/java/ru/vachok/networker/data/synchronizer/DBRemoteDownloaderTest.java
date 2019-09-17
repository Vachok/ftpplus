package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;

import java.io.File;


/**
 @see DBRemoteDownloader
 @since 08.09.2019 (17:46) */
public class DBRemoteDownloaderTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DBRemoteDownloader.class.getSimpleName(), System.nanoTime());
    
    private DBRemoteDownloader dbRemoteDownloader = new DBRemoteDownloader(100);
    
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
        dbRemoteDownloader.setDbToSync(ConstantsFor.DBBASENAME_U0466446_VELKOM + ".pcuserauto");
        String data = dbRemoteDownloader.syncData();
        Assert.assertEquals(data, "u0466446_velkom.pcuserauto.table");
    }
    
    @Test
    public void testToString() {
        String s = dbRemoteDownloader.toString();
        Assert.assertTrue(s.contains("DBRemoteDownloader{"), s);
        Assert.assertTrue(s.contains("lastLocalId=100"), s);
    }
    
    @Test
    public void testWriteJSON() {
        File jsonFile = new File(dbRemoteDownloader.getDbToSync() + FileNames.EXT_TABLE);
        if (jsonFile.exists()) {
            Assert.assertTrue(jsonFile.delete());
        }
    
        String writeJSONRes = dbRemoteDownloader.syncData();
        Assert.assertTrue(jsonFile.exists());
        jsonFile.deleteOnExit();
    }
}