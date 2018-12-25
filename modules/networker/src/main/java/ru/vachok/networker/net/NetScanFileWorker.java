package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.util.Objects;

/**
 Файловые работы.

 @since 25.12.2018 (10:43) */
class NetScanFileWorker {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static NetScanFileWorker ourInst = new NetScanFileWorker();

    /**
     {@link DiapazonedScan#scanNew()}
     */
    private File newLanLastScan;

    /**
     {@link DiapazonedScan#scanOldLan(long)}
     */
    private File oldLanLastScan;

    private NetScanFileWorker() {

    }

    public static NetScanFileWorker getI() {
        String hashCodeAndName = ourInst.hashCode() + " " + ourInst.getClass().getSimpleName();
        LOGGER.info(hashCodeAndName);
        return ourInst;
    }

    @Override
    public int hashCode() {
        return Objects.hash(newLanLastScan, oldLanLastScan);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetScanFileWorker)) return false;
        NetScanFileWorker that = (NetScanFileWorker) o;
        return Objects.equals(newLanLastScan, that.newLanLastScan) &&
            Objects.equals(oldLanLastScan, that.oldLanLastScan);
    }

    /**
     @param newLanLastScan {@link #newLanLastScan}
     */
    void setNewLanLastScan(File newLanLastScan) {
        this.newLanLastScan = newLanLastScan;
    }

    /**
     @return {@link #newLanLastScan}, как строчка
     */
    String getNewLanLastScanAsStr() {
        try {
            return FileSystemWorker.readFile(newLanLastScan.getAbsolutePath());
        } catch (NullPointerException e) {
            return FileSystemWorker.readFile("available_last.txt") + "<p>";
        }
    }

    /**
     @param oldLanLastScan {@link #oldLanLastScan}
     */
    void setOldLanLastScan(File oldLanLastScan) {
        this.oldLanLastScan = oldLanLastScan;
    }

    /**
     @return {@link #oldLanLastScan}, как строчка
     */
    String getOldLanLastScanAsStr() {
        try {
            return FileSystemWorker.readFile(oldLanLastScan.getAbsolutePath());
        } catch (NullPointerException ignore) {
            return FileSystemWorker.readFile("old_lan.txt");
        }
    }
}
