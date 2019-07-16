package ru.vachok.networker.abstr.monitors;


import java.rmi.Remote;


/**
 @since 16.07.2019 (13:03) */
public interface RunningStatistics extends Remote {
    
    
    String getCPU();
    
    String getMemory();
    
    String getRuntime();
    
}
