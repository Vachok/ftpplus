package ru.vachok.networker.statistics;


import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;


public class InteretStatsTest {
    
    
    @Test
    public void testInetStat() {
        StatsOfNetAndUsers statsOfNetAndUsers = new WeekStats();
        String inetStats = statsOfNetAndUsers.getInetStats();
        assertFalse(inetStats.contains("does not exists!"), inetStats);
    }
    
    
}