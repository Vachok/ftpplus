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


@SuppressWarnings("ALL") public class InternetStatsTest {
    
    
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
        InternetStats internetStats = new InternetStats();
        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            internetStats.run();
            String sql = internetStats.getSql();
            Assert.assertTrue(sql.contains(ConstantsFor.SQL_SELECTINETSTATS), sql);
        }
        else {
            try {
                Assert.assertTrue(internetStats.toString().contains(ConstantsFor.FILENAME_INETSTATSIPCSV), internetStats.toString());
            }
            catch (AssertionError e) {
                System.err.println(e.getMessage());
                Assert.assertTrue(internetStats.toString().contains("Bytes in stream:"), internetStats.toString());
            }
        }
    }
    
    @Test
    public void testSelectFrom() {
        InternetStats internetStats = new InternetStats();
        internetStats.setSql(ConstantsFor.SQL_SELECTINETSTATS);
        internetStats.setFileName(ConstantsFor.FILENAME_INETSTATSIPCSV);
        int selectFromRows = internetStats.selectFrom();
        System.out.println(selectFromRows);
    }
    
    @Test
    public void testDeleteFrom() {
        InternetStats internetStats = new InternetStats();
        int i = internetStats.deleteFrom();
        Assert.assertTrue(i == -1);
    }
    
    @Test
    public void testInsertTo() {
        InternetStats internetStats = new InternetStats();
        try {
            int i = internetStats.insertTo();
        }
        catch (IllegalStateException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    @Test
    public void testUpdateTable() {
        InternetStats internetStats = new InternetStats();
        try {
            int i = internetStats.insertTo();
        }
        catch (IllegalStateException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
}