// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.componentsrepo.InvokeIllegalException;
import ru.vachok.networker.exe.KudrMonitor;
import ru.vachok.networker.exe.schedule.Do0213Monitor;
import ru.vachok.networker.services.DBMessenger;


/**
 @see ru.vachok.networker.abstr.monitors.AbstractMonitorFactoryTest
 @since 11.07.2019 (15:59) */
public abstract class AbstractMonitorFactory implements Pinger, NetMonitor {
    
    private static MessageToUser messageToUser = new DBMessenger(AbstractMonitorFactory.class.getSimpleName());
    
    @Contract("_ -> new")
    public static @NotNull NetMonitorFactory createNetMonitorFactory(String monitorParameter) {
        if (monitorParameter.equalsIgnoreCase("10.200.213.85")) {
            return new Do0213Monitor(monitorParameter);
        }
        else if (monitorParameter.equalsIgnoreCase("kudr")) {
            return new KudrMonitor();
        }
        else {
            throw new InvokeIllegalException();
        }
    }
    
    public static void testMethod() {
    
    }
}
