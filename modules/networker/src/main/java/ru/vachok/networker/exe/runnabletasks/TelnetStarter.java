// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.abstr.ConnectToMe;
import ru.vachok.networker.net.TestServer;
import ru.vachok.networker.services.MessageLocal;

import java.util.Properties;


/**
 @since 08.06.2019 (4:36) */
public class TelnetStarter implements Runnable {
    
    
    private static final Properties APP_PROPS = AppComponents.getProps();
    
    private MessageToUser messageToUser = new MessageLocal(TelnetStarter.class.getSimpleName());
    
    @Override public void run() {
        int lPort = Integer.parseInt(AppComponents.getProps().getProperty(TestServer.PR_LPORT, "9990"));
        ConnectToMe connectToMe = new TestServer(Integer.parseInt(TestServer.PR_LPORT));
        messageToUser.warn(connectToMe.getClass().getSimpleName() + " *** PORT IS: " + lPort);
        connectToMe.runSocket();
    }
}
