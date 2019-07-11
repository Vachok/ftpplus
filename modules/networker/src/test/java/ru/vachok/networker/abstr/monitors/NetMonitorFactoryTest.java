// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import org.testng.Assert;
import org.testng.annotations.Test;


/**
 @see NetMonitorFactory
 @since 7/11/2019 (9:13 PM) */
public class NetMonitorFactoryTest {
    
    
    @Test
    public void createFac() {
        NetMonitor monDo213 = NetMonitorFactory.createDo213Monitor("10.200.213.85");
        String showStr = "monDo213.toString() = " + monDo213.toString();
        
        Assert.assertTrue(showStr.contains("timeout launcher sec = 237,\ntimeIn = 0,\nelapsedMillis = 0,"), showStr);
    }
}