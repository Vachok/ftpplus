// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


public interface NetMonitor {
    
    
    Runnable getMonitoringRunnable();
    
    String getStatistics();
    
}
