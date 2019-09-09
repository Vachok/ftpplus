package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.database.DataConnectTo;


/**
 @see DBStatsUploader
 @since 08.09.2019 (10:16) */
public class DBStatsUploaderTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DBStatsUploader.class.getSimpleName(), System.nanoTime());
    
    private DataConnectTo dataConnectTo;
    
    private DBStatsUploader dbStatsUploader = new DBStatsUploader();
    
    private String aboutWhat = "10.200.210.64";
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        this.dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testGetInfo() {
        throw new InvokeEmptyMethodException("09.09.2019 (10:15)");
    }
    
    @Test
    public void testTestToString() {
        String toStr = dbStatsUploader.toString();
        Assert.assertTrue(toStr.contains("datasource=jdbc:mysql://srv-inetstat.eatmeat.ru:3306/"), toStr);
    }
}