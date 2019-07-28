// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.sysinfo;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see VersionInfo
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
     @see VersionInfo#setParams()
     */
    @Test(enabled = false)
    public void testSetParams() {
        throw new InvokeEmptyMethodException("16.07.2019 (14:51)");
    }
    
    /**
     @see VersionInfo#getParams()
     */
    @Test(enabled = false)
    public void getParamsTEST() {
        VersionInfo infoVers = new VersionInfo(AppComponents.getProps(), ConstantsFor.thisPC());
        Assert.assertFalse(infoVers.getAppBuild().isEmpty());
        Assert.assertFalse(infoVers.getBuildTime().isEmpty());
        Assert.assertFalse(infoVers.getAppVersion().isEmpty());
    }
}