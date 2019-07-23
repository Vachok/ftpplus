// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe;


import org.testng.annotations.Test;
import ru.vachok.networker.abstr.monitors.NetScanService;
import ru.vachok.networker.net.scanner.Kudr;


/**
 @see Kudr
 @since 12.07.2019 (0:46) */
public class KudrMonitorTest {
    
    
    @Test
    public void kudrMonitorTest() {
        NetScanService netScanService = new Kudr();
        netScanService.getMonitoringRunnable();
        String pingerServiceStatistics = netScanService.getStatistics();
        System.out.println("pingerServiceStatistics = " + pingerServiceStatistics);
    }
    
}