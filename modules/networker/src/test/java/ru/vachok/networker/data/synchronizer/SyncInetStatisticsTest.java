package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.*;


/**
 @see SyncInetStatistics
 @since 08.09.2019 (14:56) */
public class SyncInetStatisticsTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(SyncInetStatistics.class.getSimpleName(), System
        .nanoTime());
    
    private String aboutWhat = "192.168.13.220";
    
    private DBStatsUploader dbStatsUploader;
    
    private SyncInetStatistics syncInetStatistics = new SyncInetStatistics();
    
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
        this.aboutWhat = ConstantsFor.TABLE_VELKOMPC;
        File fileJSON = new File(ConstantsFor.TABLE_VELKOMPC + ".table");
        if (fileJSON.exists()) {
            Assert.assertTrue(fileJSON.delete());
        }
        Assert.assertFalse(fileJSON.exists());
    
        Runnable r = ()->checkWork(ConstantsFor.TABLE_VELKOMPC);
        Future<?> submit = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(r);
        try {
            submit.get(20, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    
        Assert.assertTrue(fileJSON.exists());
        Assert.assertTrue(fileJSON.delete());
    }
    
    private void checkWork(String opt) {
        SyncData syncData = SyncData.getInstance();
        syncData.setOption(opt);
        String data = syncData.syncData();
        
        if (opt.matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
            Assert.assertTrue(data.contains("stamp"), data);
            Assert.assertTrue(data.contains("squidans"), data);
            Assert.assertTrue(data.contains("bytes"), data);
            Assert.assertTrue(data.contains("timespend"), data);
            Assert.assertTrue(data.contains("site"), data);
        }
        else {
            Assert.assertTrue(data.contains("fromFileToJSON"), data);
        }
    }
    
    @Test
    @Ignore
    public void testSyncIPTable() {
        File file192 = new File("192.168.13.220.table");
        checkWork("192.168.13.220");
        Assert.assertTrue(file192.exists());
        Assert.assertTrue(file192.delete());
    }
    
    @Test
    public void testToString() {
        String toString = syncInetStatistics.toString();
        Assert.assertTrue(toString.contains("SyncInetStatistics{"), toString);
    }
    
    private void parseQueue(@NotNull String[] valuesArr) {
        Assert.assertTrue(valuesArr.length == 5, new TForms().fromArray(valuesArr));
        dbStatsUploader.setOption(aboutWhat);
        dbStatsUploader.setOption(Arrays.asList(valuesArr));
    }
}