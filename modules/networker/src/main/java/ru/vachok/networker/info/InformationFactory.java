package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.lang.management.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 @since 09.04.2019 (13:16) */
public interface InformationFactory {
    
    
    String getInfoAbout(String aboutWhat);
    
    void setInfo(Object info);
    
    static @NotNull String getRunningInformation() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CPU information:").append("\n").append(getCPU()).append("***\n");
        stringBuilder.append("Memory information:").append("\n").append(getMemory()).append("***\n");
        stringBuilder.append("Runtime information:").append("\n").append(getRuntime()).append("***\n");
        return stringBuilder.toString();
        
    }
    
    static @NotNull String getCPU() {
        StringBuilder stringBuilder = new StringBuilder();
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        
        stringBuilder.append(operatingSystemMXBean.getClass().getTypeName()).append("\n");
        stringBuilder.append(operatingSystemMXBean.getAvailableProcessors()).append(" Available Processors\n");
        stringBuilder.append(operatingSystemMXBean.getName()).append(" Name\n");
        stringBuilder.append(operatingSystemMXBean.getVersion()).append(" Version\n");
        stringBuilder.append(operatingSystemMXBean.getArch()).append(" Arch\n");
        stringBuilder.append(operatingSystemMXBean.getSystemLoadAverage()).append(" System Load Average\n");
        stringBuilder.append(operatingSystemMXBean.getObjectName()).append(" Object Name\n");
        
        return stringBuilder.toString();
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
        stringBuilder.append(runtimeMXBean.getName()).append(" Name\n");
        stringBuilder.append(new Date(runtimeMXBean.getStartTime())).append(" StartTime\n");
        
        return stringBuilder.toString();
    }
    
    default String writeLog(String logName, String information) {
        return FileSystemWorker.writeFile(logName, information);
    }
}
