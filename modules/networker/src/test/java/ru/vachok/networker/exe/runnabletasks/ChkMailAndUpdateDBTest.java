package ru.vachok.networker.exe.runnabletasks;


import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;


/**
 @since 17.06.2019 (9:05) */
public class ChkMailAndUpdateDBTest {
    
    
    @Test
    public void testRunCheck() {
        new ChkMailAndUpdateDB(new SpeedChecker()).runCheck();
        File chkMailFile = new File("ChkMailAndUpdateDB.chechMail");
        assertTrue(chkMailFile.exists());
        assertTrue(chkMailFile.lastModified() > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3));
    }
}