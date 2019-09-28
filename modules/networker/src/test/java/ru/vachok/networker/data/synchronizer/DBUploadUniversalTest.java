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
import ru.vachok.networker.data.enums.FileNames;

import java.nio.file.Paths;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 @see DBUploadUniversal
 @since 15.09.2019 (13:14) */
public class DBUploadUniversalTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DBUploadUniversalTest.class.getSimpleName(), System
        .nanoTime());
    
    private DBUploadUniversal dbUploadUniversal;
    
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
    public void initTest() {
        this.dbUploadUniversal = new DBUploadUniversal(FileSystemWorker.readFileToList(FileNames.BUILD_GRADLE), "test.test");
    }
    
    @Test
    public void testSyncData() {
        Future<String> sF = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(()->dbUploadUniversal.syncData());
        String s = "";
        try {
            s = sF.get(30, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertEquals(s, "0 rows uploaded to test.test");
    }
    
    @Test
    public void testUploadFileTo() {
        Deque<String> fileToQueue = new LinkedList<>();
        FileSystemWorker.readFileToQueue(Paths.get(FileNames.BUILD_GRADLE).toAbsolutePath().normalize()).stream().forEach(fileToQueue::addFirst);
        Assert.assertTrue(fileToQueue.size() > 0);
        dbUploadUniversal.setOption(fileToQueue);
        dbUploadUniversal.setDbToSync("test.test");
        String syncData = dbUploadUniversal.syncData();
    }
    
    @Test
    public void testMakeColumns() {
        try {
            dbUploadUniversal.makeColumns();
            Assert.fail();
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testSuperRun() {
        try {
            dbUploadUniversal.superRun();
            Assert.fail();
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testToString() {
        String s = dbUploadUniversal.toString();
        Assert.assertTrue(s.contains("DBUploadUniversal["), s);
    }
}