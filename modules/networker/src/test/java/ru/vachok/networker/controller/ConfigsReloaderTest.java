package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ModelAttributeNames;


/**
 @see ConfigsReloader
 @since 09.08.2019 (13:13) */
public class ConfigsReloaderTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(ConfigsReloader.class.getSimpleName(), System
        .nanoTime());
    
    private ConfigsReloader configsReloader = new ConfigsReloader();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test(invocationCount = 2)
    @Ignore
    public void testMakeOk() {
        ExtendedModelMap modelMap = new ExtendedModelMap();
        String makeOk = configsReloader.makeOk(modelMap, new MockHttpServletRequest());
        Assert.assertEquals(makeOk, "ok");
        Assert.assertTrue(modelMap.asMap().size() == 3, modelMap.asMap().size() + " modelMap.asMap().size()");
    
        String titleAtt = modelMap.asMap().get("title").toString();
        Assert.assertTrue(titleAtt.contains("192.168.13."), titleAtt);
        String okAtt = modelMap.asMap().get("ok").toString();
        Assert.assertTrue(okAtt.contains("sudo"), okAtt);
        String footerAtt = modelMap.asMap().get(ModelAttributeNames.FOOTER).toString();
        Assert.assertFalse(footerAtt.isEmpty(), footerAtt);
    }
    
    @Test
    public void testTestToString() {
        String toStr = configsReloader.toString();
        Assert.assertTrue(toStr.contains("OKMaker{"), toStr);
    }
}