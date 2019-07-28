// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.net.scanner.Kudr;


public class NetMonitorFactoryTest {
    
    
    @Test
    public void createFac() {
        NetScanService netScanService = new Kudr();
        String showStr = "monDo213.toString() = " + netScanService.toString();
        Assert.assertTrue(showStr.contains("monitoringCycleDelayInSeconds=24"), showStr);
    }
}