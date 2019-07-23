package ru.vachok.networker.exe.schedule;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.componentsrepo.exceptions.ScanFilesException;
import ru.vachok.networker.exe.runnabletasks.ExecScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.restapi.message.DBMessenger;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import static ru.vachok.networker.net.enums.ConstantsNet.*;


/**
 @since 23.07.2019 (12:15) */
public final class ScanFilesWorker extends DiapazonScan implements NetKeeper {
    
    
    private static final Pattern COMPILE = Pattern.compile(".txt", Pattern.LITERAL);
    
    private static MessageToUser messageToUser = new MessageLocal(ru.vachok.networker.exe.schedule.ScanFilesWorker.class.getSimpleName());
    
    private static Map<String, File> scanFiles = new ConcurrentHashMap<>();
    
    @Override
    public Deque<InetAddress> getOnlineDevicesInetAddress() {
        return new NetScanFileWorker().getOnlineDevicesInetAddress();
    }
    
    @Override
    public List<String> getCurrentScanLists() {
        if (scanFiles.size() != 9) {
            makeFilesMap();
        }
        List<String> currentScanFiles = new ArrayList<>();
        for (File file : scanFiles.values()) {
            currentScanFiles.addAll(FileSystemWorker.readFileToList(file.getAbsolutePath()));
        }
        return currentScanFiles;
    }
    
    @Override
    public List<File> getCurrentScanFiles() {
        List<File> retList = new ArrayList<>();
        File rootDir = new File(ConstantsFor.ROOT_PATH_WITH_SEPARATOR);
        for (File listFile : Objects.requireNonNull(rootDir.listFiles())) {
            if (listFile.getName().contains("lan_")) {
                retList.add(listFile);
            }
        }
        return retList;
    }
    
    static Map<String, File> getScanFiles() {
        if (scanFiles.size() != 9) {
            makeFilesMap();
        }
        return scanFiles;
    }
    
    static long getRunMin() {
        Preferences preferences = Preferences.userRoot();
        try {
            preferences.sync();
            return preferences.getLong(ExecScan.class.getSimpleName(), 1);
        }
        catch (BackingStoreException e) {
            InitProperties initProperties = new FileProps(ConstantsFor.PROPS_FILE_JAVA_ID);
            Properties props = initProperties.getProps();
            return Long.parseLong(props.getProperty(ExecScan.class.getSimpleName()));
        }
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
    
    private static void makeFilesMap() {
        if (checkAlreadyExistingFiles()) {
            
            File lan205 = new File(FILENAME_NEWLAN205);
            scanFiles.put(FILENAME_NEWLAN205, lan205);
            
            File lan210 = new File(FILENAME_NEWLAN210);
            scanFiles.put(FILENAME_NEWLAN210, lan210);
            
            File lan215 = new File(FILENAME_NEWLAN215);
            scanFiles.put(FILENAME_NEWLAN215, lan215);
            
            File lan220 = new File(FILENAME_NEWLAN220);
            scanFiles.put(FILENAME_NEWLAN220, lan220);
            
            File oldLan0 = new File(FILENAME_OLDLANTXT0);
            scanFiles.put(FILENAME_OLDLANTXT0, oldLan0);
            
            File oldLan1 = new File(FILENAME_OLDLANTXT1);
            scanFiles.put(FILENAME_OLDLANTXT1, oldLan1);
            
            File srv10 = new File(FILENAME_SERVTXT_10SRVTXT);
            scanFiles.put(FILENAME_SERVTXT_10SRVTXT, srv10);
            
            File srv21 = new File(FILENAME_SERVTXT_21SRVTXT);
            scanFiles.put(FILENAME_SERVTXT_21SRVTXT, srv21);
            
            File srv31 = new File(FILENAME_SERVTXT_31SRVTXT);
            scanFiles.put(FILENAME_SERVTXT_31SRVTXT, srv31);
        }
        
        new DBMessenger(DiapazonScan.class.getSimpleName()).info(MessageFormat.format("ScanFiles initial: \n{0}\n", new TForms().fromArray(scanFiles)));
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
