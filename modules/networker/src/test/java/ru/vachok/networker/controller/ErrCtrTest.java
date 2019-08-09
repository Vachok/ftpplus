package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see ErrCtr
 @since 09.08.2019 (12:39) */
public class ErrCtrTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(ErrCtr.class.getSimpleName(), System.nanoTime());
    
    private ErrCtr errCtr = new ErrCtr();
    
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
    public void testErrHandle() {
        String errHandle = errCtr.errHandle(new MockHttpServletRequest(), new ExtendedModelMap());
        Assert.assertEquals(errHandle, "error");
    }
    
    @Test
    public void testGetErrorPath() {
        String path = errCtr.getErrorPath();
        Assert.assertEquals(path, "/error");
    }
    
    @Test
    public void testTestToString() {
        String toStr = errCtr.toString();
        Assert.assertTrue(toStr.contains("ErrCtr{H_2_CENTER"), toStr);
    }
}