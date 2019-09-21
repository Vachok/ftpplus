package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


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
            int i = velkomPCSync.uploadCollection(FileSystemWorker.readFileToList("build.gradle"), "test.test");
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
        Assert.assertEquals(dbToSync, ConstantsFor.DBBASENAME_U0466446_VELKOM + "." + ConstantsFor.TABLE_VELKOMPC);
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
        Future<?> submit = AppComponents.threadConfig().getTaskExecutor().submit(()->velkomPCSync.superRun());
        final int localIDPCUserAuto = velkomPCSync.getLastLocalID(ConstantsFor.DB_VELKOMPCUSERAUTO);
        
        try {
            submit.get(30, TimeUnit.SECONDS);
            Assert.assertTrue(velkomPCSync.getLastLocalID(ConstantsFor.DB_VELKOMPCUSERAUTO) > localIDPCUserAuto, MessageFormat
                .format("{0} has error in superRun!", SyncData.getInstance("").toString()));
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}