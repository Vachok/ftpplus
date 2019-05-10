// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.abstr.ConnectToMe;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;


/**
 Class ru.vachok.networker.net.TestServer
 <p>
 
 @since 10.05.2019 (13:48) */
public class TestServer implements ConnectToMe {
    
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override public void runSocket() throws IOException {
        ServerSocket socketSrv = new ServerSocket(11111);
        while (true) {
            accepSoc(socketSrv.accept());
        }
    }
    
    @Override public void accepSoc(Socket socket) {
        while (socket.isConnected()) {
            try (InputStream iStream = socket.getInputStream();
                 OutputStream outputStream = socket.getOutputStream();
                 PrintStream printStream = new PrintStream(outputStream)
            ) {
                System.setIn(iStream);
                System.setOut(printStream);
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
    }
    
    @Override public void reconSock() throws IOException, InterruptedException, NullPointerException {
    
    }
    
    @Override public void doCommand(String readLine) throws IOException, InterruptedException {
    
    }
    
    @Override public void printToSocket() throws IOException {
    
    }
}