package ru.vachok.networker.services;


import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


/**
 @since 03.11.2018 (23:51) */
public class MyServer implements Runnable {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = MyServer.class.getSimpleName();

    @Override
    public void run() {
        try(ServerSocket serverSocket = new ServerSocket(9999);){
            Socket socket = serverSocket.accept();
            accepSoc(socket);
        }
        catch(IOException e){
            System.setIn(System.in);
            System.setOut(System.out);
        }

    }

    private void accepSoc(Socket socket) {
        try(
            Scanner scanner = new Scanner(socket.getInputStream());
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true)){
            System.setIn(socket.getInputStream());
            System.setOut(new PrintStream(socket.getOutputStream()));
            while(true){
                printWriter.println(System.out);
                if(scanner.nextLine().toLowerCase().contains("off")){
                    throw new UnsupportedOperationException();
                }
            }
        }
        catch(IOException | UnsupportedOperationException e){
            LoggerFactory.getLogger(SOURCE_CLASS).error(e.getMessage(), e);
            System.setOut(System.out);
            System.setIn(System.in);
        }
    }
}