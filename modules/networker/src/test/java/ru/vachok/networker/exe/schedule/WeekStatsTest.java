// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see WeekStats
 @since 20.06.2019 (10:09) */
@SuppressWarnings("ALL") public class WeekStatsTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
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
}