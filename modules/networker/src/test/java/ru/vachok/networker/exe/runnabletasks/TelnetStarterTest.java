// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.net.enums.OtherKnownDevices;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.concurrent.*;


public class TelnetStarterTest {
    
    
    private SocketAddress socketAddress;
    
    private Socket socket;
    
    @BeforeMethod
    public void setUp() {
        try {
            this.socketAddress = new InetSocketAddress(InetAddress.getByName(OtherKnownDevices.DO0213_KUDR), 9990);
        }
        catch (UnknownHostException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void startServer() {
        Runnable telnetStarter = new TelnetStarter();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<?> future = executorService.submit(telnetStarter);
        Runnable runnable = ()->{
            try {
                future.get(5, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
            catch (TimeoutException e) {
                Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
            
        };
        
        new Thread(runnable).start();
        Assert.assertTrue(checkSocket());
    }
    
    private boolean checkSocket() {
        try (Socket socketVar = new Socket()) {
            this.socket = socketVar;
            byte[] bufBytes = new byte[ConstantsFor.KBYTE];
            socket.connect(socketAddress);
            try (InputStream stream = socket.getInputStream();) {
                while (true) {
                    stream.read(bufBytes);
                    if (stream.available() <= 0) {
                        break;
                    }
                }
            }
            System.out.println("bufBytes = " + new String(bufBytes));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            return false;
        }
        return socket.isConnected();
    }
    
}