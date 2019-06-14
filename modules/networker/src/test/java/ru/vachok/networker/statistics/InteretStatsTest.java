// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.testng.annotations.Test;
import ru.vachok.networker.exe.schedule.WeekStats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;


@SuppressWarnings("ALL") public class InteretStatsTest {
    
    
    @Test(enabled = false)
    public void testInetStat() {
        StatsOfNetAndUsers statsOfNetAndUsers = new WeekStats();
        String inetStats = statsOfNetAndUsers.getInetStats();
        assertFalse(inetStats.contains("does not exists!"), inetStats);
    }
    
    @Test
    public void dayOfWeekTesting() {
        DateFormat format = new SimpleDateFormat("E");
        String weekDay = format.format(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2)));
        System.out.println(weekDay);
    }
    
    @Test
    public void testRun() {
        InteretStats interetStats = new InteretStats();
        interetStats.run();
    }
    
    @Test
    public void testSelectFrom() {
    }
    
    @Test
    public void testDeleteFrom() {
    }
    
    @Test
    public void testInsertTo() {
    }
    
    @Test
    public void testUpdateTable() {
    }
}