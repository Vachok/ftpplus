// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractNetworkerFactory;
import ru.vachok.networker.net.NetPingerServiceFactory;


/**
 @see ru.vachok.networker.abstr.monitors.NetMonitorFactoryTest
 @since 7/11/2019 (5:13 PM) */
public abstract class NetFactory extends AbstractNetworkerFactory {
    
    
    @Contract("_ -> new")
    public static @NotNull NetMonitor createOnePCMonitor(String hostAddr) {
        return new NetPingerServiceFactory();
    }
    
    public abstract void setLaunchTimeOut(int i);
}
