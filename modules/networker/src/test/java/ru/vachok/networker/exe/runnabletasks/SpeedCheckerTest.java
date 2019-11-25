// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.util.Date;
import java.util.concurrent.*;


/**
 @see SpeedChecker */
public class SpeedCheckerTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private File chkMailFile = new File(FileNames.SPEED_CHECHMAIL);
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
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
    public void testCall() {
        try {
            Future<Long> aLongFuture = Executors.newSingleThreadExecutor().submit(new SpeedChecker());
            long aLong = aLongFuture.get(30, TimeUnit.SECONDS);
            Assert.assertTrue(aLong + TimeUnit.DAYS.toMillis(3) > System.currentTimeMillis(), new Date(aLong).toString());
        }
        catch (ExecutionException | TimeoutException | ArrayIndexOutOfBoundsException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (InterruptedException e) {
            messageToUser.error(e.getMessage());
        }
    }
}