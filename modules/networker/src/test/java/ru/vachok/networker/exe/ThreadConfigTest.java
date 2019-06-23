package ru.vachok.networker.exe;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @see ThreadConfig */
public class ThreadConfigTest {
    
    
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
}