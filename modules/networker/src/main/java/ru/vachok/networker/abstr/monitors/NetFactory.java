// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractNetworkerFactory;
import ru.vachok.networker.net.NetPingerServiceFactory;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;


/**
 @see ru.vachok.networker.abstr.monitors.NetMonitorFactoryTest
 @since 7/11/2019 (5:13 PM) */
public abstract class NetFactory extends AbstractNetworkerFactory implements PingerService {
    
    
    @Contract("_ -> new")
    public static @NotNull NetMonitor createOnePCMonitor(String hostAddr) {
        return new NetPingerServiceFactory();
    }
    
    @Override
    public abstract String getPingResultStr();
    
    @Override
    public abstract String getExecution();
    
    @Override
    public abstract String writeLogToFile();
    
    public abstract List<String> pingDevices(Map<InetAddress, String> ipAddressAndDeviceNameToPing);
    
    @Override
    public abstract boolean isReach(InetAddress inetAddrStr);
}
