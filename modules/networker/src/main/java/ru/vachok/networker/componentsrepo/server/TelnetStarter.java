// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.server;


import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import java.util.StringJoiner;


/**
 @see ru.vachok.networker.componentsrepo.server.TelnetStarterTest
 @since 08.06.2019 (4:36) */
public class TelnetStarter implements Runnable {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, TelnetStarter.class.getSimpleName());
    
    private int telnetPort = 0;
    
    public int getTelnetPort() {
        return telnetPort;
    }
    
    public void setTelnetPort(int telnetPort) {
        this.telnetPort = telnetPort;
    }
    
    @Override
    public void run() {
        Thread.currentThread().setName("TELNET");
        int lPort = Integer.parseInt(InitProperties.getTheProps().getProperty(TelnetServer.PR_LPORT, "9990"));
        if (telnetPort > 0) {
            lPort = telnetPort;
        }
        ConnectToMe connectToMe = new TelnetServer(lPort);
        messageToUser.info(connectToMe.getClass().getSimpleName(), " *** PORT IS: ", String.valueOf(lPort));
        connectToMe.runSocket();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", TelnetStarter.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}
