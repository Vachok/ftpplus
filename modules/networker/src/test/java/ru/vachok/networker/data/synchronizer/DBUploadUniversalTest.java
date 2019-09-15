package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


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
        this.dbUploadUniversal = new DBUploadUniversal(FileSystemWorker.readFileToList("build.gradle"), "test.test");
    }
    
    @Test
    public void testSyncData() {
        String s = dbUploadUniversal.syncData();
        Assert.assertEquals(s, "0 rows uploaded to test.test");
    }
    
    @Test
    public void testUploadFileTo() {
        int exec = dbUploadUniversal.uploadFileTo(FileSystemWorker.readFileToList("build.gradle"), "test.test");
        Assert.assertEquals(exec, 6);
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