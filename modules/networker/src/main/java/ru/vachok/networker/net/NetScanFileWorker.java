// Copyright (c) all rights. http://networker.vachok.ru 2019.
package ru.vachok.networker.net;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
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
    
    @Contract(pure = true)
    public static NetScanFileWorker getI() {
        return NET_SCAN_FILE_WORKER;
    }
    
    /**
     Читает файлы из {@link DiapazonScan#editScanFiles()} в {@link Deque}
     <p>
 
     @return {@link Deque} of {@link String}, с именами девайсов онлайн.
     */
    public static Deque<InetAddress> getDequeOfOnlineDev() {
        Deque<InetAddress> retDeque = new ArrayDeque<>();
        Map<String, File> scanFiles = DiapazonScan.getScanFiles();
        
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
    
    private static List<InetAddress> readFilesLANToCollection(@NotNull File scanFile) {
        List<String> listOfIPAsStrings = FileSystemWorker.readFileToList(scanFile.toPath().toAbsolutePath().normalize().toString());
        Collections.sort(listOfIPAsStrings);
        throw new TODOException("List of INET Address 20.07.2019 (22:43)");
    }
}
