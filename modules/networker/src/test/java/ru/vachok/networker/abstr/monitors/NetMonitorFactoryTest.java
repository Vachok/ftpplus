// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import org.testng.Assert;
import org.testng.annotations.Test;


/**
 @see NetFactory
 @since 7/11/2019 (9:13 PM) */
public class NetMonitorFactoryTest {
    
    
    @Test
    public void createFac() {
        NetMonitor monDo213 = NetFactory.createOnePCMonitor("10.200.213.85");
        String showStr = "monDo213.toString() = " + monDo213.toString();
        Assert.assertTrue(showStr.contains("monitoringCycleDelayInSeconds=24"), showStr);
    }
}