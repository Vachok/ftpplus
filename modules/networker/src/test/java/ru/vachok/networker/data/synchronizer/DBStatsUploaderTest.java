package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 @see DBStatsUploader
 @since 08.09.2019 (10:16) */
public class DBStatsUploaderTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DBStatsUploader.class.getSimpleName(), System.nanoTime());
    
    private DBStatsUploader dbStatsUploader;
    
    private String aboutWhat = "192.168.14.194";
    
    @BeforeClass
    public void setup() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @BeforeMethod
    public void initClass() {
        this.dbStatsUploader = new DBStatsUploader(aboutWhat);
    }
    
    @Test
    public void testTestToString() {
        String toStr = dbStatsUploader.toString();
        Assert.assertTrue(toStr.contains("DBStatsUploader{"), toStr);
    }
    
    @Test
    public void testSyncData() {
        try {
            dbStatsUploader.setOption(aboutWhat);
            Future<String> submit = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(dbStatsUploader::syncData);
            submit.get(16, TimeUnit.SECONDS);
        }
        catch (IllegalArgumentException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        dbStatsUploader.setOption(aboutWhat);
        String toStr = dbStatsUploader.toString();
        Assert.assertTrue(toStr.contains("DBStatsUploader{"), toStr);
        Assert.assertTrue(toStr.contains("databaseTable='192.168.14.194'"), toStr);
    }
    
    @Test
    public void testUploadFileTo() {
        Path rootP = Paths.get(".").toAbsolutePath().normalize();
        String pathStr = getClass().getResource("/10.10.35.30.csv").getFile();
        this.dbStatsUploader = new DBStatsUploader("10.10.35.30");
        try {
            int i = dbStatsUploader.uploadCollection(FileSystemWorker.readFileToList(pathStr), "inetstats.10_10_35_30");
            System.out.println("i = " + i);
        }
        catch (NullPointerException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testMakeTable() {
        int i = dbStatsUploader.makeTable("inetstats.10_200_212_66");
        if (i == 0) {
            i = FileSystemWorker.countStringsInFile(Paths.get(Paths.get(".").toAbsolutePath().normalize().toString() + "\\inetstats\\10.200.212.66.csv"));
        }
        Assert.assertTrue(i > 0);
    }
    
    @Test
    public void testMakeColumns() {
        Map<String, String> map = dbStatsUploader.makeColumns();
        String colMapStr = new TForms().fromArray(map);
        Assert.assertEquals(colMapStr, "squidans : VARCHAR(20) NOT NULL DEFAULT 'no data'\n" +
                "site : VARCHAR(190) NOT NULL DEFAULT 'no data'\n" +
                "idrec : mediumint(11)\n" +
                "bytes : int(11)\n" +
                "timespend : int(11)\n" +
                "stamp : bigint(13)\n");
    }
    
    @Test
    public void concreteIP() {
        this.dbStatsUploader = new DBStatsUploader("10.10.35.30");
        String s = dbStatsUploader.syncData();
        Assert.assertTrue(s.contains("rows to inetstats.10_10_35_30"), s);
    }
    
    public void testSuperRun() {
        this.dbStatsUploader = new DBStatsUploader();
    }
}