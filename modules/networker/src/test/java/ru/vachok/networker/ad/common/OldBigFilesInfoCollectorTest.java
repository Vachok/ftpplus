// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.util.concurrent.*;


/**
 @since 17.06.2019 (14:41) */
public class OldBigFilesInfoCollectorTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private final OldBigFilesInfoCollector infoCollector = new OldBigFilesInfoCollector(true);
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testCall() {
        File resultFileCSV = new File(getClass().getSimpleName() + ".csv");
        String startPath = infoCollector.getStartPath();
        Assert.assertEquals(startPath, "\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Общая\\testClean\\");
        Future<String> submit = Executors.newSingleThreadExecutor().submit(infoCollector);
        String callY2K = "null";
        
        try {
            callY2K = submit.get(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    
        if (resultFileCSV.exists()) {
            Assert.assertTrue(callY2K != null && callY2K.contains("Common2Years25MbytesInfoCollectorTest.csv"), callY2K);
            FileSystemWorker.readFile(resultFileCSV.getAbsolutePath());
        }
        else {
            String logAbsolutePathString = FileSystemWorker.writeFile(getClass().getSimpleName() + ".log", callY2K);
            Assert.assertTrue(new File(logAbsolutePathString).exists());
        }
    }
    
    @Test
    public void testGetStartPath() {
        String startPath = infoCollector.getStartPath();
        Assert.assertEquals(startPath, "\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Общая\\testClean\\");
    }
    
    @Test
    public void testGetDate() {
        String collectorDate = infoCollector.getDate();
        Assert.assertNull(collectorDate);
    }
    
    @Test
    public void testSetDate() {
        String date = "29-07-2019";
        infoCollector.setDate(date);
        Assert.assertEquals(infoCollector.getDate(), date);
    }
    
    @Test
    public void testTestToString() {
        Assert.assertTrue(infoCollector.toString().contains("Common2Years25MbytesInfoCollector{"), infoCollector.toString());
    }
}