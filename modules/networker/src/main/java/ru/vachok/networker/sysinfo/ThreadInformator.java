package ru.vachok.networker.sysinfo;


import ru.vachok.networker.AppComponents;
import ru.vachok.networker.net.InfoWorker;

import java.awt.*;


/**
 @since 07.05.2019 (12:36) */
public class ThreadInformator implements InfoWorker, SystemInformator {
    
    
    @Override public String getThreadExecutorsInfo() {
        return AppComponents.threadConfig().toString();
    }
    
    @Override public String getAppInfo() {
        return AppComponents.versionInfo().toString();
    }
    
    @Override public String getInfoAbout() {
        throw new IllegalComponentStateException("08.05.2019 (16:35)");
/*
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        Objects.requireNonNull(deadlockedThreads, "No DEAD locks");
        final StringBuilder stringBuilder = new StringBuilder();
        if ((deadlockedThreads!=null)&&deadlockedThreads.length > 0) {
            for (long l : deadlockedThreads) {
                ThreadInfo threadInfo = threadMXBean.getThreadInfo(l);
                stringBuilder.append(threadInfo.getLockName()).append("\n");
                stringBuilder.append(threadInfo.getLockInfo());
            }
        }
        else {
            for (long threadId : threadMXBean.getAllThreadIds()) {
                stringBuilder.append("ThreadID: ").append(threadId).append(". Info: ");
                stringBuilder.append(threadMXBean.getThreadInfo(threadId)).append("<br>");
                stringBuilder.append("CPU time: ").append(TimeUnit.NANOSECONDS.toMillis(threadMXBean.getThreadCpuTime(threadId))).append(" millisec, ");
                stringBuilder.append("User time: ").append(TimeUnit.NANOSECONDS.toMillis(threadMXBean.getThreadUserTime(threadId))).append(" millisec, ");
            }
            ;
        }
        return stringBuilder.toString();
*/
    }
    
    @Override public void setInfo() {
        throw new IllegalComponentStateException("08.05.2019 (16:11)");
    }
}
