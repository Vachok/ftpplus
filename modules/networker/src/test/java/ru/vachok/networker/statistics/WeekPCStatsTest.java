// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.exe.schedule.WeekStats;


public class WeekPCStatsTest {
    
    
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
    public void getStatsTest() {
        StatsOfNetAndUsers statsOfNetAndUsers = new WeekStats();
        System.out.println(statsOfNetAndUsers.getPCStats());
    }
}