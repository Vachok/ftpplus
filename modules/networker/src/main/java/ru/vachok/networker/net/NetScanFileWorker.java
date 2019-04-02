// Copyright (c) all rights. http://networker.vachok.ru 2019.
package ru.vachok.networker.net;



import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Файловые работы.
 *
 * @since 25.12.2018 (10:43)
 */
public class NetScanFileWorker implements Serializable {
    
    
    private static final ConcurrentMap<String, File> SRV_FILES = new ConcurrentHashMap<>();
    
    private static final NetScanFileWorker NET_SCAN_FILE_WORKER = new NetScanFileWorker();

    private static MessageToUser messageToUser = new MessageLocal(NetScanFileWorker.class.getSimpleName());

    private long lastStamp = System.currentTimeMillis();
    
    
    public NetScanFileWorker() {
    }


    public long getLastStamp() {
        return lastStamp;
    }
    
    
    public ConcurrentMap<String, File> getSrvFiles() {
        return SRV_FILES;
    }


    void setLastStamp(long lastStamp) {
        this.lastStamp = lastStamp;
    }


    public static NetScanFileWorker getI() {
        return NET_SCAN_FILE_WORKER;
    }


    Deque<String> getListOfOnlineDev() throws IOException {
        AppComponents.threadConfig().thrNameSet("ON_Dq");

        String classMeth = "NetScanFileWorker.getListOfOnlineDev";
        String titleMsg = "retDeque.size()";
        Deque<String> retDeque = new ArrayDeque<>();
    
        File newLanLastScan200 = SRV_FILES.get("200");
        File newLanLastScan210 = SRV_FILES.get("210");
        File oldLanLastScan0 = SRV_FILES.get("old0");
        File oldLanLastScan1 = SRV_FILES.get("old1");

        String msg = newLanLastScan200.getAbsolutePath() + ";\n" + newLanLastScan210.getAbsolutePath() + ";\n" + oldLanLastScan0.getAbsolutePath() + ", " + oldLanLastScan1.getAbsolutePath() +
            ";\n" + new TForms().fromArray(SRV_FILES, false) +
            ";\nCreated by " + getClass().getSimpleName();

        if (newLanLastScan200.exists() && newLanLastScan210.exists()) {
            retDeque.addAll(FileSystemWorker.readFileToList(newLanLastScan200.getAbsolutePath()));
            retDeque.addAll(FileSystemWorker.readFileToList(newLanLastScan210.getAbsolutePath()));
            messageToUser.info(classMeth, titleMsg, " = " + retDeque.size());
        }
        else {
            boolean newFile = newLanLastScan200.createNewFile();
            boolean newFile1 = newLanLastScan210.createNewFile();
            msg = newFile + " " + msg;
            msg = newFile1 + " " + msg;
        }
        if (oldLanLastScan0.exists() && oldLanLastScan1.exists()) {
            retDeque.addAll(FileSystemWorker.readFileToList(oldLanLastScan0.getAbsolutePath()));
            retDeque.addAll(FileSystemWorker.readFileToList(oldLanLastScan1.getAbsolutePath()));
        }
        else {
            boolean oldLanLastScanNewFile0 = oldLanLastScan0.createNewFile();
            boolean oldLanLastScanNewFile1 = oldLanLastScan1.createNewFile();
            msg = oldLanLastScanNewFile0 + " " + msg;
            msg = oldLanLastScanNewFile1 + " " + msg;
        }
        if (SRV_FILES.size() == 8) {
            SRV_FILES.forEach((id, srvFileX)->fileWrk(srvFileX, titleMsg, retDeque));
        }
        else {
            messageToUser.info(msg + " " + retDeque.size(), "positions] [Returns:", "java.util.Deque<java.lang.String>");
        }
        return retDeque;
    }


    private void fileWrk(File srvFileX, String titleMsg, Deque<String> retDeque) {
        String classMeth = "NetScanFileWorker.fileWrk";

        if (srvFileX.exists() && srvFileX.canRead()) {
            retDeque.addAll(FileSystemWorker.readFileToList(srvFileX.getAbsolutePath()));
            messageToUser.info(classMeth, titleMsg, " = " + retDeque.size());
        }
        else {
            boolean srvScanFile = false;
            try {
                srvScanFile = srvFileX.createNewFile();
            }
            catch (IOException e) {
                messageToUser.errorAlert("NetScanFileWorker", "getListOfOnlineDev", e.getMessage());
            }
            messageToUser.info(srvFileX.getAbsolutePath(), " is new file? ", new StringBuilder().append(" = ").append(srvScanFile).toString());
        }
    }
}
