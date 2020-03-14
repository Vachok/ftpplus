// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.usermanagement;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.File;
import java.nio.file.Paths;


/**
 @see OwnerFixer
 @since 29.07.2019 (13:24) */
public class OwnerFixerTest {


    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 2));
        testConfigureThreadsLogMaker.before();
    }

    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }

    @Test
    @Ignore
    public void testRun() {
        OwnerFixer ownerFixer = new OwnerFixer(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\testClean\\"));
        AppConfigurationLocal.getInstance().execute(ownerFixer, 20);
        File fileOwnerFixLog = new File(OwnerFixer.class.getSimpleName() + ".res");
        Assert.assertTrue(fileOwnerFixLog.exists());
        fileOwnerFixLog.deleteOnExit();
    }

    @Test
    public void testTestToString() {
        OwnerFixer ownerFixer = new OwnerFixer(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\testClean\\"));
        Assert.assertEquals(ownerFixer.toString(), "OwnerFixer{, resultsList=0, startPath=\\\\srv-fs\\it$$\\ХЛАМ\\testClean}");
    }
}