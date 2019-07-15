// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.net.NetPinger;
import ru.vachok.networker.net.scanner.KudrNetworker;
import ru.vachok.networker.restapi.message.DBMessenger;

import java.text.MessageFormat;


public abstract class AbstractNetworkerFactory implements Pinger {
    
    
    private static MessageToUser messageToUser = new DBMessenger(AbstractNetworkerFactory.class.getSimpleName());
    
    @Contract("_ -> new")
    public static @NotNull NetNetworkerFactory createNetMonitorFactory(@NotNull String monitorParameter) {
        if (monitorParameter.equalsIgnoreCase("net")) {
            return new NetPinger();
        }
        else if (monitorParameter.equalsIgnoreCase("kudr")) {
            return new KudrNetworker();
        }
        else {
            throw new InvokeIllegalException(MessageFormat.format("Can''t create new Test Abstract Factory. {0} is bad method parameter", monitorParameter));
        }
    }
    
    public static SSHFactory createSSHFactory(String connectTo, String command, String caller) {
        SSHFactory.Builder sshFactory = new SSHFactory.Builder(connectTo, command, caller);
        return sshFactory.build();
    }
    
    @Contract(" -> fail")
    public static void testMethod() {
        throw new InvokeEmptyMethodException(AbstractNetworkerFactory.class.getTypeName());
    }
}
