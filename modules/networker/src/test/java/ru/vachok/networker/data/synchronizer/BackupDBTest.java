package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.info.NetScanService;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 @see BackupDB
 @since 17.10.2019 (21:18) */
public class BackupDBTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(BackupDBTest.class.getSimpleName(), System.nanoTime());
    
    private BackupDB backupDB;
    
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
    public void initBkpDB() {
        if (!NetScanService.isReach(OtherKnownDevices.IP_SRVMYSQL_HOME)) {
            throw new InvokeIllegalException(OtherKnownDevices.SRV_INETSTAT + " not started");
        }
        else {
            this.backupDB = new BackupDB();
        }
    }
    
    @Test
    public void testSyncData() {
        try {
            String syncRes = backupDB.syncData();
            System.out.println("AbstractForms.fromArray(map) = " + syncRes);
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testSuperRun() {
        Future<?> superRunFuture = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(()->backupDB.superRun());
        try {
            superRunFuture.get(35, TimeUnit.SECONDS);
            backupDB.setDbToSync("velkom.pcuserauto");
            superRunFuture = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(()->backupDB.superRun());
            superRunFuture.get(35, TimeUnit.SECONDS);
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
    
    @Test
    public void testUploadCollection() {
        int rowsUp = backupDB.uploadCollection(Collections.emptyList(), "test.test");
    }
    
    @Test
    public void testMakeColumns() {
        try {
            Map<String, String> map = backupDB.makeColumns();
            System.out.println("AbstractForms.fromArray(map) = " + AbstractForms.fromArray(map));
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testTestToString() {
        String toS = backupDB.toString();
        Assert.assertEquals(toS, "BackupDB[\n" +
            "dbToSync = 'velkom.velkompc',\n" +
            "option = 0\n" +
            "]");
    }
}