// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe;


import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @see ThreadConfig */
public class ThreadConfigTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private ThreadConfig threadConfig = AppComponents.threadConfig();
    
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
     @see ThreadConfig#dumpToFile(String)
     */
    @Test
    public void testDumpToFile() {
        ThreadConfig threadConfig = ThreadConfig.getI();
        String methName = "testDumpToFile";
        String dumpToFileString = ThreadConfig.dumpToFile(methName);
        Assert.assertEquals(dumpToFileString.getBytes(), new File("DUMPED: thr_" + methName + "-stack.txt").getName().getBytes());
        Assert.assertTrue(new File(dumpToFileString.replace("DUMPED: ", "")).lastModified() > System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10));
    }
    
    /**
     @see ThreadConfig#thrNameSet(String)
     */
    @Test
    public void testThrNameSet() {
        ThreadConfig threadConfig = ThreadConfig.getI();
        String thrNewName = ThreadConfig.thrNameSet("test");
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
        ThreadPoolTaskExecutor taskExecutor = threadConfig.getTaskExecutor();
        Assert.assertEquals(taskExecutor.getThreadNamePrefix(), "E-");
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
        Assert.assertTrue(AppComponents.threadConfig().toString().contains("ThreadConfig{"), AppComponents.threadConfig().toString());
    }
    
    @Test
    public void testKillAll() {
        AppComponents.threadConfig().killAll();
        try (ConfigurableApplicationContext context = IntoApplication.getConfigurableApplicationContext()) {
            Assert.assertFalse(context.isRunning());
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
}