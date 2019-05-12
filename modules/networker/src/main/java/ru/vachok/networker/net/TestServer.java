// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import ru.vachok.networker.abstr.ConnectToMe;

import java.awt.*;


/**
 Class ru.vachok.networker.net.TestServer
 <p>
 
 @since 12.05.2019 (17:23) */
public class TestServer implements ConnectToMe {
    
    
    private int listenPort;
    
    public TestServer(int listenPort) {
        this.listenPort = listenPort;
    }
    
    @Override public void runSocket() {
        throw new IllegalComponentStateException("12.05.2019 (17:24)");
    }
}