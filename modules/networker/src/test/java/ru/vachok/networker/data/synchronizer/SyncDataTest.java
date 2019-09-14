package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see SyncData
 @since 10.09.2019 (12:05) */
public class SyncDataTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(SyncData.class
            .getSimpleName(), System.nanoTime());
    
    private SyncData syncData;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        this.syncData = SyncData.getInstance();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @BeforeMethod
    public void initSync() {
        syncData.setIdColName("idrec");
        syncData.setDbToSync("u0466446_velkom.velkompc");
    }
    
    @Test
    public void testGetInstance() {
        String toString = syncData.toString();
        Assert.assertTrue(toString.contains("SyncInetStatistics{"), toString);
    }
    
    @Test
    public void testGetLastLocalID() {
        int lastLocalID = syncData.getLastLocalID("u0466446_velkom.velkompc");
        Assert.assertTrue(lastLocalID > 0);
    }
    
    @Test
    public void testGetLastRemoteID() {
        int lastRemoteID = syncData.getLastRemoteID("u0466446_velkom.velkompc");
        Assert.assertTrue(lastRemoteID > 0);
    }
    
    @Test
    public void getCustomIDTest() {
        syncData.setDbToSync("u0466446_webapp.ru_vachok_networker");
        syncData.setIdColName("counter");
        int lastRemoteID = syncData.getLastRemoteID("u0466446_velkom.velkompc");
        Assert.assertTrue(lastRemoteID > 0, null + " lastRemoteID");
    }
    
    @Test
    public void testMakeColumns() {
        throw new InvokeEmptyMethodException("MakeColumns created 14.09.2019 at 12:06");
    }
    
    @Test
    public void testGetCreateQuery() {
        throw new InvokeEmptyMethodException("GetCreateQuery created 14.09.2019 at 12:06");
    }
}