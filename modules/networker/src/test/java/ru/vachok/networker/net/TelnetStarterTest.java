// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.testng.annotations.Test;
import ru.vachok.networker.abstr.ConnectToMe;


public class TelnetStarterTest {
    
    
    @Test(enabled = false)
    public void startServer() {
        ConnectToMe connectToMe = new TestServer(9990);
        try {
            connectToMe.runSocket();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}