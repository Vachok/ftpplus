// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.data;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.data.enums.*;
import ru.vachok.networker.componentsrepo.exceptions.ScanFilesException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.net.monitor.DiapazonScan;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;


/**
 @see ru.vachok.networker.data.NetKeeperTest */
public abstract class NetKeeper implements Keeper {
    
    
    private static final ConcurrentNavigableMap<String, Boolean> SCANNED_USER_PC = new ConcurrentSkipListMap<>();
    
    private static final BlockingDeque<String> ALL_DEVICES = new LinkedBlockingDeque<>(ConstantsNet.IPS_IN_VELKOM_VLAN);
    
    private static final Map<String, File> SCAN_FILES = new ConcurrentHashMap<>();
    
    private static final List<String> CURRENT_SCAN_LIST = new ArrayList<>();
    
    private static final List<File> CURRENT_SCAN_FILES = new ArrayList<>();
    
    private static final List<String> ONE_PC_MONITOR = new ArrayList<>();
    
    private static final List<String> KUDR_WORK_TIME = new ArrayList<>();
    
    private static final Set<String> PC_NAMES_FOR_SEND_TO_DATABASE = new CopyOnWriteArraySet<>();
    
    private static final Collection<String> UNUSED_NAMES_TREE = new TreeSet<>();
    
    private static final ConcurrentMap<String, String> PC_USER = new ConcurrentHashMap<>();
    
    @Contract(pure = true)
    public static ConcurrentMap<String, String> getPcUser() {
        return PC_USER;
    }
    
    private static Properties properties = AppComponents.getProps();
    
    private static MessageToUser messageToUser = new MessageLocal(NetKeeper.class.getSimpleName());
    
    private static Map<String, File> scanFiles = getScanFiles();
    
    private static String currentProvider = "Unknown yet";
    
    @Contract(pure = true)
    public static List<String> getOnePcMonitor() {
        return ONE_PC_MONITOR;
    }
    
    @Contract(pure = true)
    public static ConcurrentNavigableMap<String, Boolean> getUsersScanWebModelMapWithHTMLLinks() {
        return SCANNED_USER_PC;
    }
    
    @Contract(pure = true)
    public static Map<String, File> getScanFiles() {
        return SCAN_FILES;
    }
    
    @Contract(" -> new")
    public static @NotNull List<String> getCurrentScanLists() {
        if (SCAN_FILES.size() != 9) {
            makeFilesMap();
            CURRENT_SCAN_LIST.clear();
        }
        for (File file : SCAN_FILES.values()) {
            CURRENT_SCAN_LIST.addAll(FileSystemWorker.readFileToList(file.getAbsolutePath()));
        }
        return new ArrayList<>(CURRENT_SCAN_LIST);
    }
    
    public static int makeFilesMap() {
        if (checkAlreadyExistingFiles()) {
    
            File lan205 = new File(FileNames.NEWLAN205);
            scanFiles.put(FileNames.NEWLAN205, lan205);
    
            File lan210 = new File(FileNames.NEWLAN210);
            scanFiles.put(FileNames.NEWLAN210, lan210);
    
            File lan215 = new File(FileNames.NEWLAN215);
            scanFiles.put(FileNames.NEWLAN215, lan215);
    
            File lan220 = new File(FileNames.NEWLAN220);
            scanFiles.put(FileNames.NEWLAN220, lan220);
    
            File oldLan0 = new File(FileNames.OLDLANTXT0);
            scanFiles.put(FileNames.OLDLANTXT0, oldLan0);
    
            File oldLan1 = new File(FileNames.OLDLANTXT1);
            scanFiles.put(FileNames.OLDLANTXT1, oldLan1);
    
            File srv10 = new File(FileNames.SERVTXT_10SRVTXT);
            scanFiles.put(FileNames.SERVTXT_10SRVTXT, srv10);
    
            File srv21 = new File(FileNames.SERVTXT_21SRVTXT);
            scanFiles.put(FileNames.SERVTXT_21SRVTXT, srv21);
    
            File srv31 = new File(FileNames.SERVTXT_31SRVTXT);
            scanFiles.put(FileNames.SERVTXT_31SRVTXT, srv31);
        }
        return scanFiles.size();
    }
    
    private static boolean checkAlreadyExistingFiles() {
        try {
            for (File scanFile : Objects.requireNonNull(new File(ConstantsFor.ROOT_PATH_WITH_SEPARATOR).listFiles())) {
                String scanFileName = scanFile.getName();
                if (scanFile.length() > 0 & scanFileName.contains("lan_")) {
                    messageToUser.info(copyToLanDir(scanFile));
                }
            }
            return true;
        }
        catch (NullPointerException e) {
            throw new ScanFilesException("No lan_ files found");
        }
    }
    
    /**
     Все возможные IP из диапазонов {@link DiapazonScan}
     
     @return {@link #ALL_DEVICES}
     */
    public static BlockingDeque<String> getAllDevices() {
        int vlanNum = ConstantsNet.IPS_IN_VELKOM_VLAN / ConstantsNet.MAX_IN_ONE_VLAN;
        properties.setProperty(PropertiesNames.PR_VLANNUM, String.valueOf(vlanNum));
        return ALL_DEVICES;
    }
    
    @Contract(pure = true)
    public static List<String> getKudrWorkTime() {
        return KUDR_WORK_TIME;
    }
    
    public static @NotNull Deque<InetAddress> getDequeOfOnlineDev() {
        Deque<InetAddress> retDeque = new ArrayDeque<>();
        List<File> scanFiles = getCurrentScanFiles();
        scanFiles.forEach((scanFile)->retDeque.addAll(readFilesLANToCollection(scanFile)));
        return retDeque;
    }
    
    public static @NotNull List<File> getCurrentScanFiles() {
        if (SCAN_FILES.size() != 9) {
            makeFilesMap();
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
    
    @Contract(pure = true)
    public static Set<String> getPcNamesForSendToDatabase() {
        return PC_NAMES_FOR_SEND_TO_DATABASE;
    }
    
    private static @NotNull List<InetAddress> readFilesLANToCollection(@NotNull File scanFile) {
        List<String> listOfIPAsStrings = FileSystemWorker.readFileToList(scanFile.toPath().toAbsolutePath().normalize().toString());
        Collections.sort(listOfIPAsStrings);
        List<InetAddress> retList = new ArrayList<>(listOfIPAsStrings.size());
        listOfIPAsStrings.forEach(addr->retList.add(parseInetAddress(addr)));
        return retList;
    }
    
    private static InetAddress parseInetAddress(@NotNull String addr) {
        InetAddress inetAddress = InetAddress.getLoopbackAddress();
        try {
            inetAddress = InetAddress.getByAddress(InetAddress.getByName(addr.split(" ")[0]).getAddress());
        }
        catch (UnknownHostException e) {
            messageToUser.error(MessageFormat.format("NetScanFileWorker.parseInetAddress: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        catch (ArrayIndexOutOfBoundsException ignore) {
            //
        }
        return inetAddress;
    }
    
    /**
     Неиспользуемые имена ПК
     */
    @Contract(pure = true)
    public static Collection<String> getUnusedNamesTree() {
        return UNUSED_NAMES_TREE;
    }
    
    @Contract(pure = true)
    public static String getCurrentProvider() {
        return currentProvider;
    }
    
    public static void setCurrentProvider(String currentProvider) {
        NetKeeper.currentProvider = currentProvider;
    }
    
    private static @NotNull String copyToLanDir(@NotNull File scanFile) {
        StringBuilder sb = new StringBuilder();
        String scanCopyFileName = scanFile.getName().replace(".txt", "_" + LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(3)) + ".scan");
        
        Path copyPath = Paths.get(ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + scanCopyFileName).toAbsolutePath();
        boolean isCopyOk = FileSystemWorker.copyOrDelFile(scanFile, copyPath, true);
        
        sb.append(scanFile.getAbsolutePath()).append("->").append(scanFile.getAbsolutePath()).append(" (").append(scanFile.length() / ConstantsFor.KBYTE)
                .append(" kilobytes)");
        sb.append(" copied: ").append(isCopyOk).append(" old must be delete!");
        if (scanFile.exists()) {
            scanFile.deleteOnExit();
        }
        return sb.toString();
    }
}
