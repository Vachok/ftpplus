// Copyright (c) all rights. http://networker.vachok.ru 2019.
package ru.vachok.networker.net;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.Keeper;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 Файловые работы.
 <p>
 @see ru.vachok.networker.net.NetScanFileWorkerTest
 @since 25.12.2018 (10:43) */
public class NetScanFileWorker implements Serializable, Keeper {
    
    
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static final NetScanFileWorker NET_SCAN_FILE_WORKER = new NetScanFileWorker();
    
    private static MessageToUser messageToUser = new MessageLocal(NetScanFileWorker.class.getSimpleName());
    
    private long lastStamp = System.currentTimeMillis();
    
    public long getLastStamp() {
        return lastStamp;
    }
    
    public void setLastStamp(long lastStamp) {
        this.lastStamp = lastStamp;
        Preferences pref = AppComponents.getUserPref();
        pref.put("NetScanFileWorker.lastStamp", new Date(lastStamp).toString());
        try {
            pref.sync();
        }
        catch (BackingStoreException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    @Contract(pure = true)
    public static NetScanFileWorker getI() {
        return NET_SCAN_FILE_WORKER;
    }
    
    @Override
    public Deque<InetAddress> getOnlineDevicesInetAddress() {
        return getDequeOfOnlineDev();
    }
    
    public void setLastStamp(long millis, String address) {
        setLastStamp(millis);
        Preferences pref = AppComponents.getUserPref();
        pref.put("lastIP", address);
        try {
            pref.sync();
        }
        catch (BackingStoreException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", NetScanFileWorker.class.getSimpleName() + "[\n", "\n]")
            .add("lastStamp = " + lastStamp)
            .toString();
    }
    
    private static List<InetAddress> readFilesLANToCollection(@NotNull File scanFile) {
        List<String> listOfIPAsStrings = FileSystemWorker.readFileToList(scanFile.toPath().toAbsolutePath().normalize().toString());
        Collections.sort(listOfIPAsStrings);
        List<InetAddress> retList = new ArrayList<>(listOfIPAsStrings.size());
        listOfIPAsStrings.forEach(addr->retList.add(parseInetAddress(addr)));
        return retList;
    }
    
    private static InetAddress parseInetAddress(String addr) {
        InetAddress inetAddress = InetAddress.getLoopbackAddress();
        try {
            inetAddress = InetAddress.getByAddress(InetAddress.getByName(addr.split(" ")[0]).getAddress());
        }
        catch (UnknownHostException e) {
            messageToUser.error(MessageFormat
                .format("NetScanFileWorker.parseInetAddress\n{0}: {1}\nParameters: [addr]\nReturn: java.net.InetAddress\nStack:\n{2}", e.getClass().getTypeName(), e
                    .getMessage(), new TForms().fromArray(e)));
        }
        catch (ArrayIndexOutOfBoundsException ignore) {
            //
        }
        return inetAddress;
    }
    
    /**
     Читает файлы из {@link DiapazonScan#editScanFiles()} в {@link Deque}
     <p>
     
     @return {@link Deque} of {@link String}, с именами девайсов онлайн.
     */
    private static Deque<InetAddress> getDequeOfOnlineDev() {
        Deque<InetAddress> retDeque = new ArrayDeque<>();
        Map<String, File> scanFiles = DiapazonScan.getScanFiles();
        
        scanFiles.forEach((scanFileName, scanFile)->retDeque.addAll(readFilesLANToCollection(scanFile)));
        return retDeque;
    }
}
