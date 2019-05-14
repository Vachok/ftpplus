// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.ConnectToMe;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.util.Properties;


public class TelnetStarter implements Runnable {
    
    
    private static final Properties APP_PROPS = AppComponents.getProps();
    
    private MessageToUser messageToUser = new MessageLocal(TelnetStarter.class.getSimpleName());
    
    private static final int L_PORT = Integer.parseInt(AppComponents.getProps().getProperty(ConstantsFor.PR_LPORT, "9990"));
    
    @Override public void run() {
        ConnectToMe connectToMe;
        if (APP_PROPS.getProperty(ConstantsFor.PR_TESTSERVER).contains("true") || ConstantsFor.PR_OSNAME_LOWERCASE.contains("free")) {
            connectToMe = new TestServer(L_PORT);
        }
        else {
            connectToMe = MyConsoleServer.getI(L_PORT);
        }
    
        try {
            messageToUser.warn(connectToMe.getClass().getSimpleName() + " *** PORT IS: " + L_PORT);
            connectToMe.runSocket();
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
}
