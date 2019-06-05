package ru.vachok.networker.net;


import org.testng.annotations.Test;
import ru.vachok.networker.abstr.ConnectToMe;


public class TelnetStarterTest {
    
    
    @Test
    public void startServer() {
        ConnectToMe connectToMe = new TestServer(9990);
        connectToMe.runSocket();
    }
    
    
}