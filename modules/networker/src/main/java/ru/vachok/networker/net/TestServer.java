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
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.OstToPst;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


/**
 Class ru.vachok.networker.net.TestServer
 <p>
 
 @since 10.05.2019 (13:48) */
public class TestServer implements ConnectToMe, Closeable {
    
    
    private static final String JAR = "file:///G:/My_Proj/FtpClientPlus/modules/networker/ostpst/build/libs/";
    
    private ServerSocket serverSocket;
    
    private PrintStream printStreamF;
    
    private Socket socket;
    
    private int listenPort;
    
    public TestServer(int listenPort) {
        this.listenPort = listenPort;
    }
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    /**
     Closes this stream and releases any system resources associated
     with it. If the stream is already closed then invoking this
     method has no effect.
     
     <p> As noted in {@link AutoCloseable#close()}, cases where the
     close may fail require careful attention. It is strongly advised
     to relinquish the underlying resources and to internally
     <em>mark</em> the {@code Closeable} as closed, prior to throwing
     the {@code IOException}.
     
     @throws IOException if an I/O error occurs
     */
    @Override public void close() throws IOException {
        this.printStreamF.close();
        this.socket.close();
        this.serverSocket.close();
    }
    
    @Override public void runSocket() throws IOException {
        ServerSocket socketSrv = new ServerSocket(11111);
        this.serverSocket = socketSrv;
        socketSrv.setReuseAddress(true);
        while (true) {
            accepSoc(socketSrv.accept());
        }
    }
    
    @Override public void accepSoc(Socket socket) {
        this.socket = socket;
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
            socket.close();
        }
        catch (IOException e) {
            System.setOut(System.err);
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".accepSoc", e));
        }finally {
            printStreamF.close();
            System.setOut(System.err);
        }
    
    }
    
    private void scanInput(String scannerLine, Socket socket) throws IOException {
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
        else if (scannerLine.equals("����\u0006")) {
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
            scanMore(scannerLine);
        }
    }
    
    private void scanMore(String line) throws IOException {
        String fileName = "\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\a.a.zavadskaya.pst";
        printStreamF.println(fileName);
        if (line.equals("ost")) {
            printStreamF.println("OSTTOPST: ");
            printStreamF.println(loadLib());
            MakeConvert ostPst = new OstToPst(fileName);
            ostPst.copyierWithSave();
            ostPst.showFileContent();
        }
    }
    
    private String loadLib() throws IOException {
        File ostJar = new File("ost.jar");
        StringBuilder stringBuilder = new StringBuilder();
        try (URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[]{new URL(JAR)});
             OutputStream outputStream = new FileOutputStream(ostJar)
        ) {
            String libName = "ostpst-8.0.1919.jar";
            Enumeration<URL> resources = urlClassLoader.getResources(libName);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                stringBuilder.append(new File(JAR + libName).length() / 1024 + "/");
                try (InputStream inputStream = url.openStream();
                     InputStreamReader reader = new InputStreamReader(inputStream);
                     BufferedReader bufferedReader = new BufferedReader(reader)
                ) {
                    while (reader.ready()) {
                        int read = inputStream.read();
                        outputStream.write(bufferedReader.read());
                    }
                }
            }
        }
        stringBuilder.append(ostJar.length() / 1024);
        ostJar.deleteOnExit();
        return stringBuilder.toString();
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