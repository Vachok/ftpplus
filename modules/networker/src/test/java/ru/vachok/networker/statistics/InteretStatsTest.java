// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.testng.annotations.Test;
import ru.vachok.networker.exe.schedule.WeekStats;

import static org.testng.Assert.assertFalse;


public class InteretStatsTest {
    
    
    @Test(enabled = false)
    public void testInetStat() {
        StatsOfNetAndUsers statsOfNetAndUsers = new WeekStats();
        String inetStats = statsOfNetAndUsers.getInetStats();
        assertFalse(inetStats.contains("does not exists!"), inetStats);
    }
    
    
}