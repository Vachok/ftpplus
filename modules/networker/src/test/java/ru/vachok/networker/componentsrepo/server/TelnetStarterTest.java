// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.server;


import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;


/**
 @see TelnetStarter */
public class TelnetStarterTest {
    
    
    private SocketAddress socketAddress;
    
    private Socket socket;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    @BeforeMethod
    public void setUp() {
        try {
            this.socketAddress = new InetSocketAddress(InetAddress.getByName(UsefulUtilities.thisPC()), 9999);
        }
        catch (UnknownHostException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void startServer() {
        TelnetStarter telnetStarter = new TelnetStarter();
        telnetStarter.setTelnetPort(9999);
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(telnetStarter);
        Future<Boolean> submit = Executors.newSingleThreadExecutor().submit(this::checkSocket);
        try {
            Assert.assertTrue(submit.get(15, TimeUnit.SECONDS));
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    private boolean checkSocket() {
        try (Socket socketVar = new Socket()) {
            this.socket = socketVar;
            socket.connect(socketAddress);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            return false;
        }
        return socket.isConnected();
    }
}