package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.DBMessenger;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static java.lang.System.err;
import static java.lang.System.out;


/**
 Телнет-сервер получения информации и ввода команд приложения.

 @since 03.11.2018 (23:51) */
@SuppressWarnings ({"resource", "IOResourceOpenedButNotSafelyClosed"})
public class MyServer extends Thread {

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = MyServer.class.getSimpleName();

    /**
     {@link AppComponents#getLogger(String)}
     */
    private static final Logger LOGGER = AppComponents.getLogger(MyServer.class.getSimpleName());

    /**
     <b>Single Instance</b>
     */
    private static final MyServer myServer = new MyServer();

    /**
     {@link DBMessenger}
     */
    private static final MessageToUser messageToUser = new MessageLocal();
    
    private static final int LPORT = Integer.parseInt(ConstantsFor.getAppProps().getProperty("LPORT", "9990"));

    /**
     <b>Сокет для клиента</b>
     <p>
     {@link #getSocket()} , {@link #setSocket(Socket)}
     */
    private static Socket socket = null;
    
    public static Socket getSocket() {
        return socket;
    }

    public static void setSocket(Socket socket) {
        MyServer.socket = socket;
    }

    /**
     @return instance
     */
    public static MyServer getI() {
        return myServer;
    }
    
    /**
     Сокет для сервера
     */
    @SuppressWarnings("CanBeFinal")
    private static ServerSocket serverSocket = null;
    
    /**
     {@link #myServer}
     */
    private MyServer() {
        AppComponents.threadConfig().thrNameSet("tport:" + LPORT);
    }

    static {
        try{
            serverSocket = new ServerSocket(LPORT);
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     <b>Обработчик ввода из Telnet</b>
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
        printStream.println((System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN + " min up | " + ConstantsFor.APPNAME_WITHMINUS);
        printStream.println(Thread.activeCount() + " active THREADS");
        printStream.println(ConstantsFor.getMemoryInfo());
        printStream.println("Press Enter or enter command:\n");
        String readLine = bufferedReader.readLine();
        makeDeal(readLine);
    }

    /**
     Упрощение {@link #reconSock()}

     @param readLine {@link #reconSock()}
     @throws IOException          socket and files
     @throws InterruptedException sleeping threads
     */
    private static void makeDeal(String readLine) throws IOException, InterruptedException {
        if(readLine.toLowerCase().contains("exit")){
            FileSystemWorker.delTemp();
            MyServer.socket.close();
            System.exit(ConstantsFor.CODE_USEREXIT);
        }
        if(readLine.toLowerCase().contains("help")){
            MyServer.ifHelp();
        }
        if(readLine.toLowerCase().contains("con")){
            MyServer.ifCon();
        }
        if(readLine.toLowerCase().contains("thread")){
            MyServer.ifThread();
        }
        if(readLine.toLowerCase().contains(ConstantsNet.ATT_NETSCAN)){
            MyServer.ifNetScan();
        }
        if(readLine.equalsIgnoreCase("shutdown")){
            Runtime.getRuntime().exec(ConstantsFor.COM_SHUTDOWN_P_F);
        }
        if (readLine.equalsIgnoreCase(ConstantsFor.COM_REBOOT)) {
            Runtime.getRuntime().exec("shutdown /r /f");
        }
        else{
            MyServer.printToSocket();
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
        long millis = TimeUnit.SECONDS.toMillis(new SecureRandom().nextInt(( int ) ConstantsFor.MY_AGE));
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
        NetScannerSvc i = AppComponents.netScannerSvc();
        try{
            String thePc = i.getThePc();
            printToSocket();
            printWriter.println(thePc);
        }
        catch(Exception e){
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
        printWriter.println((float) (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / ConstantsFor.ONE_HOUR_IN_MIN + " | " + ConstantsFor.APPNAME_WITHMINUS);
        printWriter.println("NEW SOCKET: " + socket.toString());
        while(inputStream.available() > 0){
            byte[] bytes = new byte[3];
            int read = inputStream.read(bytes);
            if(!Arrays.toString(bytes).contains("-1, -8, 3")){
                printWriter.print(out);
            }
            else{
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
        try{
            runSocket();
        }
        catch(IOException e){
            messageToUser.errorAlert("MyServer", "run", e.getMessage());
            FileSystemWorker.error("MyServer.run", e);
        }
    }

    /**
     <b>Создаёт {@link ServerSocket}</b>
     <p>
     <i>{@link #run()}</i>

     @throws IOException {@link ServerSocket} accept() , .getReuseAddress()
     */
    private static void runSocket() throws IOException {
        while(true){
            socket = serverSocket.accept();
            accepSoc(socket);
            if(socket.isClosed()){
                System.setOut(err);
                String msg = serverSocket.getReuseAddress() + " getReuseAddress";
                messageToUser.warn(msg);
                break;
            }
            if(!socket.isConnected()){
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

        try(Scanner scanner = new Scanner(System.in);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true)){
            System.setOut(new PrintStream(socket.getOutputStream()));
            f.append("\n\n")
                .append(( float ) (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / 60).append(" APP RUNNING \n");
            printWriter.println(f.toString());
            if(scanner.hasNext()){
                while(socket.isConnected()){
                    printWriter.print(out);
                }
            }
        }
        catch(IOException e){
            messageToUser.errorAlert("MyServer", "accepSoc", e.getMessage());
            FileSystemWorker.error("MyServer.accepSoc", e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MyServer{");
        sb.append("messageToUser=").append(messageToUser);
        sb.append(", myServer=").append(myServer);
        sb.append(", serverSocket=").append(serverSocket);
        sb.append(", socket=").append(socket);
        sb.append(", SOURCE_CLASS='").append(SOURCE_CLASS).append('\'');
        sb.append('}');
        return sb.toString();
    }
}