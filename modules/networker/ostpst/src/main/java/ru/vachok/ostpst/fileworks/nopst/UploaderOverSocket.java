// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks.nopst;


import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.ostpst.fileworks.FileWorker;
import ru.vachok.ostpst.utils.TFormsOST;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.Scanner;
import java.util.prefs.BackingStoreException;


/**
 @since 31.05.2019 (13:28) */
public class UploaderOverSocket implements FileWorker {
    
    
    private ByteBuffer readFile;
    
    private String writeFileName;
    
    private ServerSocket serverSocket;
    
    private Socket socket;
    
    public UploaderOverSocket() {
        this.writeFileName = "dn.soc";
        initMethod(writeFileName);
    }
    
    @Override public String chkFile() {
        throw new IllegalComponentStateException("31.05.2019 (13:28)");
    }
    
    @Override public String clearCopy() {
        PREF_MAP.clear();
        try {
            PREFERENCES_USER_ROOT.clear();
        }
        catch (BackingStoreException e) {
            new DBRegProperties("ostpst-" + getClass().getSimpleName()).delProps();
        }
        initMethod(writeFileName);
        return "Clearing previous copy attempt...";
    }
    
    @Override public long continuousCopy() {
        int port = new Random().nextInt(60000);
        if (port < 2000) {
            port = 2500;
        }
        try {
            System.out.println(port);
            this.serverSocket = new ServerSocket(port);
            while (!serverSocket.isClosed()) {
                workWithSocket();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return port;
    }
    
    @Override public String showCurrentResult() {
        throw new IllegalComponentStateException("31.05.2019 (13:30)");
    }
    
    @Override public String saveAndExit() {
        throw new IllegalComponentStateException("31.05.2019 (13:46)");
    }
    
    @Override public boolean processNewCopy() {
        clearCopy();
        return true;
    }
    
    private void workWithSocket() throws IOException {
        System.setProperty("encoding", "UTF8");
        this.socket = serverSocket.accept();
        String fromArray = new TFormsOST().fromArray(System.getProperties());
        try (InputStream inputStream = socket.getInputStream();
             SocketChannel channel = socket.getChannel();
             Scanner scanner = new Scanner(inputStream);
        ) {
            String line = scanner.nextLine();
            try (OutputStream outputStream = socket.getOutputStream();
                 PrintStream printStream = new PrintStream(outputStream, true)
            ) {
                printStream.println("Hello");
                printStream.println(line.toUpperCase());
                printStream.println("fromArray = " + fromArray);
                System.exit(222);
            }
        }
        catch (IOException e) {
            socket.close();
        }
    }
}
