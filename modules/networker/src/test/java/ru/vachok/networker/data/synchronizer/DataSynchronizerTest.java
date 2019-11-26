package ru.vachok.networker.data.synchronizer;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


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
        System.out.println("syncDataResult = " + syncDataResult);
    }
    
    @Test
    public void testSuperRun() {
        dataSynchronizer.superRun();
    }
    
    @Test
    public void testUploadCollection() {
        throw new InvokeEmptyMethodException("UploadCollection created 26.11.2019 at 21:49");
    }
    
    @Test
    public void testMakeColumns() {
        throw new InvokeEmptyMethodException("MakeColumns created 26.11.2019 at 21:49");
    }
}