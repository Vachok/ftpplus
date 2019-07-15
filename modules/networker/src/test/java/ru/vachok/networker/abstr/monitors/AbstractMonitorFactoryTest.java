// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import org.testng.annotations.Test;


/**
 @since 11.07.2019 (16:00) */
public class AbstractMonitorFactoryTest {
    
    
    private static final String MONITOR_PARAMETER = "10.200.214.80";
    
    @Test
    public void getPing() {
        NetMonitorFactory monitorFactory = AbstractMonitorFactory.createNetMonitorFactory(MONITOR_PARAMETER);
        monitorFactory.launchMonitoring();
    }
    
    @Test
    public void testCheckParameter() {
        System.out.println(AbstractMonitorFactory.checkParameter(MONITOR_PARAMETER));
    }
}