// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.apache.commons.net.ntp.TimeInfo;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 @since 15.06.2019 (15:49) */
public class TimeCheckerTest {
    
    
    @Test
    public void testCall() {
        TimeInfo timeInfoCall = new TimeChecker().call();
        timeInfoCall.computeDetails();
        Assert.assertTrue(timeInfoCall.getReturnTime() < System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(60), new Date(timeInfoCall.getReturnTime()).toString());
    }
}