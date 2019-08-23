// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.ssh.Tracerouting;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.List;
import java.util.concurrent.*;

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
    public void realRun() {
        AppInfoOnLoad load = new AppInfoOnLoad();
        Future<?> submit = Executors.newSingleThreadExecutor().submit(load);
        try {
            Assert.assertNull(submit.get(60, TimeUnit.SECONDS));
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertTrue(load.toString().contains(UsefulUtilities.thisPC()));
    }
    
    @Test
    public void testGetIISLogSize() {
        String logsSize = UsefulUtilities.getIISLogSize();
        Assert.assertTrue(logsSize.contains("MB IIS Logs"), logsSize);
    }
    
    @Test
    public void testGetBuildStamp() {
        long stampBuild = UsefulUtilities.getBuildStamp();
        long currentTimeMS = System.currentTimeMillis();
        Assert.assertTrue((currentTimeMS >= stampBuild), "\n\n" + (currentTimeMS - stampBuild) + " MS diff between build and test\n\n\n");
    }
    
    /**
     @see AppInfoOnLoad#run()
     */
    @Test(enabled = false)
    public void testRun() {
        AppInfoOnLoad.getMiniLogger().clear();
        Runnable apOnLoad = new AppInfoOnLoad();
        apOnLoad.run();
        List<String> loggerAppInfo = AppInfoOnLoad.getMiniLogger();
        Assert.assertNotNull(loggerAppInfo);
        Assert.assertTrue(loggerAppInfo.size() >= 4, loggerAppInfo.size() + " is loggerAppInfo.size()");
        File commonOwn = new File(FileNames.FILENAME_COMMONOWN);
        Path absPathToCopyCommonOwn = Paths.get(commonOwn.toPath().toAbsolutePath().normalize().toString()
            .replace(commonOwn.getName(), "lan" + System.getProperty(PropertiesNames.PRSYS_SEPARATOR) + commonOwn.getName())).toAbsolutePath().normalize();
    }
    
    @Test
    public void testToString1() {
        System.out.println(new AppInfoOnLoad().toString());
    }
    
    @Test
    public void renewInet() {
        SaveLogsToDB informationFactory = new SaveLogsToDB();
        Future<Object> submit = Executors.newSingleThreadExecutor().submit(informationFactory);
        String infoAbout = "";
        try {
            infoAbout = (String) submit.get(60, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            infoAbout = e.getMessage();
        }
        informationFactory.writeLog(this.getClass().getSimpleName() + ".log", infoAbout);
        System.out.println("infoAbout = " + infoAbout);
    }
    
    @Test(enabled = false)
    public void testKudrMonitor() {
        new AppInfoOnLoad().kudrMonitoring();
    }
    
    @Test
    public void onePCMonStart() {
        boolean isAfter830 = LocalTime.parse("08:30").toSecondOfDay() < LocalTime.now().toSecondOfDay();
        boolean isBefore1730 = LocalTime.now().toSecondOfDay() < LocalTime.parse("17:30").toSecondOfDay();
        boolean isWeekEnds = (LocalDate.now().getDayOfWeek().equals(SUNDAY) || LocalDate.now().getDayOfWeek().equals(DayOfWeek.SATURDAY));
        if (UsefulUtilities.thisPC().toLowerCase().contains("do0213")) {
            Assert.assertTrue(!isWeekEnds && isAfter830 && isBefore1730);
        }
        else {
            Assert.assertFalse(!isWeekEnds && isAfter830 && isBefore1730);
        }
    }
    
    @Test
    public void providerSet() {
        try {
            NetKeeper.setCurrentProvider(new Tracerouting().call());
        }
        catch (Exception e) {
            NetKeeper.setCurrentProvider("<br><a href=\"/makeok\">" + e.getMessage() + "</a><br>");
            Thread.currentThread().interrupt();
        }
        String provider = NetKeeper.getCurrentProvider();
        Assert.assertFalse(provider.isEmpty());
    }
    
    private static int getScansDelay() {
        int parseInt = Integer.parseInt(AppComponents.getUserPref().get(PropertiesNames.PR_SCANSINMIN, "111"));
        if (parseInt <= 0) {
            parseInt = 1;
        }
        if (parseInt < 80 | parseInt > 112) {
            parseInt = 85;
        }
        return parseInt;
    }
}