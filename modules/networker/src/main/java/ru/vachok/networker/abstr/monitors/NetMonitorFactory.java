// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import ru.vachok.networker.exe.schedule.Do0213Monitor;


/**
 @see ru.vachok.networker.abstr.monitors.NetMonitorFactoryTest
 @since 7/11/2019 (5:13 PM) */
public abstract class NetMonitorFactory extends AbstractMonitorFactory {
    
    
    public static NetMonitor createDo213Monitor(String hostAddr) {
        return new Do0213Monitor(hostAddr);
    }
    
    public abstract void setLaunchTimeOut(int i);
}
