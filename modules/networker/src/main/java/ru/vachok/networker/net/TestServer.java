// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.IntoApplication;
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
import java.util.Scanner;


/**
 Class ru.vachok.networker.net.TestServer
 <p>
 
 @since 10.05.2019 (13:48) */
public class TestServer implements ConnectToMe {
    
    private ServerSocket serverSocket;
    
    private PrintStream printStreamF;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override public void runSocket() throws IOException {
        ServerSocket socketSrv = new ServerSocket(11111);
        this.serverSocket = socketSrv;
        socketSrv.setReuseAddress(true);
        while (true) {
            accepSoc(socketSrv.accept());
        }
    }
    
    @Override public void accepSoc(Socket socket) {
        try {
            InputStream iStream = socket.getInputStream();
            Scanner scanner = new Scanner(iStream);
            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            {
                this.printStreamF = printStream;
                System.setOut(printStreamF);
                System.out.println("Socket " + socket.getInetAddress().toString() + ":" + socket.getPort() + " is connected");
                while (socket.isConnected()) {
                    System.setIn(iStream);
                    scanInput(scanner.nextLine(), socket);
                    printStream.print(iStream.read());
                }
            }
        }
        catch (IOException e) {
            System.setOut(System.err);
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".accepSoc", e));
        }
        printStreamF.close();
        System.setOut(System.out);
    }
    
    private void scanInput(String scannerLine, Socket socket) throws IOException {
        if (scannerLine.contains("test")) {
            System.out.println("test");
            accepSoc(socket);
        }
        else if (scannerLine.contains("refresh")) {
            ConfigurableApplicationContext context = IntoApplication.getConfigurableApplicationContext();
            context.stop();
            context.close();
            context = new SpringApplication().run(IntoApplication.class);
            new IntoApplication().setConfigurableApplicationContext(context);
            context.start();
        }
        else if (scannerLine.contains("q")) {
            socket.close();
        }
        else {
            accepSoc(socket);
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