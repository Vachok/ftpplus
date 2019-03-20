// Copyright (c) all rights. http://networker.vachok.ru 2019.

/*
 * Copyright (c) 2019.
 */

/*
 * Copyright (c) 2019.
 */

package ru.vachok.networker.net;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Файловые работы.
 *
 * @since 25.12.2018 (10:43)
 */
@Scope(ConstantsFor.SINGLETON)
@Component
class NetScanFileWorker {
    
    
    private static NetScanFileWorker ourInst = new NetScanFileWorker();
    
    private static MessageToUser messageToUser = new MessageLocal();
    
    private long lastStamp = System.currentTimeMillis();
    
    /**
     * {@link DiapazonedScan#scanNew()}
     */
    private File newLanLastScan200 = new File(ConstantsNet.FILENAME_AVAILABLELAST200210TXT);
    
    private File newLanLastScan210 = new File(ConstantsNet.FILENAME_AVAILABLELAST210220TXT);
    
    private List<File> srvFiles = new ArrayList<>(4);
    
    private ConcurrentMap<File, Long> filesScan = new ConcurrentHashMap<>();
    
    private File oldLanLastScan0 = new File(ConstantsNet.FILENAME_OLDLANTXT0);
    
    private File oldLanLastScan1 = new File(ConstantsNet.FILENAME_OLDLANTXT1);
    
    private NetScanFileWorker() {
    }
    
    public void setSrvFiles(List<File> srvFiles) {
        messageToUser.info("NetScanFileWorker.setSrvFiles", "srvFiles.size()", " = " + srvFiles.size());
        if (srvFiles.size() < 5) {
            this.srvFiles = srvFiles;
        } else {
            this.srvFiles = new ArrayList<>();
        }
    }
    
    public long getLastStamp() {
        return lastStamp;
    }
    
    public ConcurrentMap<File, Long> getFilesScan() {
        filesScan.put(newLanLastScan200, newLanLastScan200.length());
        filesScan.put(newLanLastScan210, newLanLastScan210.length());
        filesScan.put(oldLanLastScan0, oldLanLastScan0.length());
        filesScan.put(oldLanLastScan1, oldLanLastScan1.length());
        srvFiles.forEach(file->filesScan.put(file, file.length()));
        return filesScan;
    }
    
    void setLastStamp(long lastStamp) {
        this.lastStamp = lastStamp;
    }
    
    public static NetScanFileWorker getI() {
        return ourInst;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScanFileWorker{");
        sb.append("lastStamp=").append(lastStamp);
        sb.append(", newLanLastScan=").append(newLanLastScan200.getAbsolutePath());
        sb.append(", newLanLastScan=").append(newLanLastScan210.getAbsolutePath());
        sb.append(", oldLanLastScan=").append(oldLanLastScan0.getAbsolutePath());
        sb.append(", oldLanLastScan=").append(oldLanLastScan1.getAbsolutePath());
        sb.append('}');
        return sb.toString();
    }
    
    /**
     @param oldLanLastScan0 {@link #oldLanLastScan0}
     @param oldLanLastScan1 {@link #oldLanLastScan1}
     */
    void setOldLanLastScan(File oldLanLastScan0, File oldLanLastScan1) {
        this.oldLanLastScan0 = oldLanLastScan0;
        this.oldLanLastScan1 = oldLanLastScan1;
    }
    
    /**
     Сетает NEW LAN SCAN
     <p>
     Создаёт файлы. <br>
     * @param newLanLastScan200 {@link #newLanLastScan200}
     * @param newLanLastScan210 {@link #newLanLastScan210}
     */
    void setNewLanLastScan(File newLanLastScan200, File newLanLastScan210) {
        this.newLanLastScan200 = newLanLastScan200;
        this.newLanLastScan210 = newLanLastScan210;
    }
    
    /**
     * @return {@link #newLanLastScan200} и {@link #newLanLastScan210}, как строчка
     */
    String getNewLanLastScanAsStr() {
        try {
            return FileSystemWorker.readFile(newLanLastScan200.getAbsolutePath()) + "\n<br>" + FileSystemWorker.readFile(newLanLastScan210.getAbsolutePath());
        } catch (NullPointerException e) {
            return FileSystemWorker.readFile(ConstantsNet.FILENAME_AVAILABLELAST200210TXT) + "<p>" + FileSystemWorker.readFile(ConstantsNet.FILENAME_AVAILABLELAST210220TXT);
        }
    }
    
    /**
     * Читает построчно
     * <p>
     *
     * @return {@link Deque} строк - содержимое {@link #oldLanLastScan0}, {@link #oldLanLastScan1} и {@link #newLanLastScan200} + {@link #newLanLastScan210}
     * @throws IOException files
     * @see NetListKeeper#onlinesAddressesList()
     */
    Deque<String> getListOfOnlineDev() throws IOException {
        AppComponents.threadConfig().thrNameSet("ON_Dq");
        
        String classMeth = "NetScanFileWorker.getListOfOnlineDev";
        String titleMsg = "retDeque.size()";
        Deque<String> retDeque = new ArrayDeque<>();
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
        if (srvFiles.size() == 4) {
            srvFiles.forEach(srvFileX->fileWrk(srvFileX, titleMsg, retDeque));
        }
        else {
            messageToUser.info(msg + " " + retDeque.size(), "positions] [Returns:", "java.util.Deque<java.lang.String>");
        }
        return retDeque;
    }
    
    /**
     @return {@link #oldLanLastScan0} и {@link #oldLanLastScan1}, как строчка
     */
    String getOldLanLastScanAsStr() {
        try {
            return FileSystemWorker.readFile(oldLanLastScan0.getAbsolutePath()) + "\n<br>" + FileSystemWorker.readFile(oldLanLastScan1.getAbsolutePath());
        }
        catch (NullPointerException ignore) {
            return FileSystemWorker.readFile(ConstantsNet.FILENAME_OLDLANTXT0) + "\n<br>" + FileSystemWorker.readFile(ConstantsNet.FILENAME_OLDLANTXT1);
        }
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
