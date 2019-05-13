package ru.vachok.networker.net;


import ru.vachok.networker.abstr.ConnectToMe;

import java.io.IOException;


public class TelnetStarter implements Runnable {
    
    
    @Override public void run() {
        ConnectToMe connectToMe = new MyConsoleServer();
        try {
            connectToMe.runSocket();
            while (true) {
                connectToMe.reconSock();
            }
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }
}
