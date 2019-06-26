package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @see CommonCleaner
 @since 25.06.2019 (10:28) */
public class CommonCleanerTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private final File infoAboutOldCommon = new File("files_2.5_years_old_25mb.csv");
    
    private final long epochSecondOfStart = LocalDateTime.of(2019, 6, 25, 11, 45, 00).toEpochSecond(ZoneOffset.ofHours(3));
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    /**
     @see CommonCleaner#call()
     */
    @Test(enabled = false)
    public void testCall() {
        infoAboutOldCommon.setLastModified(epochSecondOfStart * 1000);
        System.out.println("Last modified = " + new Date(infoAboutOldCommon.lastModified()));
        CommonCleaner cleaner = new CommonCleaner(infoAboutOldCommon);
        cleaner.call();
    }
    
    private Map<Path, String> fillMapFromFile() {
    
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
    
    private int countLimitOfDeleteFiles(File fileWithInfoAboutOldCommon) {
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