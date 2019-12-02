// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.systray;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.awt.*;
import java.util.concurrent.*;


/**
 @see ActionDelTMP
 @since 30.07.2019 (10:57) */
@Ignore
public class ActionDelTMPTest {


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

    @Test
    public void testActionPerformed() {
        ActionDelTMP actionDelTMP = new ActionDelTMP(Executors.newSingleThreadExecutor(), 5, new MenuItem(), new PopupMenu());
        try {
            Assert.assertTrue(actionDelTMP.getTimeOutSec() == 5);
            Future<?> submit = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(()->actionDelTMP.actionPerformed(null));
            submit.get(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }

    @Test
    public void testTestToString() {
        String toStr = new ActionDelTMP(Executors.newSingleThreadExecutor(), 5, new MenuItem(), new PopupMenu()).toString();
        Assert.assertTrue(toStr.contains("ActionDelTMP{executor=java.util.concurrent.Executors"), toStr);
    }
}