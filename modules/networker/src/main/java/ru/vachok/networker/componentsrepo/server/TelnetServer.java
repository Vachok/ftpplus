// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.server;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.SwitchesWiFi;
import ru.vachok.networker.net.ssh.Tracerouting;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.componentsrepo.server.TelnetServerTest
 @since 10.05.2019 (13:48) */
@SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed"})
public class TelnetServer implements ConnectToMe {


    private ServerSocket serverSocket;

    private static final String JAR = "file:///G:/My_Proj/FtpClientPlus/modules/networker/ostpst/build/libs/";

    private PrintStream printStreamF;

    private Socket socket;

    private int listenPort;

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, TelnetServer.class.getSimpleName());

    private static final MessageToUser trayMsg = MessageToUser.getInstance(MessageToUser.TRAY, TelnetServer.class.getSimpleName());

    public static final String PR_LPORT = String.valueOf(9990);

    public TelnetServer(int listenPort) {
        this.listenPort = listenPort;
        try {
            this.serverSocket = new ServerSocket(listenPort);
        }
        catch (IOException e) {
            messageToUser.error(getClass().getSimpleName(), "Constructor", e.getMessage() + " see line: 47");
        }

    }

    @Override public Socket getSocket() {
        runSocket();
        return this.socket;
    }

    @Override public void runSocket() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        try {
            this.socket = serverSocket.accept();
            do {
                accepSoc();
            } while (!socket.isClosed());
        }
        catch (IOException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            new TelnetServer(listenPort).runSocket();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TelnetServer{");
        sb.append("serverSocket=").append(serverSocket.getInetAddress());
        try {
            sb.append(", socket=").append(socket.getInetAddress());
        }
        catch (RuntimeException e) {
            messageToUser.error("TelnetServer", "toString", e.getMessage() + " see line: 79");
        }
        sb.append(", listenPort=").append(listenPort);
        sb.append('}');
        return sb.toString();
    }

    private void scanMore(@NotNull String line) throws IOException {
        if (line.equalsIgnoreCase("thr")) {
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

    private void scanInput(@NotNull String scannerLine) throws IOException {
        if (scannerLine.contains("test")) {
            printStreamF.println("test OK");
            accepSoc();
        }
        else if (scannerLine.equals("q")) {
            System.setOut(System.err);
            accepSoc();
        }
        else if (scannerLine.equals(ConstantsFor.FILESUF_SSHACTIONS)) {
            try {
                System.setOut(System.err);
                printStreamF.println(new Tracerouting().call());
                accepSoc();
            }
            catch (Exception e) {
                System.setOut(System.err);
                messageToUser.error("TelnetServer", "scanInput", e.getMessage() + " see line: 146");
                socket.close();
            }
        }
        else if (scannerLine.contains("sshactions:")) {
            System.setOut(System.err);
            String sshCom = scannerLine.split(":")[1];
            SSHFactory buildSSH = new SSHFactory.Builder(SwitchesWiFi.IPADDR_SRVGIT, sshCom, getClass().getSimpleName()).build();
            printStreamF.println(getClass().getSimpleName() + ".scanInput buildSSH  = " + buildSSH.call());
            accepSoc();
        }
        else {
            scanMore(scannerLine);
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
            reconSock();
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
                    trayMsg.info(getClass().getSimpleName(), socket.getLocalSocketAddress().toString(), socket.getRemoteSocketAddress().toString());
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
            System.setOut(System.err);
            reconSock();
        }
        finally {
            System.setOut(System.err);
            reconSock();
        }
    }

    private void reconSock() {
        this.socket = null;
        try {
            this.socket = serverSocket.accept();
            accepSoc();
        }
        catch (IOException e) {
            runSocket();
            messageToUser.error(getClass().getSimpleName(), "reconSock", e.getMessage() + " see line: 219");
        }
    }
}