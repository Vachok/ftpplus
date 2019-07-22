// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe;


import org.testng.annotations.Test;
import ru.vachok.networker.AbstractNetworkerFactory;
import ru.vachok.networker.abstr.monitors.NetFactory;
import ru.vachok.networker.net.scanner.Kudr;


/**
 @see Kudr
 @since 12.07.2019 (0:46) */
public class KudrMonitorTest {
    
    
    @Test
    public void kudrMonitorTest() {
        NetFactory kudrMon = (NetFactory) AbstractNetworkerFactory.getInstance(NetFactory.class.getTypeName());
        kudrMon.getMonitoringRunnable();
        System.out.println(kudrMon.toString());
    }
    
}