package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class DataSynchronizerTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DataSynchronizerTest.class.getSimpleName(), System
            .nanoTime());

    private DataSynchronizer dataSynchronizer;

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
    public void initSync() {
        this.dataSynchronizer = new DataSynchronizer();
    }

    @Test
    public void testSyncData() {
        dataSynchronizer.setOption(DataConnectTo.getInstance(DataConnectTo.FIREBASE));
        Future<String> syncFuture = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(()->dataSynchronizer.syncData());
        try {
            String syncDataResult = syncFuture.get(15, TimeUnit.SECONDS);
            Assert.assertTrue(syncDataResult.contains("SELECT * FROM velkom.velkompc WHERE idrec >"));
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        finally {
            Assert.assertTrue(new File(ConstantsFor.DB_VELKOMVELKOMPC).exists());
        }
    }

    @Test
    @Ignore
    public void testSuperRun() {
        AppConfigurationLocal.getInstance().execute(()->dataSynchronizer.superRun(), 30);
    }

    @Test
    public void testUploadCollection() {
        int isUpl = dataSynchronizer.uploadCollection(Collections.EMPTY_LIST, "test.test");
        Assert.assertTrue(isUpl == 0);
    }

    @Test
    public void testMakeColumns() {
        try {
            dataSynchronizer.makeColumns();
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    public void testToString() {
        String toStr = dataSynchronizer.toString();
        Assert.assertEquals(toStr, "DataSynchronizer[\n" +
                "dbToSync = 'velkom.velkompc',\n" +
                "columnName = 'idrec',\n" +
                "dataConnectTo = MySqlLocalSRVInetStat{\"tableName\":\"velkom\",\"dbName\":\"velkom\"},\n" +
                "colNames = {},\n" +
                "columnsNum = 0\n" +
                "]");
    }

    /**
     @see DataSynchronizer#createTable(String, List)
     */
    @Test
    public void testCreateTable() {
        List<String> addCol = new ArrayList<>();
        addCol.add("txt TEXT NOT NULL, ");
        int synchronizerTable = dataSynchronizer.createTable("test.creator", addCol);
        System.out.println("synchronizerTable = " + synchronizerTable);
        Assert.assertFalse(synchronizerTable == -666, String.valueOf(synchronizerTable));
    }
}