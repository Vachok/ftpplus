// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.testng.Assert.assertTrue;


/**
 @since 17.06.2019 (15:02) */
public class CommonSRVTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private ThreadPoolTaskExecutor threadConfig = AppComponents.threadConfig().getTaskExecutor();
    
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
    public void testSearchByPat() {
        String searchInCommonResult = new CommonSRV().searchByPat("График отпусков:14_ИТ_служба\\Общая");
        String searchInCommonResult1 = new CommonSRV().searchByPat(":");
        assertTrue(searchInCommonResult.contains("График отпусков 2019г  IT.XLSX"), searchInCommonResult);
        assertTrue(searchInCommonResult.contains("График отпусков 2019г. SA.xlsx"), searchInCommonResult);
        assertTrue(searchInCommonResult1.contains("Bytes in stream:"));
    }
    
    @Test
    public void testReStoreDir() {
        File fileWithResult = new File("CommonSRV.reStoreDir.results.txt");
        try {
            Files.deleteIfExists(fileWithResult.toPath());
            Assert.assertFalse(fileWithResult.exists());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        final CommonSRV commSrv = new CommonSRV();
        String reStoreDirResult = commSrv.reStoreDir();
        assertTrue(reStoreDirResult.contains("CommonSRV.reStoreDir.results.txt"), reStoreDirResult);
        commSrv.setPathToRestoreAsStr("\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Общая\\testClean\\testClean0.virus");
        reStoreDirResult = commSrv.reStoreDir();
        System.out.println("reStoreDirResult = " + reStoreDirResult);
        Assert.assertTrue(fileWithResult.exists());
        fileWithResult.deleteOnExit();
    }
}