// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.exe.runnabletasks.TelnetStarter;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;


public class TelnetStarterTest {
    
    
    @Test(timeOut = 20000, enabled = false)
    public void startServer() {
        TelnetStarter telnetStarter = new TelnetStarter();
        Executors.newSingleThreadExecutor().execute(telnetStarter);
        try (Socket socket = new Socket()) {
            InetSocketAddress inetSocketAddress = InetSocketAddress.createUnresolved(InetAddress.getLocalHost().getCanonicalHostName(), 9990);
            socket.connect(inetSocketAddress);
            Assert.assertTrue(socket.isConnected(), socket.toString());
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
}