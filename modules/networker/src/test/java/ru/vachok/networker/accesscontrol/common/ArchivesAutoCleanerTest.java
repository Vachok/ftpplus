// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


/**
 @since 17.06.2019 (10:51)
 @see ArchivesAutoCleaner
 */
@SuppressWarnings("ALL") public class ArchivesAutoCleanerTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    public void testRun() {
        File cleanLog = new File(ConstantsFor.FILENAME_CLEANERLOGTXT);
        try {
            Files.deleteIfExists(cleanLog.toPath());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        ArchivesAutoCleaner autoCleaner = new ArchivesAutoCleaner("\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives\\14_ИТ_служба\\Общая\\Документооборот");
        Assert.assertTrue(autoCleaner.toString().contains("startFolder = '\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives\\14_ИТ_служба\\Общая\\Документооборот'"));
        autoCleaner.run();
        Assert.assertTrue(cleanLog.exists());
    }
}