// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr;


import java.io.IOException;
import java.net.Socket;


/**
 @since 12.05.2019 (17:23) */
public interface ConnectToMe {
    
    
    void runSocket() throws IOException;
    void reconSock() throws IOException, InterruptedException;
    Socket getSocket();
}
