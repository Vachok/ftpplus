// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.testng.annotations.Test;
import ru.vachok.networker.abstr.ConnectToMe;


public class TestServerTest {
    
    
    @Test
    public void testServerStart() {
        int lport = 9990;
        ConnectToMe connectToMe = new TestServer(lport);
        System.out.println("Starting new Socket. Port: " + lport + "\n" + connectToMe.getClass().getSimpleName());
        connectToMe.runSocket();
    }
}
