// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.util.concurrent.*;

import static org.testng.Assert.assertTrue;


/**
 @see ChkMailAndUpdateDB
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
        Future<?> submit = Executors.newSingleThreadExecutor().submit(new ChkMailAndUpdateDB(new SpeedChecker()));
        try {
            Assert.assertTrue(((Long) submit.get(30, TimeUnit.SECONDS)) > 0);
        }
        catch (TimeoutException | ExecutionException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (InterruptedException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        File chkMailFile = new File("ChkMailAndUpdateDB.chechMail");
        assertTrue(chkMailFile.exists());
        assertTrue(chkMailFile.lastModified() > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3));
        Assert.assertTrue(new File(FileNames.SPEED_MAIL).exists());
        Assert.assertTrue(new File(FileNames.SPEED_MAIL).lastModified() > (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)));
    }
}