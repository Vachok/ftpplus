// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.*;


/**
 @see ArchivesAutoCleaner
 @since 17.06.2019 (10:51) */
@SuppressWarnings("ALL")
public class ArchivesAutoCleanerTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
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
        ArchivesAutoCleaner autoCleaner = new ArchivesAutoCleaner(true);
        Assert.assertTrue(autoCleaner.toString().contains("\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives\\14_ИТ_служба\\Общая"));
        Future<?> submit = Executors.newSingleThreadExecutor().submit(autoCleaner);
        try {
            submit.get(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        Assert.assertTrue(cleanLog.exists());
    }
}