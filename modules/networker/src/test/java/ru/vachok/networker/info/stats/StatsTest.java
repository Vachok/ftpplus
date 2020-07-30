package ru.vachok.networker.info.stats;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;


/**
 @see Stats
 @since 25.08.2019 (17:00) */
public class StatsTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(StatsTest.class.getSimpleName(), System.nanoTime());

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }

    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }

    @Test
    public void testGetInstance() {
        String toStr = Stats.getInstance(InformationFactory.STATS_WEEKLY_INTERNET).toString();
        Assert.assertTrue(toStr.contains("\"class\":\"WeeklyInternetStats\","), toStr);

        toStr = Stats.getInstance(InformationFactory.STATS_SUDNAY_PC_SORT).toString();
        Assert.assertTrue(toStr.contains("ComputerUserResolvedStats["), toStr);
    }

    @Test
    public void testIsSunday() {
        boolean isSun = Stats.isSunday();
        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            Assert.assertTrue(isSun);
        }
        else {
            Assert.assertFalse(isSun);
        }
    }
}