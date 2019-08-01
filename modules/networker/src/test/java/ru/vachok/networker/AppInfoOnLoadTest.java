// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static java.time.DayOfWeek.SUNDAY;


/**
 @see AppInfoOnLoad
 @since 09.06.2019 (20:49) */
public class AppInfoOnLoadTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
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
    public void testGetThisDelay() {
        int scansDelayOnline = getScansDelay();
        Assert.assertFalse(scansDelayOnline == 0);
        Assert.assertTrue(scansDelayOnline > 80, String.valueOf(scansDelayOnline));
        Assert.assertTrue(scansDelayOnline < 112, String.valueOf(scansDelayOnline));
    }
    
    @Test
    public void testGetIISLogSize() {
        String logsSize = AppInfoOnLoad.getIISLogSize();
        Assert.assertTrue(logsSize.contains("MB IIS Logs"), logsSize);
    }
    
    @Test
    public void testGetBuildStamp() {
        long stampBuild = AppInfoOnLoad.getBuildStamp();
        long currentTimeMS = System.currentTimeMillis();
        Assert.assertTrue((currentTimeMS >= stampBuild), "\n\n" + (currentTimeMS - stampBuild) + " MS diff between build and test\n\n\n");
    }
    
    /**
     @see AppInfoOnLoad#run()
     */
    @Test(enabled = false)
    public void testRun() {
        AppInfoOnLoad.MINI_LOGGER.clear();
        Runnable apOnLoad = new AppInfoOnLoad();
        apOnLoad.run();
        List<String> loggerAppInfo = AppInfoOnLoad.MINI_LOGGER;
        Assert.assertNotNull(loggerAppInfo);
        Assert.assertTrue(loggerAppInfo.size() >= 4, loggerAppInfo.size() + " is loggerAppInfo.size()");
        File commonOwn = new File(ConstantsFor.FILENAME_COMMONOWN);
        Path absPathToCopyCommonOwn = Paths.get(commonOwn.toPath().toAbsolutePath().normalize().toString()
            .replace(commonOwn.getName(), "lan" + System.getProperty(ConstantsFor.PRSYS_SEPARATOR) + commonOwn.getName())).toAbsolutePath().normalize();
    }
    
    @Test
    public void testToString1() {
        System.out.println(new AppInfoOnLoad().toString());
    }
    
    @Test
    public void realRun() {
        AppInfoOnLoad load = new AppInfoOnLoad();
        load.run();
        Assert.assertTrue(load.toString().contains(ConstantsFor.thisPC()));
    }
    
    @Test(enabled = false)
    public void testKudrMonitor() {
        AppInfoOnLoad.kudrMonitoring();
    }
    
    @Test
    public void onePCMonStart() {
        boolean isAfter830 = LocalTime.parse("08:30").toSecondOfDay() < LocalTime.now().toSecondOfDay();
        boolean isBefore1730 = LocalTime.now().toSecondOfDay() < LocalTime.parse("17:30").toSecondOfDay();
        boolean isWeekEnds = (LocalDate.now().getDayOfWeek().equals(SUNDAY) || LocalDate.now().getDayOfWeek().equals(DayOfWeek.SATURDAY));
        if (ConstantsFor.thisPC().toLowerCase().contains("do0213")) {
            Assert.assertTrue(!isWeekEnds && isAfter830 && isBefore1730);
        }
        else {
            Assert.assertFalse(!isWeekEnds && isAfter830 && isBefore1730);
        }
    }
    
    private static int getScansDelay() {
        int parseInt = Integer.parseInt(AppComponents.getUserPref().get(ConstantsFor.PR_SCANSINMIN, "111"));
        if (parseInt <= 0) {
            parseInt = 1;
        }
        if (parseInt < 80 | parseInt > 112) {
            parseInt = 85;
        }
        return parseInt;
    }
}