// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.server;


import java.net.Socket;


/**
 @since 12.05.2019 (17:23) */
public interface ConnectToMe {
    
    void runSocket();
    
    Socket getSocket();
}
