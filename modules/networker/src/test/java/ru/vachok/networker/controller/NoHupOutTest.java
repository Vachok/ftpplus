package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see NoHupOut
 @since 09.08.2019 (13:08) */
public class NoHupOutTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NoHupOut.class.getSimpleName(), System
        .nanoTime());
    
    private NoHupOut noHupOut = new NoHupOut();
    
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
    public void testNoHupGet() {
        try {
            ExtendedModelMap modelMap = new ExtendedModelMap();
            String hupGet = noHupOut.noHupGet(modelMap, new MockHttpServletRequest(), new MockHttpServletResponse());
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testTestToString() {
        String toStr = noHupOut.toString();
        Assert.assertTrue(toStr.contains("NoHupOut{"), toStr);
    }
}