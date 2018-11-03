package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.System.out;


/**
 @since 03.11.2018 (23:51) */
public class MyServer implements Runnable {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = MyServer.class.getSimpleName();

    public static final Logger LOGGER = AppComponents.getLogger();

    private static MyServer myServer = new MyServer();

    private Socket socket;

    private int listenPort;

    public static MyServer getI() {
        return myServer;
    }

    /*Instances*/
    protected MyServer(int listenPort, Socket socket) {
        this.listenPort = listenPort;
        this.socket = socket;
    }

    private MyServer() {
    }

    @Override
    public void run() {
        try(ServerSocket serverSocket = new ServerSocket(ConstantsFor.LISTEN_PORT)){
            sock(serverSocket);
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void sock(ServerSocket serverSocket) throws IOException {
        while(true){
            socket = serverSocket.accept();
            accepSoc(socket);
            if(serverSocket.isClosed()){
                break;
            }
        }
    }

    private void accepSoc(Socket socket) throws IOException {
        try(Scanner scanner = new Scanner(System.in);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true)){
            System.setOut(new PrintStream(socket.getOutputStream()));
            if(scanner.hasNext()){
                while(socket.isConnected()){
                    StringBuilder f = new StringBuilder();
                    f.append("\n\n")
                        .append(( float ) (System.currentTimeMillis() - ConstantsFor.START_STAMP) / 1000 / 60)
                        .append(" APP RUNNING \n")
                        .append(ConstantsFor.APP_NAME)
                        .append("\n\n\n");
                    printWriter.println(f.toString());
                    printWriter.print(out);
                }
            }
            else{
                LOGGER.warn("No scanner");
            }
        }
        catch(IOException e){
            LoggerFactory.getLogger(SOURCE_CLASS).error(e.getMessage(), e);
        }
    }

    public void stop() {
        try{
            socket.close();
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
        Thread.currentThread().checkAccess();
        Thread.currentThread().interrupt();
    }
}