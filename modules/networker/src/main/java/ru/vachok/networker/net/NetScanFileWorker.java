// Copyright (c) all rights. http://networker.vachok.ru 2019.
package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 Файловые работы.
 <p>
 @see ru.vachok.networker.net.NetScanFileWorkerTest
 @since 25.12.2018 (10:43) */
public class NetScanFileWorker implements Serializable {
    
    
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static final NetScanFileWorker NET_SCAN_FILE_WORKER = new NetScanFileWorker();
    
    private static MessageToUser messageToUser = new MessageLocal(NetScanFileWorker.class.getSimpleName());
    
    private long lastStamp = System.currentTimeMillis();
    
    public long getLastStamp() {
        return lastStamp;
    }
    
    public void setLastStamp(long lastStamp) {
        this.lastStamp = lastStamp;
        Preferences pref = AppComponents.getUserPref();
        pref.put("NetScanFileWorker.lastStamp", new Date(lastStamp).toString());
        try {
            pref.sync();
        }
        catch (BackingStoreException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    public static NetScanFileWorker getI() {
        return NET_SCAN_FILE_WORKER;
    }
    
    /**
     Читает файлы из {@link DiapazonScan#editScanFiles()} в {@link Deque}
     <p>
 
     @return {@link Deque} of {@link String}, с именами девайсов онлайн.
     */
    public static Deque<String> getDequeOfOnlineDev() {
        Deque<String> retDeque = new ArrayDeque<>();
        Map<String, File> scanFiles = DiapazonScan.getInstance().editScanFiles();
        scanFiles.forEach((scanFileName, scanFile)->{
            retDeque.addAll(readFilesLANToCollection(scanFile));
            System.out.println("Scan file added to WEB model: " + scanFileName);
        });
        return retDeque;
    }
    
    public void setLastStamp(long millis, String address) {
        setLastStamp(millis);
        Preferences pref = AppComponents.getUserPref();
        pref.put("lastIP", address);
        try {
            pref.sync();
        }
        catch (BackingStoreException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    private static List<String> readFilesLANToCollection(File scanFile) {
        List<String> fileAsList = new ArrayList<>();
        
        if (scanFile.exists() && scanFile.canRead()) {
            fileAsList.addAll(FileSystemWorker.readFileToList(scanFile.toPath().toAbsolutePath().normalize().toString()));
        }
        else {
            try {
                Path newScanFile = Files.createFile(scanFile.toPath()).toAbsolutePath();
                fileAsList.add(newScanFile.toAbsolutePath().normalize() + " created at " + LocalDateTime.now());
            }
            catch (IOException e) {
                messageToUser.error(FileSystemWorker.error(NetScanFileWorker.class.getSimpleName() + ".readFilesLANToCollection", e));
            }
        }
        Collections.sort(fileAsList);
        return fileAsList;
    }
}
