package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.util.Collections;


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
        String syncDataResult = dataSynchronizer.syncData();
        Assert.assertTrue(syncDataResult.contains("SELECT * FROM velkom.velkompc WHERE idrec >"));
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
    public void testTestToString() {
        String toStr = dataSynchronizer.toString();
        Assert.assertEquals(toStr, "DataSynchronizer[\n" +
            "dbToSync = 'velkom.velkompc',\n" +
            "columnName = 'idrec',\n" +
            "dataConnectTo = MySqlLocalSRVInetStat{\"tableName\":\"velkom\",\"dbName\":\"velkom\"},\n" +
            "colNames = {},\n" +
            "columnsNum = 0\n" +
            "]");
    }
}