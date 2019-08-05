// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.exe.schedule.ScanFilesWorker;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;


/**
 @see ru.vachok.networker.abstr.NetKeeperTest */
public abstract class NetKeeper implements Keeper, Externalizable {
    
    
    @Contract(pure = true)
    public static List<String> getOnePcMonitor() {
        return ONE_PC_MONITOR;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Map<String, Object> serialObjects = new ConcurrentHashMap<>();
        serialObjects.put("ALL_DEVICES", ALL_DEVICES);
        serialObjects.put("CURRENT_SCAN_LIST", CURRENT_SCAN_LIST);
        serialObjects.put("CURRENT_SCAN_FILES", CURRENT_SCAN_FILES);
        serialObjects.put("NETWORK", NETWORK);
        serialObjects.put("ONE_PC_MONITOR", ONE_PC_MONITOR);
        serialObjects.put("KUDR_WORK_TIME", KUDR_WORK_TIME);
        try {
            if (out == null) {
                out = new ObjectOutputStream(new FileOutputStream(NetKeeper.class.getSimpleName()));
            }
            out.writeObject(serialObjects);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("NetKeeper.writeExternal {0} - {1}\nStack:\n{2}",
                e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
        
    }
    
    private static final BlockingDeque<String> ALL_DEVICES = new LinkedBlockingDeque<>(ConstantsNet.IPS_IN_VELKOM_VLAN);
    
    private static final Map<String, File> SCAN_FILES = new ConcurrentHashMap<>();
    
    private static final List<String> CURRENT_SCAN_LIST = new ArrayList<>();
    
    private static final List<File> CURRENT_SCAN_FILES = new ArrayList<>();
    
    public static final ConcurrentNavigableMap<String, Boolean> NETWORK = new ConcurrentSkipListMap<>();
    
    private static final List<String> ONE_PC_MONITOR = new ArrayList<>();
    
    private static Properties properties = AppComponents.getProps();
    
    private static final List<String> KUDR_WORK_TIME = new ArrayList<>();
    
    @Contract(pure = true)
    public static ConcurrentNavigableMap<String, Boolean> getNetworkPCs() {
        return NETWORK;
    }
    
    private static MessageToUser messageToUser = new MessageLocal(NetKeeper.class.getSimpleName());
    
    @Contract(pure = true)
    public static Map<String, File> getScanFiles() {
        return SCAN_FILES;
    }
    
    public abstract Deque<InetAddress> getOnlineDevicesInetAddress();
    
    @Contract(" -> new")
    public static @NotNull List<String> getCurrentScanLists() {
        if (SCAN_FILES.size() != 9) {
            ScanFilesWorker.makeFilesMap();
            CURRENT_SCAN_LIST.clear();
        }
        for (File file : SCAN_FILES.values()) {
            CURRENT_SCAN_LIST.addAll(FileSystemWorker.readFileToList(file.getAbsolutePath()));
        }
        return new ArrayList<>(CURRENT_SCAN_LIST);
    }
    
    public static @NotNull List<File> getCurrentScanFiles() {
        if (SCAN_FILES.size() != 9) {
            ScanFilesWorker.makeFilesMap();
        }
        List<File> retList = new ArrayList<>();
        for (File listFile : SCAN_FILES.values()) {
            if (listFile.exists()) {
                retList.add(listFile);
            }
            else {
                try {
                    Files.createFile(listFile.toPath());
                    retList.add(listFile);
                }
                catch (IOException e) {
                    messageToUser.error(MessageFormat.format("ScanFilesWorker.getCurrentScanFiles: {0}, ({1})", e.getMessage(), e.getClass().getName()));
                }
            }
        }
        return retList;
    }
    
    /**
     Все возможные IP из диапазонов {@link DiapazonScan}
     
     @return {@link #ALL_DEVICES}
     */
    public static BlockingDeque<String> getAllDevices() {
        int vlanNum = ConstantsNet.IPS_IN_VELKOM_VLAN / ConstantsNet.MAX_IN_ONE_VLAN;
        properties.setProperty(ConstantsFor.PR_VLANNUM, String.valueOf(vlanNum));
        return ALL_DEVICES;
    }
    
    @Contract(pure = true)
    public static List<String> getKudrWorkTime() {
        return KUDR_WORK_TIME;
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Map<String, Object> serialObjects = new ConcurrentHashMap<>();
        try {
            if (in == null) {
                in = new ObjectInputStream(new FileInputStream(NetKeeper.class.getSimpleName()));
            }
            serialObjects = (Map<String, Object>) in.readObject();
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("NetKeeper.readExternal {0} - {1}\nStack:\n{2}",
                e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
        ALL_DEVICES.addAll((Collection<? extends String>) serialObjects.get("ALL_DEVICES"));
        CURRENT_SCAN_FILES.addAll((Collection<? extends File>) serialObjects.get("CURRENT_SCAN_FILES"));
        CURRENT_SCAN_LIST.addAll((Collection<? extends String>) serialObjects.get("CURRENT_SCAN_LIST"));
        ONE_PC_MONITOR.addAll((Collection<? extends String>) serialObjects.get("ONE_PC_MONITOR"));
        KUDR_WORK_TIME.addAll((Collection<? extends String>) serialObjects.get("KUDR_WORK_TIME"));
        NETWORK.putAll((Map<? extends String, ? extends Boolean>) serialObjects.get("NETWORK"));
    }
}
