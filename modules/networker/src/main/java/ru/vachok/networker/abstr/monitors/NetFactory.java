// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import ru.vachok.networker.AbstractNetworkerFactory;
import ru.vachok.networker.net.scanner.Kudr;


/**
 @see ru.vachok.networker.abstr.monitors.NetMonitorFactoryTest
 @since 7/11/2019 (5:13 PM) */
public abstract class NetFactory extends AbstractNetworkerFactory {
    
    
    
    public static NetMonitor createOnePCMonitor(String hostAddr) {
        return new Kudr();
    }
    
    public abstract void setLaunchTimeOut(int i);
}
