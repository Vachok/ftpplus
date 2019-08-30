// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.prefs.*;


/**
 @see PreferencesHelper
 @since 06.08.2019 (23:24) */
public class PreferencesHelperTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private Preferences userRoot = Preferences.userRoot();
    
    private Preferences networker = Preferences.userRoot().node("networker");
    
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
    public void getPref() {
        String networkerPrefString = new TForms().fromArray(networker);
        String userRootPrefString = new TForms().fromArray(userRoot);
        Assert.assertTrue(networkerPrefString.contains("USER PREFS"), networkerPrefString);
        Assert.assertNotEquals(userRootPrefString, networker);
        System.out.println("networkerPref = " + networkerPrefString);
        System.out.println("userRoot = " + userRootPrefString);
    }
    
    @Test
    public void setPref() {
        networker.put("test", String.valueOf(LocalTime.now()));
        networker.put("buildTime", String.valueOf(LocalTime.now()));
        try {
            networker.flush();
            networker.sync();
        }
        catch (BackingStoreException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        String networkerPrefString = new TForms().fromArray(networker);
        System.out.println("networkerPrefString = " + networkerPrefString);
    }
    
    @Test
    public void setFromXML() {
        try {
            networker.clear();
            networker.flush();
            networker.sync();
        }
        catch (BackingStoreException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        try {
            Preferences.importPreferences(new FileInputStream("networker.prefer"));
        }
        catch (IOException | InvalidPreferencesFormatException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        String networkerPrefString = new TForms().fromArray(networker);
        Assert.assertFalse(networkerPrefString.isEmpty());
        System.out.println("networkerPrefString = " + networkerPrefString);
    }
    
    @Test
    public void testReal() {
        Preferences fromRealClass = new PreferencesHelper().getPref();
        System.out.println("new TForms().fromArray(freomRealClass) = " + new TForms().fromArray(fromRealClass));
        String fileWorkerValue = fromRealClass.get("charset", "");
        Assert.assertEquals(fileWorkerValue, "UTF-8");
    }
    
    @Test(enabled = false)
    public void clearUserRoot() {
        try {
            userRoot.clear();
            userRoot.sync();
            userRoot = Preferences.userRoot();
            System.out.println("new TForms().fromArray(userRoot) = " + new TForms().fromArray(userRoot));
        }
        catch (BackingStoreException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testTestGetPref() {
        Preferences pref = new PreferencesHelper().getPref();
        System.out.println("new TForms().fromArray(pref) = " + new TForms().fromArray(pref));
    }
}