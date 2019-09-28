package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 @see SyncInDBStatistics
 @since 08.09.2019 (14:56) */
public class SyncInDBStatisticsTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(SyncInDBStatistics.class.getSimpleName(), System
        .nanoTime());
    
    private String aboutWhat = "192.168.13.220";
    
    private DBStatsUploader dbStatsUploader;
    
    private SyncInDBStatistics syncInDBStatistics = new SyncInDBStatistics("");
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 3));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testSyncData() {
        this.aboutWhat = ConstantsFor.TABLE_VELKOMPC;
        File fileJSON = new File(ConstantsFor.DBBASENAME_U0466446_VELKOM + "." + ConstantsFor.TABLE_VELKOMPC + FileNames.EXT_TABLE);
        if (fileJSON.exists()) {
            Assert.assertTrue(fileJSON.delete());
        }
        Assert.assertFalse(fileJSON.exists());
    
        Runnable r = this::checkWork;
        Future<?> submit = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(r);
        try {
            submit.get(20, TimeUnit.SECONDS);
            Assert.assertTrue(fileJSON.exists());
            Assert.assertTrue(fileJSON.delete());
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    
    
    }
    
    private void checkWork() {
        SyncData syncData = SyncData.getInstance(SyncData.VELKOMPCSYNC);
        String data = syncData.syncData();
        
        Assert.assertTrue(data.contains(ConstantsFor.DBCOL_STAMP), data);
        Assert.assertTrue(data.contains(ConstantsFor.DBCOL_SQUIDANS), data);
        Assert.assertTrue(data.contains("bytes"), data);
        Assert.assertTrue(data.contains(ConstantsFor.DBCOL_TIMESPEND), data);
        Assert.assertTrue(data.contains("site"), data);
        Assert.assertTrue(data.contains("fromFileToJSON"), data);

    }
    
    @Test
    @Ignore
    public void testSyncIPTable() {
        File file192 = new File("192.168.13.220.table");
        checkWork();
        Assert.assertTrue(file192.exists());
        Assert.assertTrue(file192.delete());
    }
    
    @Test
    public void testToString() {
        String toString = syncInDBStatistics.toString();
        Assert.assertTrue(toString.contains("SyncInetStatistics{"), toString);
    }
    
    private void parseQueue(@NotNull String[] valuesArr) {
        Assert.assertTrue(valuesArr.length == 5, new TForms().fromArray(valuesArr));
        dbStatsUploader.setOption(aboutWhat);
        dbStatsUploader.setOption(Arrays.asList(valuesArr));
    }
}