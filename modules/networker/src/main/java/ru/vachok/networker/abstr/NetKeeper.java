// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr;


import ru.vachok.networker.exe.schedule.ScanFilesWorker;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public abstract class NetKeeper implements Keeper {
    
    
    private static final Map<String, File> SCAN_FILES = new ConcurrentHashMap<>();
    
    private static final List<String> CURRENT_SCAN_LIST = new ArrayList<>();
    
    private static final List<File> CURRENT_SCAN_FILES = new ArrayList<>();
    
    private static MessageToUser messageToUser = new MessageLocal(NetKeeper.class.getSimpleName());
    
    public static Map<String, File> getScanFiles() {
        return SCAN_FILES;
    }
    
    public abstract Deque<InetAddress> getOnlineDevicesInetAddress();
    
    public static List<String> getCurrentScanLists() {
        if (SCAN_FILES.size() != 9) {
            ScanFilesWorker.makeFilesMap();
        }
        for (File file : SCAN_FILES.values()) {
            CURRENT_SCAN_LIST.addAll(FileSystemWorker.readFileToList(file.getAbsolutePath()));
        }
        return Collections.unmodifiableList(CURRENT_SCAN_LIST);
    }
    
    public static List<File> getCurrentScanFiles() {
        if (SCAN_FILES.size() != 9) {
            ScanFilesWorker.makeFilesMap();
        }
        List<File> retList = new ArrayList<>();
        for (File listFile : SCAN_FILES.values()) {
            if (listFile.exists()) {
                retList.add(listFile);
            }
            else {
                try {
                    Files.createFile(listFile.toPath());
                }
                catch (IOException e) {
                    messageToUser.error(MessageFormat.format("ScanFilesWorker.getCurrentScanFiles: {0}, ({1})", e.getMessage(), e.getClass().getName()));
                }
            }
        }
        return retList;
    }
}
