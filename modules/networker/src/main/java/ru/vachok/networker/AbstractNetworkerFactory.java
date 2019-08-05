// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.net.LongNetScanServiceFactory;
import ru.vachok.networker.restapi.fsworks.FilesWorkerFactory1;

import java.lang.management.*;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 @see ru.vachok.networker.AbstractNetworkerFactoryTest
 @since 15.07.2019 (10:45) */
public abstract class AbstractNetworkerFactory {
    
    private static String concreteFactoryName = AbstractNetworkerFactory.class.getTypeName();
    
    public static SSHFactory getSSHFactory(String srvName, String commandSSHToExecute, String classCaller) {
        SSHFactory.Builder builder = new SSHFactory.Builder(srvName, commandSSHToExecute, classCaller);
        return builder.build();
    }
    
    @Contract(pure = true)
    public static @NotNull FilesWorkerFactory1 getFilesFactory() {
        return FilesWorkerFactory1.getInstance();
    }
    
    @Contract(" -> new")
    public static @NotNull LongNetScanServiceFactory netScanServiceFactory() {
        return new LongNetScanServiceFactory();
    }
    
    @Contract("_ -> new")
    @Deprecated
    public static @NotNull AbstractNetworkerFactory getInstance(@NotNull String concreteFactoryName) {
        if (concreteFactoryName.equals(LongNetScanServiceFactory.class.getTypeName())) {
            return new LongNetScanServiceFactory();
        }
        if (concreteFactoryName.equals(FilesWorkerFactory1.class.getTypeName())) {
            return FilesWorkerFactory1.getInstance();
        }
        else {
            throw new TODOException("22.07.2019 (15:30)");
        }
    }
    
    @Contract(" -> new")
    public static @NotNull AbstractNetworkerFactory getInstance() {
        return new LongNetScanServiceFactory();
    }
    
    public boolean isReach(InetAddress name) {
        System.out.println(AppComponents.ipFlushDNS());
        return new LongNetScanServiceFactory().isReach(name);
    }
    
    public static @NotNull String getApplicationRunInformation() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CPU information:").append("\n").append(getCPU()).append("***\n");
        stringBuilder.append("Memory information:").append("\n").append(getMemory()).append("***\n");
        stringBuilder.append("Runtime information:").append("\n").append(getRuntime()).append("***\n");
        return stringBuilder.toString();
    }
    
    private static @NotNull String getCPU() {
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
    
    private static @NotNull String getMemory() {
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
    
    private static @NotNull String getRuntime() {
        StringBuilder stringBuilder = new StringBuilder();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        stringBuilder.append(runtimeMXBean.getClass().getSimpleName()).append("\n");
        stringBuilder.append(runtimeMXBean.getName()).append(" Name\n");
        stringBuilder.append(new Date(runtimeMXBean.getStartTime())).append(" StartTime\n");
        
        return stringBuilder.toString();
    }
}
