// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.statistics.Stats;


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
        String pcStats = Stats.getPCStats().getInfo();
        Assert.assertTrue(pcStats.contains("total pc:"), pcStats);
    }
    
    /**
     @see WeekStats#getInetStats()
     */
    @Test
    public void testGetInetStats() {
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.INET_STATS);
        String inetStats = ((Stats) informationFactory).getInetStats().getInfo();
        Assert.assertTrue(inetStats.contains("Bytes in stream"), inetStats);
    }
}