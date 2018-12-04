package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.VersionInfo;
import ru.vachok.networker.services.DBMessenger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.System.*;


/**
 Телнет-сервер получения информации и ввода команд приложения.

 @since 03.11.2018 (23:51) */
public class MyServer extends Thread {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = MyServer.class.getSimpleName();

    /**
     {@link AppComponents#getLogger()}
     */
    public static final Logger LOGGER = AppComponents.getLogger();

    /**
     <b>Single Instance</b>
     */
    private static MyServer myServer = new MyServer();

    /**
     <b>Сокет для сервера</b>

     @see ConstantsFor#LISTEN_PORT
     */
    private static ServerSocket serverSocket;

    /**
     {@link DBMessenger}
     */
    private static MessageToUser messageToUser = new DBMessenger();

    /**
     <b>Сокет для клиента</b>
     <p>
     {@link #getSocket()} , {@link #setSocket(Socket)}
     */
    private static Socket socket;

    private static final Properties propsToSave = ConstantsFor.getPROPS();

    /**
     <i>{@link SystemTrayHelper#recOn()}</i>

     @return {@link Socket}
     */
    public static Socket getSocket() {
        return socket;
    }

    /**
     <i>{@link SystemTrayHelper#recOn()}</i>

     @param socket подключения для клиента
     */
    public static void setSocket(Socket socket) {
        MyServer.socket = socket;
    }

    /**
     @return instance
     */
    public static MyServer getI() {
        return myServer;
    }

    /*Instances*/

    /**
     {@link #myServer}
     */
    private MyServer() {
    }

    static {
        try {
            serverSocket = new ServerSocket(ConstantsFor.LISTEN_PORT);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     <b>Обработчик ввода из Telnet</b>
     <p>
     <i>{@link SystemTrayHelper#recOn()}</i>
     <p>
     Слушает первую строку ввода из Telnet. <br> Обращается в {@link #printToSocket()}

     @throws IOException          {@link InputStream} ; {@link Socket} ; {@link #printToSocket()}
     @throws InterruptedException help и thread
     */
    public static void reconSock() throws IOException, InterruptedException, NullPointerException {
        Socket socket = serverSocket.accept();
        setSocket(socket);
        InputStream inputStream = socket.getInputStream();
        PrintStream printStream = new PrintStream(socket.getOutputStream());
        InputStreamReader reader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(reader);
        try{
            printStream.println(new VersionInfo().toString());
        }
        catch(RuntimeException e){
            LOGGER.warn(e.getMessage());
        }
        printStream.println((System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN + " min up | " + ConstantsFor.APP_NAME);
        printStream.println(Thread.activeCount() + " active THREADS");
        printStream.println("Press Enter or enter command:\n");
        String readLine = bufferedReader.readLine();
        if (readLine.toLowerCase().contains("exit")) {
            IntoApplication.delTemp();
            ConstantsFor.saveProps(propsToSave);
            socket.close();
            System.exit(ConstantsFor.USER_EXIT);
        }
        if (readLine.toLowerCase().contains("help")) {
            ifHelp();
        }
        if (readLine.toLowerCase().contains("con")) {
            ifCon();
        }
        if (readLine.toLowerCase().contains("thread")) {
            ifThread();
        }
        if (readLine.toLowerCase().contains("netscan")) {
            ifNetScan();
        }
        if (readLine.equalsIgnoreCase("shutdown")) {
            Runtime.getRuntime().exec("shutdown /p /f");
        }
        if (readLine.equalsIgnoreCase("reboot")) {
            Runtime.getRuntime().exec("shutdown /r /f");
        } else {
            printToSocket();
        }
    }

    /**
     <b>Help if</b>

     @throws IOException          {@link #printToSocket()} , {@link #socket}.getOutputStream()
     @throws InterruptedException {@link Thread}.sleep()
     */
    private static void ifHelp() throws IOException, InterruptedException {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        printWriter.println("exit - close connection and exit app");
        printWriter.println("con - switch out to System.err and close connection");
        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        printToSocket();
    }

    /**
     <b>Con if</b>

     @throws IOException {@link #socket}.close()
     */
    private static void ifCon() throws IOException {
        System.setOut(err);
        socket.close();
        setSocket(new Socket());
    }

    /**
     <b>Thread if</b>

     @throws IOException          {@link #printToSocket()} , {@link #socket}.getOutputStream()
     @throws InterruptedException {@link Thread}.sleep()
     */
    private static void ifThread() throws IOException, InterruptedException {
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

    /**
     <b>Netscan if</b>

     @throws IOException {@link #socket}.close()
     */
    private static void ifNetScan() throws IOException {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        NetScannerSvc i = NetScannerSvc.getI();
        try {
            String thePc = i.getThePc();
            printToSocket();
            printWriter.println(thePc);
        } catch (Exception e) {
            System.setOut(err);
            messageToUser.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().fromArray(e, false));
            socket.close();
            socket = new Socket();

        }
    }

    /**
     <i>{@link #reconSock()}</i>
     <p>
     Поддерживает соединение и возможность reconnect <br> {@code while(inputStream.available()>0)}

     @throws IOException {@link InputStream} из {@link Socket}
     */
    private static void printToSocket() throws IOException {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        InputStream inputStream = socket.getInputStream();
        System.setOut(new PrintStream(socket.getOutputStream()));
        printWriter.println((float) (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN + " | " + ConstantsFor.APP_NAME);
        printWriter.println("NEW SOCKET: " + socket.toString());
        printWriter.println("APP INFO: " + new VersionInfo().toString());
        while (inputStream.available() > 0) {
            byte[] bytes = new byte[3];
            int read = inputStream.read(bytes);
            if (!Arrays.toString(bytes).contains("-1, -8, 3")) {
                printWriter.print(out);
            } else {
                printWriter.println(read);
                System.setOut(err);
                socket.close();
                setSocket(new Socket());
            }
        }
    }

    /**
     {@link #runSocket()}
     */
    @Override
    public void run() {
        try {
            runSocket();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     <b>Создаёт {@link ServerSocket}</b>
     <p>
     <i>{@link #run()}</i>

     @throws IOException {@link ServerSocket} accept() , .getReuseAddress()
     */
    private static void runSocket() throws IOException {
        while (true) {
            socket = serverSocket.accept();
            accepSoc(socket);
            if (socket.isClosed()) {
                System.setOut(err);
                String msg = serverSocket.getReuseAddress() + " getReuseAddress";
                LOGGER.warn(msg);
                break;
            }
            if (!socket.isConnected()) {
                System.setOut(err);
            }
        }
    }

    /**
     <b>Первоначальное подключение</b>
     <p>
     <i> {@link #runSocket()} </i>

     @param socket {@link Socket} для подключившегося клиента
     */
    private static void accepSoc(Socket socket) {
        StringBuilder f = new StringBuilder();
        try (Scanner scanner = new Scanner(System.in);
             PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true)) {
            System.setOut(new PrintStream(socket.getOutputStream()));
            f.append("\n\n")
                .append((float) (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / 60)
                .append(" APP RUNNING \n")
                .append(ConstantsFor.APP_NAME)
                .append("\n\n\n")
                .append(new Date())
                .append(" build ")
                .append(new VersionInfo().toString());
            printWriter.println(f.toString());
            if (scanner.hasNext()) {
                while (socket.isConnected()) {
                    printWriter.print(out);
                }
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(SOURCE_CLASS).error(e.getMessage(), e);
        }
    }
}