package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;


/**
 Файловые работы.

 @since 25.12.2018 (10:43) */
@Scope(ConstantsFor.SINGLETON)
@Component
class NetScanFileWorker {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static NetScanFileWorker ourInst = new NetScanFileWorker();

    private long lastStamp = System.currentTimeMillis();

    /**
     {@link DiapazonedScan#scanNew()}
     */
    private File newLanLastScan = new File(ConstantsFor.AVAILABLE_LAST_TXT);

    /**
     {@link DiapazonedScan#scanOldLan(long)}
     */
    private File oldLanLastScan = new File(ConstantsFor.OLD_LAN_TXT);

    /**
     @param newLanLastScan {@link #newLanLastScan}
     */
    void setNewLanLastScan(File newLanLastScan) {
        this.newLanLastScan = newLanLastScan;
        new MessageCons().infoNoTitles("newLanLastScan = [" + this.newLanLastScan.getAbsolutePath() + "]");
    }

    /**
     @param oldLanLastScan {@link #oldLanLastScan}
     */
    void setOldLanLastScan(File oldLanLastScan) {
        this.oldLanLastScan = oldLanLastScan;
        new MessageCons().infoNoTitles("oldLanLastScan = [" + this.oldLanLastScan.getAbsolutePath() + "]");
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
     @return {@link #newLanLastScan}, как строчка
     */
    String getNewLanLastScanAsStr() {
        try {
            return FileSystemWorker.readFile(newLanLastScan.getAbsolutePath());
        } catch (NullPointerException e) {
            return FileSystemWorker.readFile(ConstantsFor.AVAILABLE_LAST_TXT) + "<p>";
        }
    }

    /**
     Читает построчно
     <p>

     @return {@link Deque} строк - содержимое {@link #oldLanLastScan} и {@link #newLanLastScan}
     @throws IOException files
     @see NetListKeeper#onlinesAddressesList()
     */
    Deque<String> getListOfOnlineDev() throws IOException {
        LOGGER.warn("NetScanFileWorker.getListOfOnlineDev");
        Deque<String> retDeque = new ArrayDeque<>();
        String msg = newLanLastScan.getAbsolutePath() + oldLanLastScan.getAbsolutePath() + " is created by " + getClass().getSimpleName();

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
        new MessageCons().info(msg + " " + retDeque.size(), "positions] [Returns:", "java.util.Deque<java.lang.String>");
        return retDeque;
    }

    /**
     @return {@link #oldLanLastScan}, как строчка
     */
    String getOldLanLastScanAsStr() {
        try {
            return FileSystemWorker.readFile(oldLanLastScan.getAbsolutePath());
        } catch (NullPointerException ignore) {
            return FileSystemWorker.readFile(ConstantsFor.OLD_LAN_TXT);
        }
    }

    private NetScanFileWorker() {
        new MessageCons().infoNoTitles("NetScanFileWorker.NetScanFileWorker");
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
