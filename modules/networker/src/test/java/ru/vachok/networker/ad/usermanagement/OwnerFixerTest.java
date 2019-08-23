// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.usermanagement;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;


/**
 @see OwnerFixer
 @since 29.07.2019 (13:24) */
public class OwnerFixerTest {
    
    
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
    public void testRun() {
        OwnerFixer ownerFixer = new OwnerFixer(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\testClean\\"));
        ownerFixer.run();
        File fileOwnerFixLog = new File(OwnerFixer.class.getSimpleName() + ".res");
        Assert.assertTrue(fileOwnerFixLog.exists());
        Assert.assertTrue(fileOwnerFixLog.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10)));
    }
    
    @Test
    public void testTestToString() {
        OwnerFixer ownerFixer = new OwnerFixer(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\testClean\\"));
        Assert.assertEquals(ownerFixer.toString(), "OwnerFixer{, resultsList=0, startPath=\\\\srv-fs\\it$$\\ХЛАМ\\testClean}");
    }
}