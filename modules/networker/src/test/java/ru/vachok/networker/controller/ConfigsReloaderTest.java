package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


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
    
    @Test
    public void testMakeOk() {
        ExtendedModelMap modelMap = new ExtendedModelMap();
        String makeOk = configsReloader.makeOk(modelMap, new MockHttpServletRequest());
        Assert.assertEquals(makeOk, "ok");
        Assert.assertTrue(modelMap.asMap().size() == 3);
        Assert.assertTrue(modelMap.asMap().get("title").toString().contains("192.168.13."));
        Assert.assertTrue(modelMap.asMap().get("ok").toString().contains("sudo"));
        Assert.assertFalse(modelMap.asMap().get("footer").toString().isEmpty());
    }
    
    @Test
    public void testTestToString() {
        String toStr = configsReloader.toString();
        Assert.assertTrue(toStr.contains("OKMaker{"), toStr);
    }
}