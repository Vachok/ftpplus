// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe;


import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;


/**
 @see ThreadConfig */
public class ThreadConfigTest {


    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());

    private final ThreadConfig threadConfig = AppComponents.threadConfig();

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
        File file = new File(this.getClass().getSimpleName());
        ThreadConfig threadConfig = ThreadConfig.getI();
        Runnable runnable = ()->FileSystemWorker.writeFile(file.getAbsolutePath(), threadConfig.toString());
        boolean b = threadConfig.execByThreadConfig(runnable);
        Assert.assertTrue(b);
        try {
            Thread.sleep(400);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        finally {
            Assert.assertTrue(file.exists());
            Assert.assertTrue(file.delete());
        }
    }

    @Test
    public void testGetTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = threadConfig.getTaskExecutor();
        Assert.assertEquals(taskExecutor.getThreadNamePrefix(), "E_");
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
}