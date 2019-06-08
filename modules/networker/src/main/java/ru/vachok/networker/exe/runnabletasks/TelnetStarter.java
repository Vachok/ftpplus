// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.ConnectToMe;
import ru.vachok.networker.net.MyConsoleServer;
import ru.vachok.networker.net.TestServer;
import ru.vachok.networker.services.MessageLocal;

import java.util.Properties;


/**
 @since 08.06.2019 (4:36) */
public class TelnetStarter extends Thread {
    
    
    private static final Properties APP_PROPS = AppComponents.getProps();
    
    private MessageToUser messageToUser = new MessageLocal(TelnetStarter.class.getSimpleName());
    
    private static final int L_PORT = Integer.parseInt(AppComponents.getProps().getProperty(ConstantsFor.PR_LPORT, "9990"));
    
    @Override public void run() {
        ConnectToMe connectToMe;
        if (ConstantsFor.PR_OSNAME_LOWERCASE.contains("free")) {
            connectToMe = MyConsoleServer.getI(L_PORT);
        }
        else {
            connectToMe = new TestServer();
        }
    
        messageToUser.warn(connectToMe.getClass().getSimpleName() + " *** PORT IS: " + L_PORT);
        connectToMe.runSocket();
    }
}
