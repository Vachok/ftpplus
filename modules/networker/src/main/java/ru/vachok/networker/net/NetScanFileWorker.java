package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 Файловые работы.

 @since 25.12.2018 (10:43) */
@Scope (ConstantsFor.SINGLETON)
@Component
class NetScanFileWorker {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static NetScanFileWorker ourInst = new NetScanFileWorker();

    private long lastStamp;

    public long getLastStamp() {
        return lastStamp;
    }

    void setLastStamp(long lastStamp) {
        this.lastStamp = lastStamp;
    }

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
    }

    /**
     @param oldLanLastScan {@link #oldLanLastScan}
     */
    void setOldLanLastScan(File oldLanLastScan) {
        this.oldLanLastScan = oldLanLastScan;
    }

    public static NetScanFileWorker getI() {
        return ourInst;
    }

    @Override
    public int hashCode() {
        return Objects.hash(newLanLastScan, oldLanLastScan);
    }

    /**
     @return {@link #newLanLastScan}, как строчка
     */
    String getNewLanLastScanAsStr() {
        try{
            return FileSystemWorker.readFile(newLanLastScan.getAbsolutePath());
        }
        catch(NullPointerException e){
            return FileSystemWorker.readFile(ConstantsFor.AVAILABLE_LAST_TXT) + "<p>";
        }
    }

    List<String> getListOfOnlineDev() throws IOException {
        List<String> retList = new ArrayList<>();
        String msg = newLanLastScan.getAbsolutePath() + oldLanLastScan.getAbsolutePath() + " is created by " + getClass().getSimpleName();
        if(newLanLastScan.exists() && newLanLastScan.canRead()){
            retList.addAll(FileSystemWorker.readFileToList(newLanLastScan.getAbsolutePath()));
        }
        else{
            boolean newFile = newLanLastScan.createNewFile();
            msg = newFile + " " + msg;
            LOGGER.warn(msg);
        }
        if(oldLanLastScan.exists() && oldLanLastScan.canRead()){
            retList.addAll(FileSystemWorker.readFileToList(oldLanLastScan.getAbsolutePath()));
        }
        else{
            boolean oldLanLastScanNewFile = oldLanLastScan.createNewFile();
            msg = oldLanLastScanNewFile + " " + msg;
            LOGGER.warn(msg);
        }
        return retList;
    }

    /**
     @return {@link #oldLanLastScan}, как строчка
     */
    String getOldLanLastScanAsStr() {
        try{
            return FileSystemWorker.readFile(oldLanLastScan.getAbsolutePath());
        }
        catch(NullPointerException ignore){
            return FileSystemWorker.readFile(ConstantsFor.OLD_LAN_TXT);
        }
    }

    private NetScanFileWorker() {

    }

    @Override
    public boolean equals(Object o) {
        if(this==o){
            return true;
        }
        if(!(o instanceof NetScanFileWorker)){
            return false;
        }
        NetScanFileWorker that = ( NetScanFileWorker ) o;
        return Objects.equals(newLanLastScan, that.newLanLastScan) &&
            Objects.equals(oldLanLastScan, that.oldLanLastScan);
    }
}
