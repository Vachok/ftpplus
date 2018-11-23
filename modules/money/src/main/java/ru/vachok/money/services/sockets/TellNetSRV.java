package ru.vachok.money.services.sockets;


import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.config.AppComponents;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 Телнет-сервер получения информации и ввода команд приложения.

 @since 03.11.2018 (23:51) */
@Component
public class TellNetSRV implements Runnable {

    /**
     {@link AppComponents#getLogger()}
     */
    public static final Logger LOGGER = AppComponents.getLogger();

    private static Socket socket;

    static Socket getSocket() {
        return TellNetSRV.socket;
    }

    static void setSocket(Socket socket) {
        TellNetSRV.socket = socket;
    }

    @Override
    public void run() {
        try(ServerSocket serverSocket = new ServerSocket(ConstantsFor.LISTEN_PORT)){
            while(true){
                TellNetSRV.socket = serverSocket.accept();
                String msg = socket.toString();
                LOGGER.warn(msg);
            }
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }
}