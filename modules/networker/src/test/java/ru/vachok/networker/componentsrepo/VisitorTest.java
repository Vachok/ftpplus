package ru.vachok.networker.componentsrepo;


import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import javax.servlet.http.HttpServletRequest;


/**
 @see Visitor
 @since 21.06.2019 (8:59) */
public class VisitorTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
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
    public void checkVisits() {
        HttpServletRequest request = new MockHttpServletRequest();
        String testRemoteAddr = "10.200.213.85";
        ((MockHttpServletRequest) request).setRemoteAddr(testRemoteAddr);
        Visitor visitor = new AppComponents().visitor(request);
        Assert.assertEquals(visitor.getRemAddr(), testRemoteAddr);
    }
}