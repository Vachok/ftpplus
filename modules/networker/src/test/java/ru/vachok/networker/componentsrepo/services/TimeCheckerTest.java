// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.services;


import org.apache.commons.net.ntp.TimeInfo;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 @since 15.06.2019 (15:49) */
public class TimeCheckerTest {
    
    
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
    
    
    @Test
    public void testCall() {
        TimeInfo timeInfoCall = new TimeChecker().call();
        timeInfoCall.computeDetails();
        Assert.assertTrue(timeInfoCall.getReturnTime() < System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(60), new Date(timeInfoCall.getReturnTime()).toString());
    }
}