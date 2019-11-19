package ru.vachok.networker.exe;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.net.monitor.DiapazonScan;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.util.concurrent.TimeUnit;


/**
 @see ThreadTimeout
 @since 19.11.2019 (15:46) */
public class ThreadTimeoutTest {
    
    
    NetScanService diapazonScan = DiapazonScan.getInstance();
    
    @Test
    public void testRun() {
        final long startTime = System.currentTimeMillis();
        AppConfigurationLocal.getInstance().execute(diapazonScan, 30);
        Assert.assertTrue(startTime > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(29)));
    }
}