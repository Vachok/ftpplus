// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static ru.vachok.networker.net.enums.ConstantsNet.*;


public class DiapazonScanTest {
    
    
    @Test
    public void testRun() {
        DiapazonScan.getInstance().run();
    }
    
    @Test
    public void makeFilesMapTest() {
        Map<String, File> map = copyOfMakeMap();
        String s = new TForms().fromArray(map, false);
        Assert.assertNotNull(s);
        Assert.assertTrue(s.contains("lan_11vsrv.txt"));
    }
    
    private Map<String, File> copyOfMakeMap() {
        Path absolutePath = Paths.get("").toAbsolutePath();
        Map<String, File> scanMap = new ConcurrentHashMap<>();
        try {
            for (File scanFile : Objects.requireNonNull(new File(absolutePath.toString()).listFiles())) {
                if (scanFile.getName().contains("lan_")) {
                    System.out.println("PUT ON INSTANCE MAP\n key: " + scanFile.getName() + "\nval: " + scanFile);
                }
            }
        }
        catch (NullPointerException e) {
            Assert.assertNull(e, e.getMessage());
        }
        
        scanMap.putIfAbsent(FILENAME_NEWLAN220, new File(FILENAME_NEWLAN220));
        scanMap.putIfAbsent(FILENAME_NEWLAN210, new File(FILENAME_NEWLAN210));
        scanMap.putIfAbsent(FILENAME_NEWLAN213, new File(FILENAME_NEWLAN213));
        scanMap.putIfAbsent(FILENAME_OLDLANTXT0, new File(FILENAME_OLDLANTXT0));
        scanMap.putIfAbsent(FILENAME_OLDLANTXT1, new File(FILENAME_OLDLANTXT1));
        scanMap.putIfAbsent(FILENAME_SERVTXT_10SRVTXT, new File(FILENAME_SERVTXT_10SRVTXT));
        scanMap.putIfAbsent(FILENAME_SERVTXT_21SRVTXT, new File(FILENAME_SERVTXT_21SRVTXT));
        scanMap.putIfAbsent(FILENAME_SERVTXT_31SRVTXT, new File(FILENAME_SERVTXT_31SRVTXT));
        return scanMap;
    }
}