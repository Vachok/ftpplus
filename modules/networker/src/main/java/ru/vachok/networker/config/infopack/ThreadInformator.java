package ru.vachok.networker.config.infopack;


import ru.vachok.networker.net.InfoWorker;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;


/**
 @since 07.05.2019 (12:36) */
public class ThreadInformator implements InfoWorker {
    
    
    @Override public String getInfoAbout() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        return "null";
    }
    
    @Override public void setInfo() {
    
    }
}
