// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe;


import org.testng.annotations.Test;
import ru.vachok.networker.abstr.monitors.AbstractMonitorFactory;
import ru.vachok.networker.abstr.monitors.NetMonitorFactory;


/**
 @see KudrMonitor
 @since 12.07.2019 (0:46) */
public class KudrMonitorTest {
    
    
    @Test
    public void kudrMonitorTest() {
        NetMonitorFactory kudrMon = AbstractMonitorFactory.createNetMonitorFactory("kudr");
        kudrMon.setLaunchTimeOut(100500);
        kudrMon.launchMonitoring();
        System.out.println(kudrMon.toString());
    }
    
}