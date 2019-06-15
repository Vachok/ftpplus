// Copyright (c) all rights. http://networker.vachok.ru 2019.
package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 Файловые работы.
 
 @since 25.12.2018 (10:43) */
public class NetScanFileWorker implements Serializable {
    
    
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
     Читает файлы из {@link DiapazonScan#getScanFiles()} в {@link Deque}
     <p>
 
     @return {@link Deque} of {@link String}, с именами девайсов онлайн.
     */
    public static Deque<String> getDequeOfOnlineDev() {
        AppComponents.threadConfig().thrNameSet("ON");
        Deque<String> retDeque = new ArrayDeque<>();
        getMapOfScanFiles().forEach((fileName, srvFileX)->readFilesLANToCollection(srvFileX, retDeque));
        return retDeque;
    }
    
    private static Map<String, File> getMapOfScanFiles() {
        return DiapazonScan.getInstance().getScanFiles();
    }
    
    
    /**
     Чтение файлов {@code lan_*} и заполнение {@link Deque} строками <br>
     {@code retPath} = C:\Users\ikudryashov\IdeaProjects\ftpplus\modules\networker\lan_*.txt
     <p>
 
     @param srvFileX файл lan_* из корневой папки.
     @param retDeque обратная очередь, для наполнения.
     */
    private static Path readFilesLANToCollection(File srvFileX, Collection<String> retDeque) {
        Path retPath = Paths.get("");
        retPath = Paths.get(retPath.toAbsolutePath() + ConstantsFor.FILESYSTEM_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR);
        
        if (srvFileX.exists() && srvFileX.canRead()) {
            retDeque.addAll(FileSystemWorker.readFileToSet(srvFileX.toPath()));
            retPath = Paths.get(srvFileX.toPath().toAbsolutePath().toString().replace("." + ConstantsFor.FILESYSTEM_SEPARATOR, ConstantsFor.FILESYSTEM_SEPARATOR));
        }
        else {
            try {
                retPath = Files.createFile(srvFileX.toPath()).toAbsolutePath();
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
        return retPath.toAbsolutePath();
    }
}
