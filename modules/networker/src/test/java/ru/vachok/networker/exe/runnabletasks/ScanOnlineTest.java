// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.exe.schedule.DiapazonScan;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 Сканирование только тех, что он-лайн
 <p>
 
 @see DiapazonScan
 @since 26.01.2019 (11:18) */
public class ScanOnlineTest {
    
    
    @Test
    public void runAway() {
        ScanOnline scanOnline = new ScanOnline();
        scanOnline.run();
        File fileScan = new File(ConstantsFor.FILENAME_ONSCAN);
        Assert.assertTrue(fileScan.exists(), fileScan.getAbsolutePath());
        Assert.assertTrue(fileScan.lastModified() > TimeUnit.SECONDS.toMillis(5));
    }
}