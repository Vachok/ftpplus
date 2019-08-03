// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.util.concurrent.*;

import static org.testng.Assert.assertTrue;


/**
 @since 17.06.2019 (9:05) */
public class ChkMailAndUpdateDBTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
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
    
    
    @Test
    public void testRunCheck() {
        Future<?> submit = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(new ChkMailAndUpdateDB(new SpeedChecker()));
        try {
            submit.get(ConstantsFor.DELAY * 2, TimeUnit.SECONDS);
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        File chkMailFile = new File("ChkMailAndUpdateDB.chechMail");
        assertTrue(chkMailFile.exists());
        assertTrue(chkMailFile.lastModified() > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3));
    }
}