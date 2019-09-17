package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.util.Map;


/**
 @see VelkomPCSync
 @since 15.09.2019 (10:22) */
public class VelkomPCSyncTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(VelkomPCSyncTest.class.getSimpleName(), System.nanoTime());
    
    private VelkomPCSync velkomPCSync;
    
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
    public void initRun() {
        this.velkomPCSync = new VelkomPCSync();
    }
    
    @Test
    public void testSyncData() {
        String s = velkomPCSync.syncData();
        Assert.assertTrue(s.contains("DBUploadUniversal["), s);
    }
    
    @Test
    public void testUploadFileTo() {
        try {
            int i = velkomPCSync.uploadFileTo(FileSystemWorker.readFileToList("build.gradle"), "test.test");
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testToString() {
        String s = velkomPCSync.toString();
        Assert.assertTrue(s.contains("VelkomPCSync["), s);
    }
    
    @Test
    public void testGetDbToSync() {
        String dbToSync = velkomPCSync.getDbToSync();
        Assert.assertEquals(dbToSync, "u0466446_velkom.velkompc");
    }
    
    @Test
    public void testSetDbToSync() {
        try {
            velkomPCSync.setDbToSync("");
            Assert.fail();
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testMakeColumns() {
        Map<String, String> map = velkomPCSync.makeColumns();
        String colMapStr = new TForms().fromArray(map);
        Assert.assertEquals(colMapStr, "whenQueried :  TIMESTAMP on update CURRENT_TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP\n" +
                "pcName : VARCHAR(20) NOT NULL DEFAULT 'no data'\n" +
                "userName : VARCHAR(45) NOT NULL DEFAULT 'no data'\n" +
                "lastmod : enum('DO0213', 'HOME', 'rups00')\n");
    }
    
    /**
     @see VelkomPCSync#superRun()
     */
    @Test
    public void testSuperRun() {
        velkomPCSync.superRun();
    }
}