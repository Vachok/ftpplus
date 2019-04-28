// Copyright (c) all rights. http://networker.vachok.ru 2019.
package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 Файловые работы.
 
 @since 25.12.2018 (10:43) */
public class NetScanFileWorker implements Serializable {
    
    
    private static final ConcurrentMap<String, File> SCAN_FILES = new ConcurrentHashMap<>();
    
    private static final NetScanFileWorker NET_SCAN_FILE_WORKER = new NetScanFileWorker();
    
    private static MessageToUser messageToUser = new MessageLocal(NetScanFileWorker.class.getSimpleName());
    
    private long lastStamp = System.currentTimeMillis();
    
    
    public long getLastStamp() {
        return lastStamp;
    }
    
    void setLastStamp(long lastStamp) {
        this.lastStamp = lastStamp;
    }
    
    public ConcurrentMap<String, File> getScanFiles() {
        for (File f : Objects.requireNonNull(new File(".").listFiles())) {
            if (f.getName().contains("lan_")) {
                SCAN_FILES.putIfAbsent(f.getName(), f);
            }
        }
        if (SCAN_FILES.size() == 7) {
            return SCAN_FILES;
        }
        else {
            SCAN_FILES.putIfAbsent(ConstantsNet.FILENAME_NEWLAN210, new File(ConstantsNet.FILENAME_NEWLAN210));
            SCAN_FILES.putIfAbsent(ConstantsNet.FILENAME_NEWLAN220, new File(ConstantsNet.FILENAME_NEWLAN220));
            SCAN_FILES.putIfAbsent(ConstantsNet.FILENAME_OLDLANTXT0, new File(ConstantsNet.FILENAME_OLDLANTXT0));
            SCAN_FILES.putIfAbsent(ConstantsNet.FILENAME_OLDLANTXT1, new File(ConstantsNet.FILENAME_OLDLANTXT1));
            SCAN_FILES.putIfAbsent(ConstantsNet.FILENAME_SERVTXT_10SRVTXT, new File(ConstantsNet.FILENAME_SERVTXT_10SRVTXT));
            SCAN_FILES.putIfAbsent(ConstantsNet.FILENAME_SERVTXT_21SRVTXT, new File(ConstantsNet.FILENAME_SERVTXT_21SRVTXT));
            SCAN_FILES.putIfAbsent(ConstantsNet.FILENAME_SERVTXT_31SRVTXT, new File(ConstantsNet.FILENAME_SERVTXT_31SRVTXT));
            return SCAN_FILES;
        }
    }
    
    public static NetScanFileWorker getI() {
        return NET_SCAN_FILE_WORKER;
    }
    
    
    /**
     @return {@link Deque} of {@link String}, с именами девайсов онлайн.
     */
    Deque<String> getListOfOnlineDev() {
        AppComponents.threadConfig().thrNameSet("ON");
        Deque<String> retDeque = new ArrayDeque<>();
        Set<String> fileNameSet = SCAN_FILES.keySet();
        SCAN_FILES.forEach((fileName, srvFileX)->messageToUser.info(getClass().getSimpleName(), "list onLine", " = " + fileWrk(srvFileX, retDeque)));
        return retDeque;
    }
    
    
    /**
     @param srvFileX файл lan_* из корневой папки.
     @param retDeque обратная очередь, для наполнения.
     */
    private Path fileWrk(File srvFileX, Collection<String> retDeque) {
        Path retPath = Paths.get("\\lan\\");
        if (srvFileX.exists() && srvFileX.canRead()) {
            retDeque.addAll(FileSystemWorker.readFileToList(srvFileX.getAbsolutePath()));
            retPath = srvFileX.toPath();
        }
        else {
            try {
                retPath = Files.createFile(srvFileX.toPath());
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
        return retPath;
    }
}
