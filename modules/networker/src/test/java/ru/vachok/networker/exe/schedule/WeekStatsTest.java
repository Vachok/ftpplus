package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TestConfigure;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 @see WeekStats
 @since 20.06.2019 (10:09) */
@SuppressWarnings("ALL") public class WeekStatsTest {
    
    
    private final TestConfigure testConfigure = new TestConfigure(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigure.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigure.afterClass();
    }
    
    /**
     @see WeekStats#getPCStats()
     */
    @Test
    public void testGetPCStats() {
        WeekStats weekStats = new WeekStats();
        String pcStats = weekStats.getPCStats();
        Assert.assertTrue(pcStats.contains("total pc:"), pcStats);
    }
    
    /**
     @see WeekStats#getInetStats()
     */
    @Test
    public void testGetInetStats() {
        WeekStats weekStats = new WeekStats();
        String inetStats = weekStats.getInetStats();
        Assert.assertTrue(inetStats.contains("Bytes in stream"), inetStats);
    }
    
    /**
     * @see WeekStats#run()
     */
    @Test
    public void testRun() {
        WeekStats weekStats = new WeekStats();
        weekStats.run();
        Assert.assertTrue(new File("inetstatsIP.csv").lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
    }
}