// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.abstr.monitors.RunningStatistics;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.net.NetPingerServiceFactory;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.fsworks.FilesWorkerFactory;
import ru.vachok.networker.restapi.message.DBMessenger;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Callable;


public abstract class AbstractNetworkerFactory implements RunningStatistics {
    
    
    private static MessageToUser messageToUser = new DBMessenger(AbstractNetworkerFactory.class.getSimpleName());
    
    private static String concreteFactoryName = AbstractNetworkerFactory.class.getTypeName();
    
    public static void setConcreteFactoryName(String concreteFactoryName) {
        AbstractNetworkerFactory.concreteFactoryName = concreteFactoryName;
    }
    
    public static Callable<String> getSSHFactory(String srvName, String commandSSHToExecute, String classCaller) {
        return createSSHFactory(srvName, commandSSHToExecute, classCaller);
    }
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Contract("_ -> new")
    public static @NotNull AbstractNetworkerFactory getInstance(String concreteFactoryName) {
        setConcreteFactoryName(concreteFactoryName);
    
        if (concreteFactoryName.equals(NetPingerServiceFactory.class.getTypeName())) {
            return new NetPingerServiceFactory();
        }
        if (concreteFactoryName.equals(FilesWorkerFactory.class.getTypeName())) {
            return FilesWorkerFactory.getInstance();
        }
        else {
            throw new TODOException("22.07.2019 (15:30)");
        }
    }
    
    @Contract(" -> new")
    public static @NotNull AbstractNetworkerFactory getInstance() {
        messageToUser.warn(AbstractNetworkerFactory.class.getSimpleName(), "Returning DEFAULT instance!", NetPingerServiceFactory.class.getTypeName());
        return new NetPingerServiceFactory();
    }
    
    @Contract(" -> fail")
    public static void testMethod() {
        throw new InvokeEmptyMethodException(AbstractNetworkerFactory.class.getTypeName());
    }
    
    public boolean isReach(InetAddress name) {
        return new NetPingerServiceFactory().isReach(name);
    }
    
    @Override
    public String getCPU() {
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
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractNetworkerFactory{");
        sb.append(concreteFactoryName);
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String getMemory() {
        return ConstantsFor.getMemoryInfo();
    }
    
    @Override
    public String getRuntime() {
        StringBuilder stringBuilder = new StringBuilder();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        stringBuilder.append(runtimeMXBean.getClass().getSimpleName()).append("\n\n");
        stringBuilder.append(runtimeMXBean.getName()).append(" Name\n");
        stringBuilder.append(new Date(runtimeMXBean.getStartTime())).append(" StartTime\n");
    
        return stringBuilder.toString();
    }
    
    
    private static SSHFactory createSSHFactory(String connectTo, String command, String caller) {
        SSHFactory.Builder sshFactory = new SSHFactory.Builder(connectTo, command, caller);
        concreteFactoryName = SSHFactory.class.getTypeName() + "\n" + new TForms().fromArray(Arrays.asList(SSHFactory.class.getTypeParameters()));
        return sshFactory.build();
    }
    
    @Contract(" -> fail")
    private static RunningStatistics createStat() {
        throw new InvokeEmptyMethodException("16.07.2019 (13:12)");
    }
}
