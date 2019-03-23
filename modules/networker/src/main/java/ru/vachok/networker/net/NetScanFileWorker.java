// Copyright (c) all rights. http://networker.vachok.ru 2019.
package ru.vachok.networker.net;



import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Файловые работы.
 *
 * @since 25.12.2018 (10:43)
 */
public class NetScanFileWorker {


    static final ConcurrentMap<String, File> srvFiles = new ConcurrentHashMap<>();

    private static NetScanFileWorker ourInst = new NetScanFileWorker(srvFiles);

    private static MessageToUser messageToUser = new MessageLocal(NetScanFileWorker.class.getSimpleName());

    private long lastStamp = System.currentTimeMillis();


    public NetScanFileWorker( ConcurrentMap<String, File> mapFiles ) {
    }


    public long getLastStamp() {
        return lastStamp;
    }


    public ConcurrentMap<String, File> getFilesScan() {
        return srvFiles;
    }


    void setLastStamp(long lastStamp) {
        this.lastStamp = lastStamp;
    }


    public static NetScanFileWorker getI() {
        return ourInst;
    }


    Deque<String> getListOfOnlineDev() throws IOException {
        AppComponents.threadConfig().thrNameSet("ON_Dq");

        String classMeth = "NetScanFileWorker.getListOfOnlineDev";
        String titleMsg = "retDeque.size()";
        Deque<String> retDeque = new ArrayDeque<>();

        File newLanLastScan200 = srvFiles.get("200");
        File newLanLastScan210 = srvFiles.get("210");
        File oldLanLastScan0 = srvFiles.get("old0");
        File oldLanLastScan1 = srvFiles.get("old1");

        String msg = newLanLastScan200.getAbsolutePath() + ";\n" + newLanLastScan210.getAbsolutePath() + ";\n" + oldLanLastScan0.getAbsolutePath() + ", " + oldLanLastScan1.getAbsolutePath() +
            ";\n" + new TForms().fromArray(srvFiles, false) +
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
        if (srvFiles.size() == 8) {
            srvFiles.forEach(( id , srvFileX ) -> fileWrk(srvFileX , titleMsg , retDeque));
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
