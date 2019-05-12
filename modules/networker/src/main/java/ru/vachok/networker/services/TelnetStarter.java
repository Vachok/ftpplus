// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.ConnectToMe;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.MyConsoleServer;
import ru.vachok.networker.net.TestServer;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;


/**
 Class ru.vachok.networker.services.TelnetStarter
 <p>
 
 @since 12.05.2019 (18:42) */
public class TelnetStarter implements Runnable {
    
    
    private static final Properties APP_PROPS = AppComponents.getProps();
    
    private static MessageToUser messageToUser = new MessageLocal(TelnetStarter.class.getSimpleName());
    
    @Override public void run() {
        AppComponents.threadConfig().thrNameSet(APP_PROPS.getProperty(ConstantsFor.PR_LPORT));
        starterTelnet();
    }
    
    @SuppressWarnings("resource")
    private void starterTelnet() {
        ConnectToMe myConsoleServer = MyConsoleServer.getI();
        if (ConstantsFor.PR_OSNAME_LOWERCASE.contains("bsd") || APP_PROPS.getProperty(ConstantsFor.PR_TESTSERVER).contains("true")) {
            AppComponents.threadConfig().execByThreadConfig(TelnetStarter::testServerStart);
        }
        else {
            ((MyConsoleServer) myConsoleServer).setSocket(new Socket());
            while (!((MyConsoleServer) myConsoleServer).getSocket().isClosed()) {
                try {
                    myConsoleServer.reconSock();
                }
                catch (IOException | InterruptedException | NullPointerException e1) {
                    messageToUser.info("AppInfoOnLoad.starterTelnet", "e1.getMessage()", e1.getMessage());
                    FileSystemWorker.error("SystemTrayHelper.starterTelnet", e1);
                    Thread.currentThread().interrupt();
                }
            }
            System.setOut(System.err);
        }
    }
    
    private static void testServerStart() {
        AppComponents.threadConfig().thrNameSet("11111");
        try {
            ConnectToMe connectToMe = new TestServer(11111);
            connectToMe.runSocket();
        }
        catch (Exception e) {
            messageToUser.error(FileSystemWorker.error(AppInfoOnLoad.class.getSimpleName() + ".testServerStart", e));
        }
    }
    
    
}