// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.FileNames;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;


/**
 @see Cleaner
 @since 25.06.2019 (10:28) */
@Ignore
public class CleanerTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private final File infoAboutOldCommon = new File(FileNames.FILES_OLD);
    
    private final long epochSecondOfStart = LocalDateTime.of(2019, 6, 25, 11, 45, 0).toEpochSecond(ZoneOffset.ofHours(3));
    
    private Cleaner cleaner = new Cleaner();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    /**
     @see Cleaner#call()
     */
    @Test
    @Ignore
    public void testCall() {
        Future<String> stringFuture = AppComponents.threadConfig().getTaskExecutor().submit(()->cleaner.call());
        try {
            String cleanStr = stringFuture.get(10, TimeUnit.SECONDS);
            Assert.assertEquals(cleanStr, "Cleaner complete: false");
        }
        catch (InvokeIllegalException | ExecutionException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    public void testBlockCall() {
        cleaner.call();
    }
    
    @Test
    public void testTestToString() {
        Assert.assertTrue(cleaner.toString().contains("Cleaner{"), cleaner.toString());
    }
    
    private @NotNull Map<Path, String> fillMapFromFile() {
    
        Map<Path, String> filesToDeleteWithAttrs = new HashMap<>();
        int limitOfDeleteFiles = countLimitOfDeleteFiles(infoAboutOldCommon);
        List<String> fileAsList = FileSystemWorker.readFileToList(infoAboutOldCommon.toPath().toAbsolutePath().normalize().toString());
        Random random = new Random();
        
        for (int i = 0; i < limitOfDeleteFiles; i++) {
            String deleteFileAsString = fileAsList.get(random.nextInt(fileAsList.size()));
            try {
                String[] pathAndAttrs = deleteFileAsString.split(", ,");
                filesToDeleteWithAttrs.putIfAbsent(Paths.get(pathAndAttrs[0]), pathAndAttrs[1]);
            }
            catch (IndexOutOfBoundsException | NullPointerException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
            }
        }
        return filesToDeleteWithAttrs;
    }
    
    private int countLimitOfDeleteFiles(@NotNull File fileWithInfoAboutOldCommon) {
        int stringsInLogFile = FileSystemWorker.countStringsInFile(fileWithInfoAboutOldCommon.toPath().toAbsolutePath().normalize());
        long lastModified = fileWithInfoAboutOldCommon.lastModified();
        
        if (System.currentTimeMillis() < lastModified + TimeUnit.DAYS.toMillis(1)) {
            System.out.println(stringsInLogFile = (stringsInLogFile / 100) * 10);
        }
        else if (System.currentTimeMillis() < lastModified + TimeUnit.DAYS.toMillis(2)) {
            System.out.println(stringsInLogFile = (stringsInLogFile / 100) * 25);
        }
        else if (System.currentTimeMillis() < lastModified + TimeUnit.DAYS.toMillis(3)) {
            System.out.println(stringsInLogFile = (stringsInLogFile / 100) * 75);
        }
        else {
            System.out.println(stringsInLogFile);
        }
        
        return stringsInLogFile;
    }
}