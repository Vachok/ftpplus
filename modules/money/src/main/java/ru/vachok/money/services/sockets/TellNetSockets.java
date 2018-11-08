package ru.vachok.money.services.sockets;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.config.AppComponents;

import java.io.*;
import java.net.Socket;


@Service
public class TellNetSockets {

    private static final Logger LOGGER = AppComponents.getLogger();

    private Socket socket;

    private TellNetSRV tellNetSRV;

    @Autowired
    public TellNetSockets(TellNetSRV tellNetSRV) {
        this.tellNetSRV = tellNetSRV;
        this.socket = TellNetSRV.getSocket();
    }

    public void consoleToSocket() {
        TellNetSRV.setSocket(new Socket());
        try (InputStream inputStream = new FileInputStream("build.gradle");
             OutputStream outputStream = socket.getOutputStream()) {
            PrintWriter printWriter = new PrintWriter(outputStream, true);
            byte[] bytes = new byte[ConstantsFor.MEGABYTE];
            while (inputStream.available() > 0) {
                int read = inputStream.read(bytes, 0, inputStream.available());
                printWriter.println(new String(bytes));
            }

        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        try {
            String msg = socket.toString() + " " + socket.isConnected();
            msg = msg + "\n" + tellNetSRV.toString();
            return msg;
        } catch (NullPointerException e) {
            return e.getMessage();
        }
    }
}
