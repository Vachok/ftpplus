package ru.vachok.networker.info;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Queue;


/**
 @see DBStatsUploader
 @since 08.09.2019 (10:16) */
public class DBStatsUploaderTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DBStatsUploader.class.getSimpleName(), System.nanoTime());
    
    private DataConnectTo dataConnectTo;
    
    private DBStatsUploader dbStatsUploader = new DBStatsUploader();
    
    private String aboutWhat;
    
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
    public void testGetInfoAbout() {
        throw new InvokeEmptyMethodException("GetInfoAbout created 08.09.2019 at 10:12");
    }
    
    @Test
    public void testGetInfo() {
        String info = dbStatsUploader.getInfo();
        System.out.println("info = " + info);
    }
    
    @Test
    public void testTestToString() {
        String toStr = dbStatsUploader.toString();
        Assert.assertTrue(toStr.contains("datasource=jdbc:mysql://srv-inetstat.eatmeat.ru:3306/"), toStr);
    }
    
    @Test
    public void testCreateUploadStatTable() {
        this.aboutWhat = "10_200_204_86";
        dbStatsUploader.setClassOption(aboutWhat);
        int i = dbStatsUploader.createUploadStatTable("ALTER TABLE `10_10_10_10`\n" +
            "  MODIFY `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '';");
        Assert.assertTrue(i == 0);
    }
    
    @Test
    public void testUploadToTable() {
        this.aboutWhat = "10_200_204_86";
        Path rootPath = Paths.get(".");
        String inetStatsPath = rootPath.toAbsolutePath().normalize()
            .toString() + ConstantsFor.FILESYSTEM_SEPARATOR + "inetstats" + ConstantsFor.FILESYSTEM_SEPARATOR + aboutWhat + ".csv";
        Queue<String> statAbout = FileSystemWorker.readFileToQueue(Paths.get(inetStatsPath));
        Assert.assertTrue(statAbout.size() > 0);
        while (!statAbout.isEmpty()) {
            String entryStat = statAbout.poll();
            if (entryStat.isEmpty()) {
                continue;
            }
            String[] valuesArr = entryStat.split(",");
            System.out.println("new TForms().fromArray(valuesArr) = " + new TForms().fromArray(valuesArr));
            Assert.assertTrue(valuesArr.length == 5);
            dbStatsUploader.uploadToTable(valuesArr);
        }
    }
}