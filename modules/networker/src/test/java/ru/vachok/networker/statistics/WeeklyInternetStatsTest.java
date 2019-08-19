// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.info.InformationFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;


@SuppressWarnings("ALL")
public class WeeklyInternetStatsTest {
    
    
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
        InformationFactory stats = Stats.getInetStats();
        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            String inetStats = stats.getInfo();
            assertFalse(inetStats.contains("does not exists!"), inetStats);
        }
        else {
            Assert.assertTrue(stats.toString().contains(LocalDate.now().getDayOfWeek().toString()), stats.toString());
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
        WeeklyInternetStats weeklyInternetStats = new WeeklyInternetStats();
        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
    
            weeklyInternetStats.run();
    
            String sql = weeklyInternetStats.getSql();
            Assert.assertTrue(sql.contains(ConstantsFor.SQL_SELECTINETSTATS), sql);
        }
        else {
            try {
                Assert.assertTrue(weeklyInternetStats.toString().contains(FileNames.FILENAME_INETSTATSIPCSV), weeklyInternetStats.toString());
            }
            catch (AssertionError e) {
                System.err.println(e.getMessage());
                Assert.assertTrue(weeklyInternetStats.toString().contains("Bytes in stream:"), weeklyInternetStats.toString());
            }
        }
    }
    
    @Test
    public void testSelectFrom() {
        WeeklyInternetStats weeklyInternetStats = new WeeklyInternetStats();
        weeklyInternetStats.setSql();
        weeklyInternetStats.setFileName(FileNames.FILENAME_INETSTATSIPCSV);
        int selectFromRows = weeklyInternetStats.selectFrom();
        System.out.println(selectFromRows);
    }
    
    @Test
    public void testDeleteFrom() {
        WeeklyInternetStats weeklyInternetStats = new WeeklyInternetStats();
        long i = weeklyInternetStats.deleteFrom();
        Assert.assertTrue(i == -1);
    }
}