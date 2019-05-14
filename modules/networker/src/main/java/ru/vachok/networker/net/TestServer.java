// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.abstr.ConnectToMe;
import ru.vachok.networker.abstr.MakeConvert;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.OstLoader;
import ru.vachok.networker.services.MessageLocal;

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
public class TestServer implements ConnectToMe {
    
    
    private static final String JAR = "file:///G:/My_Proj/FtpClientPlus/modules/networker/ostpst/build/libs/";
    
    private ServerSocket serverSocket;
    
    private PrintStream printStreamF;
    
    private int listenPort;
    
    public TestServer(int listenPort) {
        this.listenPort = listenPort;
    }
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override public Socket getSocket() {
        throw new IllegalComponentStateException("14.05.2019 (20:30)");
    }
    
    @Override public void runSocket() throws IOException {
        final ServerSocket socketSrv = new ServerSocket(listenPort);
        this.serverSocket = socketSrv;
        socketSrv.setReuseAddress(true);
        while (true) {
            accepSoc(socketSrv.accept());
        }
    }
    
    @Override public void reconSock() {
        throw new IllegalComponentStateException("13.05.2019 (12:57)");
    }
    
    private void accepSoc(final Socket socket) {
        try {
            InputStream iStream = socket.getInputStream();
            Scanner scanner = new Scanner(iStream);
            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            this.printStreamF = printStream;
            printStreamF.println("Socket " + socket.getInetAddress() + ":" + socket.getPort() + " is connected");
            printStreamF.println("Press ENTER. \nOr press something else for quit...");
            while (socket.isConnected()) {
                if (scanner.hasNextLine()) {
                    System.setOut(printStreamF);
                    System.setIn(socket.getInputStream());
                    scanInput(scanner.nextLine(), socket);
                    printStream.print(iStream.read());
                }
            }
            socket.close();
            if (socket.isClosed()) {
                System.setOut(System.err);
                printStream.close();
            }
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".accepSoc", e));
        }
    }
    
    private void scanInput(String scannerLine, Socket socket) throws IOException {
        if (scannerLine.contains("test")) {
            printStreamF.println("test OK");
            accepSoc(socket);
        }
        else if (scannerLine.contains("refresh")) {
            ConfigurableApplicationContext context = IntoApplication.getConfigurableApplicationContext();
            context.stop();
            context.close();
            context = new SpringApplication().run(IntoApplication.class);
            new IntoApplication().setConfigurableApplicationContext(context);
            context.start();
            accepSoc(socket);
        }
        else if (scannerLine.equals("q")) {
            System.setOut(System.err);
        }
        else if (scannerLine.equals("ssh")) {
            try {
                System.setOut(System.err);
                printStreamF.println(new AppComponents().sshActs().getProviderTraceStr());
                accepSoc(socket);
            }
            catch (InterruptedException | TimeoutException | ExecutionException e) {
                System.setOut(System.err);
                messageToUser.error(e.getMessage());
                socket.close();
            }
        }
        else if (scannerLine.contains("ssh:")) {
            System.setOut(System.err);
            String sshCom = scannerLine.split(":")[1];
            SSHFactory buildSSH = new SSHFactory.Builder(ConstantsFor.IPADDR_SRVGIT, sshCom, getClass().getSimpleName()).build();
            printStreamF.println(getClass().getSimpleName() + ".scanInput buildSSH  = " + buildSSH.call());
            accepSoc(socket);
        }
        else {
            scanMore(scannerLine, socket);
        }
    }
    
    private void scanMore(String line, Socket socket) throws IOException {
        if (line.equals("ost")) {
            String fileName = "\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\a.a.zavadskaya.pst";
            printStreamF.println("OSTTOPST: ");
            printStreamF.println(loadLib());
            MakeConvert ostPst = new OstLoader(fileName);
            ostPst.copyierWithSave();
            ostPst.showFileContent();
            accepSoc(socket);
        }
        else if (line.equalsIgnoreCase("scan")) {
            String netScan = NetScannerSvc.getInst().toString();
            printStreamF.println(netScan);
            accepSoc(socket);
        }
        else if (line.isEmpty()) {
            accepSoc(socket);
        }
        else {
            socket.close();
            printStreamF.close();
            System.setOut(System.err);
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
}