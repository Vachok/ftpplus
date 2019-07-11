package ru.vachok.networker.abstr.monitors;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.componentsrepo.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.InvokeIllegalException;
import ru.vachok.networker.exe.schedule.Do0213Monitor;
import ru.vachok.networker.services.DBMessenger;


/**
 @see ru.vachok.networker.abstr.monitors.AbstractMonitorFactoryTest
 @since 11.07.2019 (15:59) */
public abstract class AbstractMonitorFactory implements Pinger, NetMonitor {
    
    
    private static final InvokeEmptyMethodException EMPTY_METHOD_EXCEPTION = new InvokeEmptyMethodException(AbstractMonitorFactory.class.getTypeName());
    
    private static MessageToUser messageToUser = new DBMessenger(AbstractMonitorFactory.class.getSimpleName());
    
    public static NetMonitorFactory createNetMonitorFactory(String monitorParameter) {
        if (monitorParameter.contains("10.200")) {
            return new Do0213Monitor(monitorParameter);
        }
        else {
            throw new InvokeIllegalException();
        }
    }
}
