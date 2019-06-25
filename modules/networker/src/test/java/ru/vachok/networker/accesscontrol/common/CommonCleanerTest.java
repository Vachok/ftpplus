package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 @see CommonCleaner
 @since 25.06.2019 (10:28) */
public class CommonCleanerTest {
    
    
    /**
     @see CommonCleaner#call()
     */
    @Test(enabled = false)
    public void testCall() {
        CommonCleaner cleaner = new CommonCleaner(new File("files_2.5_years_old_25mb.csv"));
        cleaner.call();
    }
    
    private Map<Path, String> fillMapFromFile() {
        File fileWithInfoAboutOldCommon = new File("files_2.5_years_old_25mb.csv");
        Map<Path, String> filesToDeleteWithAttrs = new HashMap<>();
        int limitOfDeleteFiles = countLimitOfDeleteFiles(fileWithInfoAboutOldCommon);
        List<String> fileAsList = FileSystemWorker.readFileToList(fileWithInfoAboutOldCommon.toPath().toAbsolutePath().normalize().toString());
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