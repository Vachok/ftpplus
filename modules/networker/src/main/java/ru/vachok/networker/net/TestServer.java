// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.abstr.ConnectToMe;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;


/**
 Class ru.vachok.networker.net.TestServer
 <p>
 
 @since 10.05.2019 (13:48) */
public class TestServer implements ConnectToMe {
    
    
    private ServerSocketChannel channel;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override public void runSocket() throws IOException {
        ServerSocket socketSrv = new ServerSocket(11111);
        while (true) {
            socketSrv.setReuseAddress(true);
            this.channel = socketSrv.getChannel();
            accepSoc(socketSrv.accept());
        }
    }
    
    @Override public void accepSoc(final Socket socket) {
        
        try {
            InputStream iStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            {
                while (socket.isConnected()) {
                    System.setIn(iStream);
                    System.setOut(printStream);
                    printStream.print(iStream.read());
                }
            }
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".accepSoc", e));
        }
        System.setOut(System.err);
    }
    
    @Override public void reconSock() throws IOException, InterruptedException, NullPointerException {
        throw new IllegalComponentStateException("10.05.2019 (14:07)");
    }
    
    @Override public void doCommand(String readLine) throws IOException, InterruptedException {
        throw new IllegalComponentStateException("10.05.2019 (14:07)");
    }
    
    @Override public void printToSocket() throws IOException {
        throw new IllegalComponentStateException("10.05.2019 (14:08)");
    }
}