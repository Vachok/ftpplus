// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe;


import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @see ThreadConfig */
public class ThreadConfigTest {
    
    
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
    
    
    /**
     @see ThreadConfig#dumpToFile()
     */
    @Test
    public void testDumpToFile() {
        ThreadConfig threadConfig = ThreadConfig.getI();
        String dumpToFileString = threadConfig.dumpToFile();
        Assert.assertEquals(dumpToFileString, new File("stack.txt").getAbsolutePath());
        Assert.assertTrue(new File("stack.txt").lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
    }
    
    /**
     @see ThreadConfig#killAll()
     */
    @Test(enabled = false)
    public void testKillAll() {
        ThreadConfig threadConfig = ThreadConfig.getI();
        boolean isKillAll = threadConfig.killAll();
        Assert.assertTrue(isKillAll);
    }
    
    /**
     @see ThreadConfig#thrNameSet(String)
     */
    @Test
    public void testThrNameSet() {
        ThreadConfig threadConfig = ThreadConfig.getI();
        String thrNewName = threadConfig.thrNameSet("test");
        Assert.assertTrue(thrNewName.contains("test"));
        Assert.assertTrue(Thread.currentThread().getName().contains("test"));
    }
    
    /**
     @see ThreadConfig#execByThreadConfig(Runnable)
     */
    @Test
    public void testExecByThreadConfig() {
        ThreadConfig threadConfig = ThreadConfig.getI();
        Runnable runnable = ()->System.out.println("threadConfig = " + threadConfig);
        boolean execByThreadConfig = threadConfig.execByThreadConfig(runnable);
        Assert.assertTrue(execByThreadConfig);
    }
    
    @Test
    public void testGetTaskExecutor() {
    }
    
    @Test
    public void testGetI() {
        ThreadConfig thrConfig = ThreadConfig.getI();
        Assert.assertNotNull(thrConfig);
    }
    
    @Test
    public void testGetTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = AppComponents.threadConfig().getTaskScheduler();
        Assert.assertNotNull(scheduler);
    }
    
    @Test
    public void testToString1() {
        Assert.assertTrue(AppComponents.threadConfig().toString().contains(ConstantsFor.GOOD_NO_LOCKS));
    }
}