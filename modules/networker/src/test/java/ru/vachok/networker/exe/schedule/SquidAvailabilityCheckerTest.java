// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @since 20.06.2019 (9:54) */
@SuppressWarnings("ALL") public class SquidAvailabilityCheckerTest {
    
    
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
    public void testRun() {
        SquidAvailabilityChecker squidAvailabilityChecker = new SquidAvailabilityChecker();
        File squidAvailabilityCheckerLog = new File("SquidAvailabilityChecker.log");
        try {
            if (ConstantsFor.thisPC().contains("do0213")) {
                squidAvailabilityChecker.run();
                Assert.assertTrue(squidAvailabilityCheckerLog.lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1), squidAvailabilityCheckerLog.getAbsolutePath());
            }
            else {
                System.out.println("ConstantsFor.thisPC() = " + ConstantsFor.thisPC());
                Assert.assertFalse(squidAvailabilityCheckerLog.exists());
            }
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        testConfigureThreadsLogMaker.getPrintStream().println(new TForms().fromArray(FileSystemWorker.readFileToList("SquidAvailabilityChecker.log"), false));
    }
    
    @Test
    public void testCall() {
        SquidAvailabilityChecker squidAvailabilityChecker = new SquidAvailabilityChecker();
        try {
            if (ConstantsFor.thisPC().contains("do0213")) {
                String call = squidAvailabilityChecker.call();
                Assert.assertTrue(call.contains("/usr/local/etc/squid/squid.conf (squid)"));
            }
            else {
                System.out.println("ConstantsFor.thisPC() = " + ConstantsFor.thisPC());
            }
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
}