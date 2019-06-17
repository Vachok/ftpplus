// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;


/**
 @since 09.06.2019 (20:49) */
public class AppInfoOnLoadTest {
    
    
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
    
    @Test()
    public void testRun() {
        AppInfoOnLoad.MINI_LOGGER.clear();
        Runnable apOnLoad = new AppInfoOnLoad();
        apOnLoad.run();
        List<String> loggerAppInfo = AppInfoOnLoad.MINI_LOGGER;
        Assert.assertNotNull(loggerAppInfo);
        Assert.assertTrue(loggerAppInfo.size() >= 11, loggerAppInfo.size() + " is loggerAppInfo.size()");
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