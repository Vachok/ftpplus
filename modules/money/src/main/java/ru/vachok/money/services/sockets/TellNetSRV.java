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

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = TellNetSRV.class.getSimpleName();

    private static Socket socket;

    public static Socket getSocket() {
        return TellNetSRV.socket;
    }

    public static void setSocket(Socket socket) {
        TellNetSRV.socket = socket;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(ConstantsFor.LISTEN_PORT)) {
            while (true) {
                TellNetSRV.socket = serverSocket.accept();
                String msg = socket.toString();
                LOGGER.warn(msg);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}