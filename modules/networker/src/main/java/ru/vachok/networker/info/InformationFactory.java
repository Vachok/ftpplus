// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.accesscontrol.inetstats.InternetUse;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.statistics.Stats;

import java.lang.management.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @since 09.04.2019 (13:16) */
public interface InformationFactory {
    
    
    ThreadMXBean MX_BEAN_THREAD = ManagementFactory.getThreadMXBean();
    
    String LOCAL = "pcinfo";
    
    String INET_USAGE = "inetusage";
    
    String STATS_PC = "savelogs";
    
    String STATS_WEEKLYINET = "inetstats";
    
    String LOGS_EXT = "ru.vachok.stats.SaveLogsToDB";
    
    String USER = "user";
    
    String getInfoAbout(String aboutWhat);
    
    /**
     @param classOption объект, вспомогательный для класса.
     */
    void setClassOption(Object classOption);
    
    static @NotNull String getOS() {
        StringBuilder stringBuilder = new StringBuilder();
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        
        stringBuilder.append(getTotalCPUTime()).append("\n");
        stringBuilder.append(operatingSystemMXBean.getClass().getTypeName()).append("\n");
        stringBuilder.append(operatingSystemMXBean.getAvailableProcessors()).append(" Available Processors\n");
        stringBuilder.append(operatingSystemMXBean.getName()).append(" Name\n");
        stringBuilder.append(operatingSystemMXBean.getVersion()).append(" Version\n");
        stringBuilder.append(operatingSystemMXBean.getArch()).append(" Arch\n");
        stringBuilder.append(operatingSystemMXBean.getSystemLoadAverage()).append(" System Load Average\n");
        stringBuilder.append(operatingSystemMXBean.getObjectName()).append(" Object Name\n");
        
        return stringBuilder.toString();
    }
    
    static @NotNull String getTotalCPUTime() {
        long cpuTime = UsefulUtilities.getCPUTime();
        return MessageFormat.format("Total CPU time for all threads = {0} milliseconds.", TimeUnit.NANOSECONDS.toMillis(cpuTime));
    }
    
    static @NotNull String getMemory() {
        StringBuilder stringBuilder = new StringBuilder();
        
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        memoryMXBean.setVerbose(true);
        stringBuilder.append(memoryMXBean.getHeapMemoryUsage()).append(" Heap Memory Usage; \n");
        stringBuilder.append(memoryMXBean.getNonHeapMemoryUsage()).append(" NON Heap Memory Usage; \n");
        stringBuilder.append(memoryMXBean.getObjectPendingFinalizationCount()).append(" Object Pending Finalization Count; \n");
        
        List<MemoryManagerMXBean> memoryManagerMXBean = ManagementFactory.getMemoryManagerMXBeans();
        for (MemoryManagerMXBean managerMXBean : memoryManagerMXBean) {
            stringBuilder.append(Arrays.toString(managerMXBean.getMemoryPoolNames())).append(" \n");
        }
        
        ClassLoadingMXBean classLoading = ManagementFactory.getClassLoadingMXBean();
        stringBuilder.append(classLoading.getLoadedClassCount()).append(" Loaded Class Count; \n");
        stringBuilder.append(classLoading.getUnloadedClassCount()).append(" Unloaded Class Count; \n");
        stringBuilder.append(classLoading.getTotalLoadedClassCount()).append(" Total Loaded Class Count; \n");
        
        CompilationMXBean compileBean = ManagementFactory.getCompilationMXBean();
        stringBuilder.append(compileBean.getName()).append(" Name; \n");
        stringBuilder.append(compileBean.getTotalCompilationTime()).append(" Total Compilation Time; \n");
        
        return stringBuilder.toString();
    }
    
    static @NotNull String getRuntime() {
        StringBuilder stringBuilder = new StringBuilder();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        stringBuilder.append(runtimeMXBean.getClass().getSimpleName()).append("\n");
        stringBuilder.append(new Date(runtimeMXBean.getStartTime())).append(" StartTime\n");
        stringBuilder.append(MX_BEAN_THREAD.getObjectName()).append(" object name, \n");
        stringBuilder.append(MX_BEAN_THREAD.getTotalStartedThreadCount()).append(" total threads started, \n");
        stringBuilder.append(MX_BEAN_THREAD.getThreadCount()).append(" current threads live, \n");
        stringBuilder.append(MX_BEAN_THREAD.getPeakThreadCount()).append(" peak live, ");
        stringBuilder.append(MX_BEAN_THREAD.getDaemonThreadCount()).append(" Daemon Thread Count, \n");
        return stringBuilder.toString();
    }
    
/*    static long countCPUTime() {
        long retLong = 0;
        for (long threadId : MX_BEAN_THREAD.getAllThreadIds()) {
            long cpuTime = MX_BEAN_THREAD.getThreadCpuTime(threadId);
            retLong += cpuTime;
        }
        return retLong;
    }*/
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    static @NotNull InformationFactory getInstance(@NotNull String type) {
        switch (type) {
            case INET_USAGE:
                return InternetUse.getI();
            case STATS_PC:
                return Stats.getPCStats();
            case STATS_WEEKLYINET:
                return Stats.getInetStats();
            case LOGS_EXT:
                return Stats.getLogStats();
            case USER:
                return UserInfo.getI(type);
            default:
                return PCInfo.getInstance(type);
        }
    }
    
    default String writeLog(String logName, String information) {
        information = new Date().toString() + "\n" + information;
        return FileSystemWorker.writeFile(logName, information);
    }
    
    String getInfo();
}
