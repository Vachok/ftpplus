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
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 Файловые работы.
 
 @since 25.12.2018 (10:43) */
public class NetScanFileWorker implements Serializable {
    
    
    private static final ConcurrentMap<String, File> SRV_FILES = new ConcurrentHashMap<>();
    
    private static final NetScanFileWorker NET_SCAN_FILE_WORKER = new NetScanFileWorker();
    
    private static MessageToUser messageToUser = new MessageLocal(NetScanFileWorker.class.getSimpleName());
    
    private long lastStamp = System.currentTimeMillis();
    
    
    public long getLastStamp() {
        return lastStamp;
    }
    
    void setLastStamp(long lastStamp) {
        this.lastStamp = lastStamp;
    }
    
    public ConcurrentMap<String, File> getSrvFiles() {
        for (File f : new File(".").listFiles()) {
            if (f.getName().contains("lan_")) {
                SRV_FILES.putIfAbsent(f.getName(), f);
            }
        }
        if (SRV_FILES.size() == 8) {
            return SRV_FILES;
        }
        else {
            SRV_FILES.putIfAbsent(ConstantsNet.FILENAME_NEWLAN210, new File(ConstantsNet.FILENAME_NEWLAN210));
            SRV_FILES.putIfAbsent(ConstantsNet.FILENAME_NEWLAN200210, new File(ConstantsNet.FILENAME_NEWLAN200210));
            SRV_FILES.putIfAbsent(ConstantsNet.FILENAME_OLDLANTXT0, new File(ConstantsNet.FILENAME_OLDLANTXT0));
            SRV_FILES.putIfAbsent(ConstantsNet.FILENAME_OLDLANTXT1, new File(ConstantsNet.FILENAME_OLDLANTXT1));
            SRV_FILES.putIfAbsent(ConstantsNet.FILENAME_SERVTXT_11SRVTXT, new File(ConstantsNet.FILENAME_SERVTXT_11SRVTXT));
            SRV_FILES.putIfAbsent(ConstantsNet.FILENAME_SERVTXT_21SRVTXT, new File(ConstantsNet.FILENAME_SERVTXT_21SRVTXT));
            SRV_FILES.putIfAbsent(ConstantsNet.FILENAME_SERVTXT_31SRVTXT, new File(ConstantsNet.FILENAME_SERVTXT_31SRVTXT));
            SRV_FILES.putIfAbsent(ConstantsNet.FILENAME_SERVTXT_41SRVTXT, new File(ConstantsNet.FILENAME_SERVTXT_41SRVTXT));
            return SRV_FILES;
        }
    }
    
    public static NetScanFileWorker getI() {
        return NET_SCAN_FILE_WORKER;
    }
    
    
    Deque<String> getListOfOnlineDev() throws IOException {
        AppComponents.threadConfig().thrNameSet("ON");
        
        String classMeth = "NetScanFileWorker.getListOfOnlineDev";
        String titleMsg = "retDeque.size()";
        Deque<String> retDeque = new ArrayDeque<>();
        Set<String> fileNameSet = SRV_FILES.keySet();
        
        if (SRV_FILES.size() == 8) {
            SRV_FILES.forEach((fileName, srvFileX)->fileWrk(srvFileX, titleMsg, retDeque));
        }
        else {
            messageToUser.error(SRV_FILES.size() + " is SRV_FILES!");
        }
        return retDeque;
    }
    
    
    private void fileWrk(File srvFileX, String titleMsg, Collection<String> retDeque) {
        if (srvFileX.exists() && srvFileX.canRead()) {
            retDeque.addAll(FileSystemWorker.readFileToList(srvFileX.getAbsolutePath()));
        }
        else {
            boolean srvScanFile = false;
            try {
                srvScanFile = srvFileX.createNewFile();
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
    }
}
