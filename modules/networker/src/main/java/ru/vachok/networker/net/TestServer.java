// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


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
    
    @Override public void accepSoc(final Socket socket) {
        try {
            InputStream iStream = socket.getInputStream();
            Scanner scanner = new Scanner(iStream);
            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            System.setIn(iStream);
            this.printStreamF = printStream;
            System.setOut(printStreamF);
            System.out.println("Socket " + socket.getInetAddress().toString() + ":" + socket.getPort() + " is connected");
            while (socket.isConnected()) {
                if (scanner.hasNextLine()) {
                    scanInput(scanner.nextLine(), socket);
                }
                printStream.print(iStream.read());
                }
    
        }
        catch (IOException e) {
            System.setOut(System.err);
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".accepSoc", e));
        }
        printStreamF.close();
        System.setOut(System.err);
    }
    
    private void scanInput(String scannerLine, final Socket socket) throws IOException {
        if (scannerLine.contains("test")) {
            printStreamF.println("test OK");
            this.accepSoc(socket);
        }
        else if (scannerLine.contains("refresh")) {
            ConfigurableApplicationContext context = IntoApplication.getConfigurableApplicationContext();
            context.stop();
            context.close();
            context = new SpringApplication().run(IntoApplication.class);
            new IntoApplication().setConfigurableApplicationContext(context);
            context.start();
            this.accepSoc(socket);
        }
        else if (scannerLine.equals("q")) {
            socket.close();
        }
        else if (scannerLine.contains("ssh")) {
            try {
                printStreamF.println(new AppComponents().sshActs().getProviderTraceStr());
            }
            catch (InterruptedException | TimeoutException | ExecutionException e) {
                messageToUser.error(e.getMessage());
                this.accepSoc(socket);
            }
        }
        else if (scannerLine.contains("con")) {
            System.setOut(System.out);
            accepSoc(socket);
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