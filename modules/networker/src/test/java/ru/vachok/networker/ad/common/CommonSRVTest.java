// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSearcher;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.FileNames;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.testng.Assert.assertTrue;


/**
 @see CommonSRV
 @since 17.06.2019 (15:02) */
public class CommonSRVTest {


    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());

    private CommonSRV commSrv;

    private final ThreadPoolTaskExecutor threadConfig = AppComponents.threadConfig().getTaskExecutor();

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

    @Test
    public void testSearchByPat() {
        String searchInCommonResult = new CommonSRV().searchByPat("График отпусков:14_ИТ_служба\\Общая");
        String searchInCommonResult1 = new CommonSRV().searchByPat(":");
        assertTrue(searchInCommonResult.contains("written: true"), searchInCommonResult);
        assertTrue(searchInCommonResult.contains(FileNames.SEARCH_LAST), searchInCommonResult);
        assertTrue(searchInCommonResult1
                .contains("\"stamp\":\"1572132524328\",\"upstring\":\"\\\\\\\\srv-fs.eatmeat.ru\\\\common_new\\\\14_ит_служба\\\\общая\\\\Инструкции\\\\Бицерба\\\\IMG_20181010_103105.jpg\""), searchInCommonResult1);
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
        Path startPath = Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\_AdminTools\\__TCPU65\\Programm\\");
        int threadsCount = Runtime.getRuntime().availableProcessors() - 2;
        List<String> dirs = new ArrayList<>(18);
        File[] searchFolders = startPath.toFile().listFiles();
        Assert.assertNotNull(searchFolders);
        for (int i = 0; i < 18; i++) {
            File searchFolder = searchFolders[i];
            if (searchFolder.isDirectory()) {
                dirs.add(searchFolder.getAbsolutePath());
            }
        }
        int dirsSize = dirs.size();
        Assert.assertTrue(dirsSize > 0);

        long startTime = System.nanoTime();

        ExecutorService stealingPool = Executors.newWorkStealingPool(threadsCount);
        List<Callable<Set<String>>> fjList = new ArrayList<>();
        for (String dir : dirs) {
            Callable<Set<String>> callSet = new FileSearcher(".txt", Paths.get(dir));
            fjList.add(callSet);
        }
        try {
            stealingPool.invokeAll(fjList);
        }
        catch (InterruptedException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        long stopTime = System.nanoTime();
        String execServiceStr = new FileSearcher().getSearchResultsFromDB();
        System.out.println("execServiceStr = " + execServiceStr);
        long differenceNANOs = stopTime - startTime;
        System.out.println(MessageFormat.format("Time difference = {0} NANOs", differenceNANOs));
    }

    @Test
    public void testGetLastSearchResultFromDB() {
        String resultFromDB = new FileSearcher(".txt").getSearchResultsFromDB();
        Assert.assertFalse(resultFromDB.isEmpty());
        Assert.assertTrue(resultFromDB.contains("Searching for: .docx"), resultFromDB);
    }
}