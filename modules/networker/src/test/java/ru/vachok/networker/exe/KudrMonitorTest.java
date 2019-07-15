// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe;


import org.testng.annotations.Test;
import ru.vachok.networker.abstr.monitors.AbstractNetworkerFactory;
import ru.vachok.networker.abstr.monitors.NetNetworkerFactory;
import ru.vachok.networker.net.scanner.KudrNetworker;


/**
 @see KudrNetworker
 @since 12.07.2019 (0:46) */
public class KudrMonitorTest {
    
    
    @Test
    public void kudrMonitorTest() {
        NetNetworkerFactory kudrMon = AbstractNetworkerFactory.createNetMonitorFactory("kudr");
        kudrMon.setLaunchTimeOut(100500);
        kudrMon.getMonitoringRunnable();
        System.out.println(kudrMon.toString());
    }
    
}