// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.exe.KudrMonitor;
import ru.vachok.networker.exe.schedule.Do0213Monitor;
import ru.vachok.networker.services.DBMessenger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;


/**
 @see ru.vachok.networker.abstr.monitors.AbstractMonitorFactoryTest
 @since 11.07.2019 (15:59) */
public abstract class AbstractMonitorFactory implements Pinger, NetMonitor {
    
    private static MessageToUser messageToUser = new DBMessenger(AbstractMonitorFactory.class.getSimpleName());
    
    @Contract("_ -> new")
    public static @NotNull NetMonitorFactory createNetMonitorFactory(String monitorParameter) {
        if (monitorParameter.equalsIgnoreCase("do0213")) {
            return new Do0213Monitor(monitorParameter);
        }
        else if (monitorParameter.equalsIgnoreCase("kudr")) {
            return new KudrMonitor();
        }
        else {
            throw new InvokeIllegalException(MessageFormat.format("Can''t create new Test Abstract Factory. {0} is bad method parameter", monitorParameter));
        }
    }
    
    protected static InetAddress checkParameter(String parameter) {
        NameOrIPChecker nameOrIPChecker = new NameOrIPChecker(parameter);
        InetAddress resolvedIP = InetAddress.getLoopbackAddress();
        try {
            resolvedIP = nameOrIPChecker.resolveIP();
        }
        catch (UnknownHostException e) {
            messageToUser.error(MessageFormat.format("{0} checkParameter.\n{1}", e.getMessage(), new TForms().fromArray(e)));
        }
        return resolvedIP;
    }
    
    public static void testMethod() {
        throw new InvokeEmptyMethodException(AbstractMonitorFactory.class.getTypeName());
    }
}
