// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.testng.annotations.Test;
import ru.vachok.networker.abstr.ConnectToMe;


public class TelnetStarterTest {
    
    
    @Test
    public void startServer() {
        ConnectToMe connectToMe = new TestServer();
        connectToMe.runSocket();
    }
    
    
}