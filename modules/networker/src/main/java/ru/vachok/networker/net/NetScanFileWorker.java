package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;


/**
 * Файловые работы.
 *
 * @since 25.12.2018 (10:43)
 */
@Scope(ConstantsFor.SINGLETON)
@Component
class NetScanFileWorker {

    private static final Logger LOGGER = AppComponents.getLogger(NetScanFileWorker.class.getSimpleName());

    private static NetScanFileWorker ourInst = new NetScanFileWorker();

    private static MessageToUser messageToUser = new MessageCons();

    private long lastStamp = System.currentTimeMillis();

    /**
     * {@link DiapazonedScan#scanNew()}
     */
    private File newLanLastScan = new File(ConstantsNet.FILENAME_AVAILABLELASTTXT);

    /**
     * {@link ConstantsNet#FILENAME_OLDLANTXT}}
     */
    private File oldLanLastScan = new File(ConstantsNet.FILENAME_OLDLANTXT);

    private List<File> srvFiles;

    private NetScanFileWorker() {
    }

    public void setSrvFiles(List<File> srvFiles) {
        this.srvFiles = srvFiles;
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

    /**
     * @param newLanLastScan {@link #newLanLastScan}
     */
    void setNewLanLastScan(File newLanLastScan) {
        this.newLanLastScan = newLanLastScan;
        messageToUser.infoNoTitles("newLanLastScan = [" + this.newLanLastScan.getAbsolutePath() + "]");
    }

    /**
     * @param oldLanLastScan {@link #oldLanLastScan}
     */
    void setOldLanLastScan(File oldLanLastScan) {
        this.oldLanLastScan = oldLanLastScan;
        messageToUser.infoNoTitles("oldLanLastScan = [" + this.oldLanLastScan.getAbsolutePath() + "]");
    }

    /**
     * @return {@link #newLanLastScan}, как строчка
     */
    String getNewLanLastScanAsStr() {
        try {
            return FileSystemWorker.readFile(newLanLastScan.getAbsolutePath());
        } catch (NullPointerException e) {
            return FileSystemWorker.readFile(ConstantsNet.FILENAME_AVAILABLELASTTXT) + "<p>";
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
        AppComponents.threadConfig().thrNameSet("ONDEQ");
        Deque<String> retDeque = new ArrayDeque<>();
        String msg = newLanLastScan.getAbsolutePath() +
            ";\n" + oldLanLastScan.getAbsolutePath() +
            ";\n" + new TForms().fromArray(srvFiles, false) +
            ";\nCreated by " + getClass().getSimpleName();

        if (newLanLastScan.exists() && newLanLastScan.canRead()) {
            retDeque.addAll(FileSystemWorker.readFileToList(newLanLastScan.getAbsolutePath()));
        } else {
            boolean newFile = newLanLastScan.createNewFile();
            msg = newFile + " " + msg;
        }
        if (oldLanLastScan.exists() && oldLanLastScan.canRead()) {
            retDeque.addAll(FileSystemWorker.readFileToList(oldLanLastScan.getAbsolutePath()));
        } else {
            boolean oldLanLastScanNewFile = oldLanLastScan.createNewFile();
            msg = oldLanLastScanNewFile + " " + msg;
        }
        srvFiles.forEach(srvFileX -> {
            if (srvFileX.exists() && srvFileX.canRead()) {
                retDeque.addAll(FileSystemWorker.readFileToList(srvFileX.getAbsolutePath()));
                messageToUser.info("NetScanFileWorker.getListOfOnlineDev", "retDeque.size()", " = " + retDeque.size());
            } else {
                boolean srvScanFile = false;
                try {
                    srvScanFile = srvFileX.createNewFile();
                } catch (IOException e) {
                    messageToUser.errorAlert("NetScanFileWorker", "getListOfOnlineDev", e.getMessage());
                    FileSystemWorker.error("NetScanFileWorker.getListOfOnlineDev", e);
                }
                messageToUser.info(srvFileX.getAbsolutePath(), " is new file? ", " = " + srvScanFile);
            }
        });
        messageToUser.info(msg + " " + retDeque.size(), "positions] [Returns:", "java.util.Deque<java.lang.String>");
        return retDeque;
    }

    /**
     * @return {@link #oldLanLastScan}, как строчка
     */
    String getOldLanLastScanAsStr() {
        try {
            return FileSystemWorker.readFile(oldLanLastScan.getAbsolutePath());
        } catch (NullPointerException ignore) {
            return FileSystemWorker.readFile(ConstantsNet.FILENAME_OLDLANTXT);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(newLanLastScan, oldLanLastScan);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NetScanFileWorker)) {
            return false;
        }
        NetScanFileWorker that = (NetScanFileWorker) o;
        return Objects.equals(newLanLastScan, that.newLanLastScan) &&
            Objects.equals(oldLanLastScan, that.oldLanLastScan);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScanFileWorker{");
        sb.append("lastStamp=").append(lastStamp);
        sb.append(", newLanLastScan=").append(newLanLastScan.getAbsolutePath());
        sb.append(", oldLanLastScan=").append(oldLanLastScan.getAbsolutePath());
        sb.append(", ourInst=").append(ourInst.hashCode());
        sb.append('}');
        return sb.toString();
    }
}
