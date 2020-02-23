// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 @see SpeedChecker */
public class SpeedCheckerTest {


    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());

    public static final String SPEED_CHECHMAIL = "Speed.chechMail";

    private File chkMailFile = new File(SPEED_CHECHMAIL);

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
            Future<Long> aLongFuture = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(new SpeedChecker());
            long aLong = aLongFuture.get(30, TimeUnit.SECONDS);
            Assert.assertTrue(aLong + TimeUnit.DAYS.toMillis(3) > System.currentTimeMillis(), new Date(aLong).toString());
        }
        catch (ExecutionException | TimeoutException | ArrayIndexOutOfBoundsException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        catch (InterruptedException e) {
            messageToUser.error(e.getMessage());
        }
    }
}