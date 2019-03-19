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

    /**
     * {@link ConstantsNet#FILENAME_OLDLANTXT}}
     */
    private File oldLanLastScan = new File(ConstantsNet.FILENAME_OLDLANTXT);
    
    private List<File> srvFiles = new ArrayList<>(4);

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
        sb.append(", oldLanLastScan=").append(oldLanLastScan.getAbsolutePath());
        sb.append('}');
        return sb.toString();
    }
    
    /**
     @param oldLanLastScan {@link #oldLanLastScan}
     */
    void setOldLanLastScan(File oldLanLastScan) {
        this.oldLanLastScan = oldLanLastScan;
        messageToUser.infoNoTitles("oldLanLastScan = [" + this.oldLanLastScan.getAbsolutePath() + "]");
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
        messageToUser.infoNoTitles("newLanLastScan = [" + this.newLanLastScan200.getAbsolutePath() + "]");
        messageToUser.infoNoTitles("newLanLastScan = [" + this.newLanLastScan210.getAbsolutePath() + "]");
    }

    /**
     * @return {@link #newLanLastScan}, как строчка
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
     * @return {@link Deque} строк - содержимое {@link #oldLanLastScan} и {@link #newLanLastScan}
     * @throws IOException files
     * @see NetListKeeper#onlinesAddressesList()
     */
    Deque<String> getListOfOnlineDev() throws IOException {
        AppComponents.threadConfig().thrNameSet("ON_Dq");
    
        String classMeth = "NetScanFileWorker.getListOfOnlineDev";
        String titleMsg = "retDeque.size()";
        Deque<String> retDeque = new ArrayDeque<>();
        String msg = newLanLastScan200.getAbsolutePath() + ";\n" + newLanLastScan210.getAbsolutePath() + ";\n" + oldLanLastScan.getAbsolutePath() +
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
        if (oldLanLastScan.exists() && oldLanLastScan.canRead()) {
            retDeque.addAll(FileSystemWorker.readFileToList(oldLanLastScan.getAbsolutePath()));
            messageToUser.info(classMeth, titleMsg, " = " + retDeque.size());
        }
        else {
            boolean oldLanLastScanNewFile = oldLanLastScan.createNewFile();
            msg = oldLanLastScanNewFile + " " + msg;
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
     @return {@link #oldLanLastScan}, как строчка
     */
    String getOldLanLastScanAsStr() {
        try {
            return FileSystemWorker.readFile(oldLanLastScan.getAbsolutePath());
        }
        catch (NullPointerException ignore) {
            return FileSystemWorker.readFile(ConstantsNet.FILENAME_OLDLANTXT);
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
