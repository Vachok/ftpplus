package ru.vachok.networker.net;


import ru.vachok.networker.abstr.ConnectToMe;

import java.io.IOException;


public class TelnetStarter implements Runnable {
    
    
    @Override public void run() {
        ConnectToMe connectToMe = MyConsoleServer.getI();
        try {
            connectToMe.runSocket();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
