// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.abstr.monitors.PingerService;
import ru.vachok.networker.abstr.monitors.RunningStatistics;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.net.NetPingerService;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.DBMessenger;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.Callable;


public abstract class AbstractNetworkerFactory implements PingerService, RunningStatistics {
    
    
    private static MessageToUser messageToUser = new DBMessenger(AbstractNetworkerFactory.class.getSimpleName());
    
    private static String concreteFactoryName;
    
    public Callable<String> getSSHFactory(String srvName, String commandSSHToExecute, String classCaller) {
        return createSSHFactory(srvName, commandSSHToExecute, classCaller);
    }
    
    public static AbstractNetworkerFactory getInstance() {
        concreteFactoryName = NetPingerService.class.getTypeName();
        return new NetPingerService();
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
        throw new InvokeEmptyMethodException("16.07.2019 (14:11)");
    }
    
    @Override
    public String getExecution() {
        throw new InvokeEmptyMethodException("16.07.2019 (14:11)");
    }
    
    @Override
    public String getPingResultStr() {
        throw new InvokeEmptyMethodException("16.07.2019 (14:11)");
    }
    
    @Override
    public boolean isReach(InetAddress inetAddrStr) {
        return new NetPingerService().isReach(inetAddrStr);
    }
    
    private static SSHFactory createSSHFactory(String connectTo, String command, String caller) {
        SSHFactory.Builder sshFactory = new SSHFactory.Builder(connectTo, command, caller);
        concreteFactoryName = SSHFactory.class.getTypeName() + "\n" + new TForms().fromArray(Arrays.asList(SSHFactory.class.getTypeParameters()));
        return sshFactory.build();
    }
    
    @Override
    public String writeLogToFile() {
        throw new InvokeEmptyMethodException("16.07.2019 (14:11)");
    }
    
    private static RunningStatistics createStat() {
        throw new InvokeEmptyMethodException("16.07.2019 (13:12)");
    }
    
    @Contract(" -> fail")
    public static void testMethod() {
        throw new InvokeEmptyMethodException(AbstractNetworkerFactory.class.getTypeName());
    }
}
