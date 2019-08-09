// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.server;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.util.StringJoiner;


/**
 @see ru.vachok.networker.componentsrepo.server.TelnetStarterTest
 @since 08.06.2019 (4:36) */
public class TelnetStarter implements Runnable {
    
    private MessageToUser messageToUser = new MessageLocal(TelnetStarter.class.getSimpleName());
    
    @Override
    public void run() {
        Thread.currentThread().setName("TELNET");
        int lPort = Integer.parseInt(AppComponents.getProps().getProperty(TelnetServer.PR_LPORT, "9990"));
        ConnectToMe connectToMe = new TelnetServer(Integer.parseInt(TelnetServer.PR_LPORT));
        messageToUser.warn(connectToMe.getClass().getSimpleName() + " *** PORT IS: " + lPort);
        connectToMe.runSocket();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", TelnetStarter.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}
