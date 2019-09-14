package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;


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
        this.dbStatsUploader = new DBStatsUploader();
    }
    
    @Test
    public void testTestToString() {
        String toStr = dbStatsUploader.toString();
        Assert.assertTrue(toStr.contains("syncTable='velkompc'"), toStr);
    }
    
    @Test
    public void testSyncData() {
        try {
            dbStatsUploader.setOption(aboutWhat);
            String syncDt = dbStatsUploader.syncData();
        }
        catch (IllegalArgumentException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        dbStatsUploader.setOption(aboutWhat);
        dbStatsUploader.setOption(new String[]{aboutWhat});
        String toStr = dbStatsUploader.toString();
        Assert.assertTrue(toStr.contains("DBStatsUploader{"), toStr);
        Assert.assertTrue(toStr.contains("syncTable='192.168.14.194'"), toStr);
    }
    
    @Test
    public void testUploadFileTo() {
        Path rootP = Paths.get(".").toAbsolutePath().normalize();
        String pathStr = rootP.toString() + "\\inetstats\\10.200.218.54.csv";
        int i = dbStatsUploader.uploadFileTo(FileSystemWorker.readFileToList(pathStr), "test.10_200_218_54");
        Assert.assertTrue(i > 0);
    }
    
    @Test
    public void testMakeTable() {
        int i = dbStatsUploader.makeTable("10_200_214_128");
        int countStr = FileSystemWorker.countStringsInFile(Paths.get(".\\inetstats\\10.200.214.128.csv"));
        Assert.assertTrue(i > 0);
    }
    
    @Test
    public void testMakeColumns() {
        Map<String, String> map = dbStatsUploader.makeColumns();
        String colMapStr = new TForms().fromArray(map);
        Assert.assertEquals(colMapStr, "squidans : varchar(20)\n" +
            "site : varchar(190)\n" +
            "idrec : mediumint(11)\n" +
            "bytes : int(11)\n" +
            "timespend : int(11)\n" +
            "stamp : bigint(13)\n");
    }
    
    @Test
//    @Ignore
    public void superRun() {
        File[] allStatFiles = new File(".\\inetstats").listFiles();
        Assert.assertNotNull(allStatFiles);
        Assert.assertTrue(allStatFiles.length > 0);
        for (File stat : allStatFiles) {
            dbStatsUploader.setOption(stat.getName().replace(".csv", ""));
            String syncStr = dbStatsUploader.syncData();
            System.out.println("syncStr = " + syncStr);
        }
    }
}