// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Properties;


/**
 @since 09.06.2019 (20:49) */
public class AppInfoOnLoadTest {
    
    
    @Test
    public void testGetThisDelay() {
        System.out.println(getScansDelay());
    }
    
    @Test
    public void testGetIISLogSize() {
    }
    
    @Test
    public void testGetBuildStamp() {
        try {
            long stampBuild = AppInfoOnLoad.getBuildStamp();
            Assert.assertTrue(System.currentTimeMillis() > stampBuild);
            System.out.println("stampBuild = " + stampBuild);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test(enabled = false)
    public void testRun() {
        Runnable apOnLoad = new AppInfoOnLoad();
        apOnLoad.run();
        Assert.assertNotNull(AppInfoOnLoad.MINI_LOGGER);
    }
    
    @Test
    public void testToString1() {
    }
    
    @Test
    public void testDateSchedulers() {
    }
    
    private static int getScansDelay() {
        final Properties APP_PROPS = AppComponents.getProps();
        
        int parseInt = Integer.parseInt(APP_PROPS.getProperty(ConstantsFor.PR_SCANSINMIN, "111"));
        if (parseInt <= 0) {
            parseInt = 1;
        }
        if (parseInt < 80 | parseInt > 112) {
            parseInt = 85;
        }
        return parseInt;
    }
}