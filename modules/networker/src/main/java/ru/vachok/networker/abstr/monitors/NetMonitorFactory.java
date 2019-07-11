package ru.vachok.networker.abstr.monitors;


import ru.vachok.networker.exe.schedule.Do0213Monitor;


public abstract class NetMonitorFactory extends AbstractMonitorFactory {
    
    
    public static NetMonitor createDo213Monitor(String hostAddr) {
        return new Do0213Monitor(hostAddr);
    }
}
