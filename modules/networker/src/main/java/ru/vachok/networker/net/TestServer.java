// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.*;
import ru.vachok.networker.abstr.ConnectToMe;
import ru.vachok.networker.abstr.MakeConvert;
import ru.vachok.networker.accesscontrol.sshactions.Tracerouting;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.mailserver.OstLoader;
import ru.vachok.networker.services.MessageLocal;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


/**
 Class ru.vachok.networker.net.TestServer
 <p>
 
 @since 10.05.2019 (13:48) */
public class TestServer implements ConnectToMe {
    
    
    private static final String JAR = "file:///G:/My_Proj/FtpClientPlus/modules/networker/ostpst/build/libs/";
    
    private static final String METHNAME_ACCEPTSOC = ".accepSoc";
    
    private ServerSocket serverSocket;
    
    private PrintStream printStreamF;
    
    private Socket socket;
    
    private int listenPort;
    
    public TestServer(int listenPort) {
        this.listenPort = listenPort;
        try {
            this.serverSocket = new ServerSocket(listenPort);
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".TestServer", e));
        }
    }
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override public Socket getSocket() {
        throw new IllegalComponentStateException("14.05.2019 (20:30)");
    }
    
    @Override public void runSocket() {
        try {
            this.socket = serverSocket.accept();
            do {
                accepSoc();
            } while (!socket.isClosed());
        }
        catch (Exception e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ConstantsFor.METHNAME_RUNSOCKET, e));
            runSocket();
        }
    }
    
    @Override public void reconSock() {
        this.socket = null;
        try {
            this.socket = serverSocket.accept();
            accepSoc();
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".reconSock", e));
        }
    }
    
    private void accepSoc() {
        int timeout = 0;
        try {
            socket.setTcpNoDelay(true);
            timeout = (int) (ConstantsFor.DELAY * ConstantsFor.DELAY) * 100;
            socket.setSoTimeout(timeout);
        }
        catch (SocketException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + METHNAME_ACCEPTSOC, e));
        }
        
        try {
            InputStream iStream = socket.getInputStream();
            Scanner scanner = new Scanner(iStream);
            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            this.printStreamF = printStream;
            printStreamF.println("Socket " + socket.getInetAddress() + ":" + socket.getPort() + " is connected");
            printStreamF.println("Press ENTER. \nOr press something else for quit...");
            printStreamF.println(TimeUnit.MILLISECONDS.toSeconds(timeout) + " socket timeout in second");
            while (socket.isConnected()) {
                    System.setIn(socket.getInputStream());
                    System.setOut(printStreamF);
                if (socket.isConnected()) {
                    scanInput(scanner.nextLine());
                    printStream.print(iStream.read());
                }
                else {
                    System.setOut(System.err);
                    scanner.close();
                }
            }
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + METHNAME_ACCEPTSOC, e));
            System.setOut(System.err);
            reconSock();
        }
        System.out.println(socket.isClosed() + " socket");
        System.setOut(System.err);
        reconSock();
    }
    
    private void scanInput(String scannerLine) throws IOException {
        if (scannerLine.contains("test")) {
            printStreamF.println("test OK");
            accepSoc();
        }
        else if (scannerLine.contains("refresh")) {
            ConfigurableApplicationContext context = IntoApplication.getConfigurableApplicationContext();
            context.stop();
            context.close();
            context = SpringApplication.run(IntoApplication.class);
            new IntoApplication().setConfigurableApplicationContext(context);
            context.start();
            accepSoc();
        }
        else if (scannerLine.equals("q")) {
            System.setOut(System.err);
            accepSoc();
        }
        else if (scannerLine.equals("sshactions")) {
            try {
                System.setOut(System.err);
                printStreamF.println(new Tracerouting().call());
                accepSoc();
            }
            catch (Exception e) {
                System.setOut(System.err);
                messageToUser.error(e.getMessage());
                socket.close();
            }
        }
        else if (scannerLine.contains("sshactions:")) {
            System.setOut(System.err);
            String sshCom = scannerLine.split(":")[1];
            SSHFactory buildSSH = new SSHFactory.Builder(ConstantsFor.IPADDR_SRVGIT, sshCom, getClass().getSimpleName()).build();
            printStreamF.println(getClass().getSimpleName() + ".scanInput buildSSH  = " + buildSSH.call());
            accepSoc();
        }
        else {
            scanMore(scannerLine);
        }
    }
    
    private void scanMore(String line) throws IOException {
        if (line.equals("ost")) {
            String fileName = "\\\\192.168.14.10\\IT-Backup\\Mailboxes_users\\a.a.zavadskaya.pst";
            printStreamF.println("OSTTOPST: ");
            printStreamF.println(loadLib());
            MakeConvert ostPst = new OstLoader(fileName);
            ostPst.copyierWithSave();
            ostPst.showFileContent();
            accepSoc();
        }
        else if (line.equalsIgnoreCase("scan")) {
            String netScan = NetScannerSvc.getInst().toString();
            printStreamF.println(netScan);
            accepSoc();
        }
        else if (line.equalsIgnoreCase("thr")) {
            printStreamF.println(AppComponents.threadConfig());
            accepSoc();
        }
        else if (line.equalsIgnoreCase("exitapp")) {
            new ExitApp(getClass().getSimpleName()).run();
        }
        else if (line.isEmpty()) {
            accepSoc();
        }
        else {
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
                stringBuilder.append(new File(JAR + libName).length() / 1024).append("/");
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