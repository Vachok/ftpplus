package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;

import java.io.File;
import java.nio.file.Paths;


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
    
    @Test
    public void testSuperRun() {
        dbRemoteDownloader.setDbToSync("velkom." + ConstantsFor.TABLE_VELKOMPC);
        try {
            dbRemoteDownloader.superRun();
        }
        catch (Exception e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testUploadCollection() {
        int i = dbRemoteDownloader.uploadCollection(FileSystemWorker.readFileToSet(Paths.get("build.gradle")), "test.test");
        System.out.println("i = " + i);
    }
    
    @Test
    public void testGetDbToSync() {
        throw new InvokeEmptyMethodException("GetDbToSync created 20.09.2019 at 20:54");
    }
    
    @Test
    public void testSetDbToSync() {
        throw new InvokeEmptyMethodException("SetDbToSync created 20.09.2019 at 20:54");
    }
    
    @Test
    public void testMakeColumns() {
        throw new InvokeEmptyMethodException("MakeColumns created 20.09.2019 at 20:54");
    }
}