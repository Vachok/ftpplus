// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.info.InformationFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


@SuppressWarnings("ALL")
public class WeeklyInternetStatsTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private InformationFactory stats = InformationFactory.getInstance(InformationFactory.STATS_WEEKLYINET);
    
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
        try {
            weeklyInternetStats.run();
        }
        catch (InvokeIllegalException e) {
            if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
            else {
                Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
        }
    }
    
    @Test
    public void testSelectFrom() {
        ((WeeklyInternetStats) stats).setSql();
        ((WeeklyInternetStats) stats).setFileName(FileNames.FILENAME_INETSTATSIPCSV);
        String userSites = ((WeeklyInternetStats) stats).writeLog("10.200.213.103", "15");
        Assert.assertTrue(userSites.contains(".csv"));
        File statFile = new File(userSites.split(" file")[0]);
        Queue<String> csvStats = FileSystemWorker.readFileToQueue(statFile.toPath());
        assertTrue(csvStats.size() == 15);
        statFile.deleteOnExit();
    }
    
    @Test
    public void testDeleteFrom() {
        long i = ((WeeklyInternetStats) stats).deleteFrom("10.200.213.103", "3");
        Assert.assertTrue(i == 3, i + " rows deleted for 10.200.213.103");
    }
}