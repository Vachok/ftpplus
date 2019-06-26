// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TestConfigure;
import ru.vachok.networker.exe.schedule.WeekStats;


public class WeekPCStatsTest {
    
    
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
    
    
    @Test
    public void getStatsTest() {
        StatsOfNetAndUsers statsOfNetAndUsers = new WeekStats();
        System.out.println(statsOfNetAndUsers.getPCStats());
    }
}