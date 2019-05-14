// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.ConnectToMe;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.util.Properties;


public class TelnetStarter implements Runnable {
    
    
    private static final Properties APP_PROPS = AppComponents.getProps();
    
    private MessageToUser messageToUser = new MessageLocal(TelnetStarter.class.getSimpleName());
    
    @Override public void run() {
        ConnectToMe connectToMe = MyConsoleServer.getI();
        if (APP_PROPS.getProperty(ConstantsFor.PR_TESTSERVER).contains("true") || ConstantsFor.PR_OSNAME_LOWERCASE.contains("free")) {
            connectToMe = new TestServer(9990);
            try {
                connectToMe.runSocket();
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
        else {
            while (!connectToMe.getSocket().isClosed()) {
                try {
                    connectToMe.reconSock();
                }
                catch (IOException | InterruptedException e) {
                    messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".run", e));
                    Thread.currentThread().checkAccess();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
