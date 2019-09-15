// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSearcher;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.concurrent.*;

import static org.testng.Assert.assertTrue;


/**
 @see CommonSRV
 @since 17.06.2019 (15:02) */
public class CommonSRVTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private ThreadPoolTaskExecutor threadConfig = AppComponents.threadConfig().getTaskExecutor();
    
    CommonSRV commSrv;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @BeforeMethod
    public void initObj() {
        this.commSrv = new CommonSRV();
    }
    
    @Test(invocationCount = 3)
    public void testSearchByPat() {
        String searchInCommonResult = new CommonSRV().searchByPat("График отпусков:14_ИТ_служба\\Общая");
        String searchInCommonResult1 = new CommonSRV().searchByPat(":");
        assertTrue(searchInCommonResult.contains("written: true"), searchInCommonResult);
        assertTrue(searchInCommonResult.contains("search.last"), searchInCommonResult);
        assertTrue(searchInCommonResult1.contains("Searching for: График отпусков"));
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
    
    @Test
    public void testToString() {
        final CommonSRV commSrv = new CommonSRV();
        String toStr = commSrv.toString();
        Assert.assertTrue(toStr.contains("CommonSRV["), toStr);
    }
    
    @Test
    public void testSetNullToAllFields() {
        final CommonSRV commSrv = new CommonSRV();
        commSrv.setPathToRestoreAsStr("q");
        Assert.assertTrue(commSrv.toString().contains("q"), commSrv.toString());
        
        commSrv.setNullToAllFields();
        Assert.assertFalse(commSrv.toString().contains("q"), commSrv.toString());
    }
    
    @Test
    public void searchManyThreads() {
        Path startPath = Paths.get("\\\\srv-fs.eatmeat.ru\\it$$");
        int threadsCout = Runtime.getRuntime().availableProcessors() - 2;
        Deque<String> dirs = new ConcurrentLinkedDeque<>();
        File[] searchFolders = startPath.toFile().listFiles();
        Assert.assertNotNull(searchFolders);
        for (File searchFolder : searchFolders) {
            if (searchFolder.isDirectory()) {
                dirs.addFirst(searchFolder.getAbsolutePath());
            }
        }
        int dirsSize = dirs.size();
        Assert.assertTrue(dirsSize > 0);
        ThreadPoolExecutor executor = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor();
        executor.purge();
        for (int i = 0; i < threadsCout; i++) {
            Future<?> submit = executor.submit(new FileSearcher(dirs.removeFirst(), Paths.get("\\\\srv-fs.eatmeat.ru\\common_new")));
            try {
                submit.get(5, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException | TimeoutException e) {
                Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
        }
        Assert.assertEquals(dirs.size(), dirsSize - threadsCout);
    }
    
    @Test
    public void testGetLastSearchResultFromDB() {
        String resultFromDB = commSrv.getLastSearchResultFromDB();
        Assert.assertFalse(resultFromDB.isEmpty());
        Assert.assertTrue(resultFromDB.contains("xls"), resultFromDB);
    }
}