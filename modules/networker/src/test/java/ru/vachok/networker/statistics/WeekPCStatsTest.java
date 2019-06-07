// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.testng.annotations.Test;
import ru.vachok.networker.exe.schedule.WeekStats;


public class WeekPCStatsTest {
    
    
    @Test
    public void getStatsTest() {
        StatsOfNetAndUsers statsOfNetAndUsers = new WeekStats();
        System.out.println(statsOfNetAndUsers.getPCStats());
    }
}