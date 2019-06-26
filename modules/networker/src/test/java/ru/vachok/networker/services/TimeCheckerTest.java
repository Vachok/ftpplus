// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.apache.commons.net.ntp.TimeInfo;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TestConfigure;

import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 @since 15.06.2019 (15:49) */
public class TimeCheckerTest {
    
    
    private final TestConfigure testConfigure = new TestConfigure(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigure.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigure.afterClass();
    }
    
    
    @Test
    public void testCall() {
        TimeInfo timeInfoCall = new TimeChecker().call();
        timeInfoCall.computeDetails();
        Assert.assertTrue(timeInfoCall.getReturnTime() < System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(60), new Date(timeInfoCall.getReturnTime()).toString());
    }
}