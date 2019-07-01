package ru.vachok.networker.exe.runnabletasks;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;


/**
 @since 17.06.2019 (9:05) */
public class ChkMailAndUpdateDBTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    
    @Test
    public void testRunCheck() {
        new ChkMailAndUpdateDB(new SpeedChecker()).runCheck();
        File chkMailFile = new File("ChkMailAndUpdateDB.chechMail");
        assertTrue(chkMailFile.exists());
        assertTrue(chkMailFile.lastModified() > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3));
    }
}