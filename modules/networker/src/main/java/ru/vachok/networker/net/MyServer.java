package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.services.DBMessenger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;


/**
 @since 03.11.2018 (23:51) */
public class MyServer extends Thread {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = MyServer.class.getSimpleName();

    public static final Logger LOGGER = AppComponents.getLogger();

    private static MyServer myServer = new MyServer();

    private static ServerSocket serverSocket;

    private static MessageToUser messageToUser = new DBMessenger();

    private static Socket socket;

    /*Get&Set*/
    public static Socket getSocket() {
        return socket;
    }

    public static void setSocket(Socket socket) {
        MyServer.socket = socket;
    }

    public static MyServer getI() {
        return myServer;
    }

    /*Instances*/
    private MyServer() {
    }

    static {
        try{
            serverSocket = new ServerSocket(ConstantsFor.LISTEN_PORT);
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        try{
            runSocket();
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static void runSocket() throws IOException {
        while(true){
            socket = serverSocket.accept();
            accepSoc(socket);
            if(socket.isClosed()){
                String msg = serverSocket.getReuseAddress() + " getReuseAddress";
                LOGGER.warn(msg);
                break;
            }
        }
    }

    private static void accepSoc(Socket socket) {
        StringBuilder f = new StringBuilder();
        try(Scanner scanner = new Scanner(System.in);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true)){
            System.setOut(new PrintStream(socket.getOutputStream()));
            f.append("\n\n")
                .append(( float ) (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / 60)
                .append(" APP RUNNING \n")
                .append(ConstantsFor.APP_NAME)
                .append("\n\n\n");
            printWriter.println(f.toString());
            if(scanner.hasNext()){
                while(socket.isConnected()){
                    printWriter.print(out);
                }
            }
        }
        catch(IOException e){
            LoggerFactory.getLogger(SOURCE_CLASS).error(e.getMessage(), e);
        }
    }

    public static void reconSock() throws IOException, InterruptedException {
        Socket socket = getServerSocket().accept();
        setSocket(socket);
        InputStream inputStream = socket.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String readLine = bufferedReader.readLine();
        if(readLine.toLowerCase().contains("exit")){
            socket.close();
            System.exit(ConstantsFor.USER_EXIT);
        }
        if(readLine.toLowerCase().contains("help")){
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            printWriter.println("exit - выход из приложения");
            printWriter.println("con - вывод в консоль");
            Thread.sleep(TimeUnit.SECONDS.toMillis(10));
            printToSocket();
        }
        if(readLine.toLowerCase().contains("con")){
            System.setOut(System.err);
            socket.close();
            setSocket(new Socket());
        }
        if(readLine.toLowerCase().contains("thread")){
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            long millis = TimeUnit.SECONDS.toMillis(new SecureRandom().nextInt(20));
            printWriter.println(Thread.currentThread().getState() + " current thread state");
            printWriter.println(Thread.currentThread().getName() + " name");
            printWriter.println(Thread.currentThread().getPriority() + " prio");
            printWriter.println(Thread.currentThread().getThreadGroup().activeCount() + " getThreadGroup().activeCount()");
            printWriter.println(Thread.currentThread().getThreadGroup().activeGroupCount() + " getThreadGroup().activeGroupCount()");
            printWriter.println(Thread.currentThread().getThreadGroup().toString() + " getThreadGroup().toString()");
            printWriter.println();
            printWriter.println(millis / 1000 + " sec to start console read");
            Thread.sleep(millis);
            printToSocket();
        }
        if(readLine.equalsIgnoreCase("shutdown")){
            Runtime.getRuntime().exec("shutdown /p /f");
        }
        if(readLine.equalsIgnoreCase("reboot")){
            Runtime.getRuntime().exec("shutdown /r /f");
        }
        else{
            messageToUser.info(MyServer.class.getSimpleName(), "RUNNING console reader", Arrays.toString(readLine.getBytes()));
            printToSocket();
        }
    }

    private static ServerSocket getServerSocket() {
        return serverSocket;
    }

    private static void printToSocket() throws IOException, InterruptedException {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        InputStream inputStream = socket.getInputStream();
        System.setOut(new PrintStream(socket.getOutputStream()));
        printWriter.println(( float ) (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN + " | " + ConstantsFor.APP_NAME);
        printWriter.println("NEW SOCKET: " + socket.toString());
        Thread.sleep(new SecureRandom().nextInt(15));
        while(inputStream.available() > 0){
            byte[] bytes = new byte[3];
            int read = inputStream.read(bytes);
            if(!Arrays.toString(bytes).contains("-1, -8, 3")){
                printWriter.print(out);
            }
            else{
                printWriter.println(read);
                socket.close();
                setSocket(new Socket());
            }
        }
    }
}