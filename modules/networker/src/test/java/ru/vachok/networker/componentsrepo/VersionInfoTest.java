// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.sysinfo.VersionInfo;


/**
 @see ru.vachok.networker.sysinfo.VersionInfo
 @since 15.06.2019 (14:00) */
@SuppressWarnings("ALL") public class VersionInfoTest {
    
    
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
    
    
    /**
     @see ru.vachok.networker.sysinfo.VersionInfo#setParams()
     */
    @Test(enabled = false)
    public void testSetParams() {
        throw new InvokeEmptyMethodException("16.07.2019 (14:51)");
    }
    
    /**
     @see ru.vachok.networker.sysinfo.VersionInfo#getParams()
     */
    @Test(enabled = false)
    public void getParamsTEST() {
        ru.vachok.networker.sysinfo.VersionInfo infoVers = new VersionInfo(AppComponents.getProps(), UsefulUtilities.thisPC());
        Assert.assertFalse(infoVers.getAppBuild().isEmpty());
        Assert.assertFalse(infoVers.getBuildTime().isEmpty());
        Assert.assertFalse(infoVers.getAppVersion().isEmpty());
    }
}