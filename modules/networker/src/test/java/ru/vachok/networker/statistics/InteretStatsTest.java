// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.exe.schedule.WeekStats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;


@SuppressWarnings("ALL") public class InteretStatsTest {
    
    
    @Test()
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
        String sql = interetStats.getSql();
        Assert.assertTrue(sql.equals(ConstantsFor.SQL_SELECTINETSTATS), sql);
    }
    
    @Test
    public void testSelectFrom() {
        InteretStats interetStats = new InteretStats();
        interetStats.setSql(ConstantsFor.SQL_SELECTINETSTATS);
        interetStats.setFileName("inetstatsIP.csv");
        int selectFromRows = interetStats.selectFrom();
        System.out.println(selectFromRows);
    }
    
    @Test
    public void testDeleteFrom() {
        InteretStats interetStats = new InteretStats();
        int i = interetStats.deleteFrom();
        Assert.assertTrue(i == -1);
    }
    
    @Test
    public void testInsertTo() {
        InteretStats interetStats = new InteretStats();
        try {
            int i = interetStats.insertTo();
        }
        catch (IllegalStateException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    @Test
    public void testUpdateTable() {
        InteretStats interetStats = new InteretStats();
        try {
            int i = interetStats.insertTo();
        }
        catch (IllegalStateException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
}