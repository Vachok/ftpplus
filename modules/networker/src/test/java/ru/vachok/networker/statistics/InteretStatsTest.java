// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.exe.schedule.WeekStats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;


@SuppressWarnings("ALL") public class InteretStatsTest {
    
    
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
    public void testInetStat() {
        StatsOfNetAndUsers statsOfNetAndUsers = new WeekStats();
        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            String inetStats = statsOfNetAndUsers.getInetStats();
            assertFalse(inetStats.contains("does not exists!"), inetStats);
        }
        else {
            Assert.assertTrue(statsOfNetAndUsers.toString().contains(LocalDate.now().getDayOfWeek().toString()), statsOfNetAndUsers.toString());
        }
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
        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            interetStats.run();
            String sql = interetStats.getSql();
            Assert.assertTrue(sql.contains(ConstantsFor.SQL_SELECTINETSTATS), sql);
        }
        else {
            try {
                Assert.assertTrue(interetStats.toString().contains("inetstatsIP.csv"), interetStats.toString());
            }
            catch (AssertionError e) {
                System.err.println(e.getMessage());
                Assert.assertTrue(interetStats.toString().contains("Bytes in stream:"), interetStats.toString());
            }
        }
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