// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.abstr.monitors.NetFactory;
import ru.vachok.networker.abstr.monitors.Pinger;
import ru.vachok.networker.abstr.monitors.RunningStatistics;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.net.NetPinger;
import ru.vachok.networker.net.scanner.Kudr;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.DBMessenger;

import java.text.MessageFormat;


public abstract class AbstractNetworkerFactory implements Pinger, RunningStatistics {
    
    
    
    private static MessageToUser messageToUser = new DBMessenger(AbstractNetworkerFactory.class.getSimpleName());
    
    @Contract("_ -> new")
    public static @NotNull NetFactory createNetMonitorFactory(@NotNull String monitorParameter) {
        if (monitorParameter.equalsIgnoreCase("net")) {
            return new NetPinger();
        }
        else if (monitorParameter.equalsIgnoreCase("kudr")) {
            return new Kudr();
        }
        else {
            throw new InvokeIllegalException(MessageFormat.format("Can''t create new Test Abstract Factory. {0} is bad method parameter", monitorParameter));
        }
    }
    
    public static SSHFactory createSSHFactory(String connectTo, String command, String caller) {
        SSHFactory.Builder sshFactory = new SSHFactory.Builder(connectTo, command, caller);
        return sshFactory.build();
    }
    
    public static AbstractNetworkerFactory getInstance() {
        throw new InvokeEmptyMethodException("16.07.2019 (14:09)");
    }
    
    private static RunningStatistics createStat() {
        throw new InvokeEmptyMethodException("16.07.2019 (13:12)");
    }
    
    @Contract(" -> fail")
    public static void testMethod() {
        throw new InvokeEmptyMethodException(AbstractNetworkerFactory.class.getTypeName());
    }
}
