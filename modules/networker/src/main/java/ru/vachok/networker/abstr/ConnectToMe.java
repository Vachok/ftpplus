// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr;


import java.io.IOException;
import java.net.Socket;


/**
 @since 10.05.2019 (12:35) */
public interface ConnectToMe {
    
    
    void runSocket() throws IOException;
    void accepSoc(Socket socket);
    void reconSock() throws IOException, InterruptedException, NullPointerException;
    void doCommand(String readLine) throws IOException, InterruptedException;
    void printToSocket() throws IOException;
}
