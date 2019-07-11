package ru.vachok.networker.abstr.monitors;


import org.testng.annotations.Test;


/**
 @since 11.07.2019 (16:00) */
public class AbstractMonitorFactoryTest {
    
    
    @Test
    public void getPing() {
        NetMonitorFactory monitorFactory = AbstractMonitorFactory.createNetMonitorFactory("10.200.214.80");
        monitorFactory.launchMonitoring();
    }
}