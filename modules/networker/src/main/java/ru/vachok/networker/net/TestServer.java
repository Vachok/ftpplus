// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
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
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


/**
 Class ru.vachok.networker.net.TestServer
 <p>
 
 @since 10.05.2019 (13:48) */
public class TestServer implements ConnectToMe {
    
    
    private ServerSocketChannel channel;
    
    private ServerSocket serverSocket;
    
    private PrintStream printStreamF;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override public void runSocket() throws IOException {
        ServerSocket socketSrv = new ServerSocket(11111);
        this.serverSocket = socketSrv;
        while (true) {
            socketSrv.setReuseAddress(true);
            this.channel = socketSrv.getChannel();
            if (channel != null) {
                channel.bind(socketSrv.getLocalSocketAddress());
            }
            accepSoc(socketSrv.accept());
        }
    }
    
    @Override public void accepSoc(final Socket socket) {
        try {
            InputStream iStream = socket.getInputStream();
            Scanner scanner = new Scanner(iStream);
            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            {
                this.printStreamF = printStream;
                while (socket.isConnected()) {
                    System.setIn(iStream);
                    System.setOut(printStream);
                    scanInput(scanner, socket);
                    printStream.print(iStream.read());
                }
            }
            this.accepSoc(socket);
        }
        catch (IOException e) {
            System.setOut(System.err);
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".accepSoc", e));
        }
        System.setOut(System.err);
    }
    
    private void scanInput(Scanner scanner, Socket socket) {
        if (scanner.nextLine().contains("test")) {
            System.out.println("test");
        }
        else {
            try {
                String str = new AppComponents().sshActs().getProviderTraceStr();
                messageToUser.info(getClass().getSimpleName() + ".scanInput", "str", " = " + str);
            }
            catch (InterruptedException | ExecutionException | TimeoutException e) {
                messageToUser.error(e.getMessage());
            }
            this.accepSoc(socket);
        }
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