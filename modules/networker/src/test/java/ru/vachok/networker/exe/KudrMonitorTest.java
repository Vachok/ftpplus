// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe;


import org.testng.annotations.Test;
import ru.vachok.networker.abstr.monitors.PingerService;
import ru.vachok.networker.net.scanner.Kudr;


/**
 @see Kudr
 @since 12.07.2019 (0:46) */
public class KudrMonitorTest {
    
    
    @Test
    public void kudrMonitorTest() {
        PingerService pingerService = new Kudr();
        pingerService.getMonitoringRunnable();
        String pingerServiceStatistics = pingerService.getStatistics();
        System.out.println("pingerServiceStatistics = " + pingerServiceStatistics);
    }
    
}