// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 @see AppInfoOnLoad
 @since 09.06.2019 (20:49) */
public class AppInfoOnLoadTest {


    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());

    private final AppInfoOnLoad appInfoOnLoad = (AppInfoOnLoad) AppInfoOnLoad.getI();

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
    public void testRun() {
        appInfoOnLoad.run();
        File miniLog = new File("AppInfoOnLoad.mini");
        Assert.assertTrue(miniLog.exists());
        Assert.assertTrue(miniLog.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(30)), new Date(miniLog.lastModified()).toString());
    }

    @Test
    public void testGetMiniLogger() {
        List<String> miniLogger = AppInfoOnLoad.getMiniLogger();
        Assert.assertTrue(miniLogger.isEmpty(), new TForms().fromArray(miniLogger));
    }

    @Test
    public void testTestToString() {
        String toStr = appInfoOnLoad.toString();
        Assert.assertTrue(toStr.contains(UsefulUtilities.thisPC()));
    }
}