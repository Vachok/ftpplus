// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.ExecScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.sysinfo.ServiceInfoCtrl;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.vachok.networker.net.enums.ConstantsNet.*;


/**
 Скан диапазона адресов
 <p>
 
 @see ru.vachok.networker.exe.schedule.DiapazonScanTest
 @since 19.12.2018 (11:35) */
@SuppressWarnings("MagicNumber")
public class DiapazonScan implements Runnable {
    
    
    /**
     Корень директории.
     */
    private static final String ROOT_PATH_STR = Paths.get("").toAbsolutePath().toString();
    
    private static final MessageToUser messageToUser = new MessageLocal(DiapazonScan.class.getSimpleName());
    
    private static final Pattern COMPILE = Pattern.compile("\\Q.txt\\E", Pattern.LITERAL);
    
    /**
     {@link ConstantsNet#getAllDevices()}
     */
    private final BlockingDeque<String> allDevLocalDeq = getAllDevices();
    
    private final ThreadConfig thrConfig = AppComponents.threadConfig();
    
    /**
     Singleton inst
     */
    private static DiapazonScan thisInst = new DiapazonScan();
    
    private List<String> executionProcessLog = new ArrayList<>();
    
    private long stopClassStampLong = NetScanFileWorker.getI().getLastStamp();
    
    private Map<String, File> scanFiles = makeFilesMap();
    
    protected DiapazonScan() {
    }
    
    public Map<String, File> getScanFiles() {
        return Collections.unmodifiableMap(scanFiles);
    
    }
    
    /**
     SINGLETON
 
     @return single.
     */
    public static DiapazonScan getInstance() {
        return thisInst;
    }
    
    public long getStopClassStampLong() {
        return stopClassStampLong;
    }
    
    /**
     Контролер пинг-экзекуторов
     <p>
     Свичи начала сегментов. Вкл. в оптическое ядро.
     
     @return лист важного оборудования
     
     @throws IllegalAccessException swF.get(swF).toString()
     */
    public static List<String> pingSwitch() throws IllegalAccessException {
        Field[] swFields = SwitchesWiFi.class.getFields();
        List<String> swList = new ArrayList<>();
        for (Field swF : swFields) {
            String ipAddrStr = swF.get(swF).toString();
            swList.add(ipAddrStr);
        }
        return swList;
    }
    
    /**
     Чтобы случайно не уничтожить Overridden {@link #toString()}
     <p>
 
     @return информация о состоянии файлов {@code DiapazonScan. Start at} ...для {@link ServiceInfoCtrl} .
     */
    public String theInfoToString() {
        StringBuilder fileTimes = new StringBuilder();
        try {
            String atStr = " size in bytes: ";
            for (Map.Entry<String, File> entry : scanFiles.entrySet()) {
                fileTimes.append(entry.getKey()).append(atStr).append(Paths.get(entry.getValue().getName()).toFile().length()).append("<br>\n");
            }
        }
        catch (NullPointerException e) {
            messageToUser.info("NO FILES!");
        }
        StringBuilder sb = new StringBuilder("DiapazonScan. Running ");
        sb.append(TimeUnit.MILLISECONDS.toMinutes(getRunMin()));
        sb.append(" min ");
        sb.append("{ ");
        sb.append("<a href=\"/showalldev\">ALL_DEVICES ");
        sb.append(allDevLocalDeq.size());
        sb.append("/");
        sb.append(IPS_IN_VELKOM_VLAN);
        sb.append("(");
        try {
            sb.append(BigDecimal.valueOf(allDevLocalDeq.size()).divide((BigDecimal.valueOf((IPS_IN_VELKOM_VLAN) / 100)), 3, RoundingMode.HALF_DOWN));
        }
        catch (ArithmeticException e) {
            sb.append((float) (allDevLocalDeq.size()) / (float) (IPS_IN_VELKOM_VLAN / 100));
        }
        sb.append(" %)").append("</a>}");
        sb.append(" ROOT_PATH_STR= ").append(ROOT_PATH_STR);
        sb.append("<br><b>\nfileTimes= </b><br>").append(fileTimes);
        return sb.toString();
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("DiapazonScan{");
        sb.append(theInfoToString()).append("<p>").append(new AppComponents().scanOnline());
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public void run() {
        startDo();
    }
    
    protected BlockingDeque<String> getAllDevLocalDeq() {
        return allDevLocalDeq;
    }
    
    protected List<String> getExecutionProcessLog() {
        return executionProcessLog;
    }
    
    private ExecScan[] getRunnables() {
        return new ExecScan[]{
            new ExecScan(10, 20, "10.10.", scanFiles.get(FILENAME_SERVTXT_10SRVTXT)),
            new ExecScan(21, 31, "10.10.", scanFiles.get(FILENAME_SERVTXT_21SRVTXT)),
            new ExecScan(31, 41, "10.10.", scanFiles.get(FILENAME_SERVTXT_31SRVTXT)),
        };
    }
    
    private Map<String, File> makeFilesMap() {
        Map<String, File> scanLanNamesFilesMap = new ConcurrentHashMap<>();
    
        checkAlreadyExistingFiles(scanLanNamesFilesMap);
    
        scanLanNamesFilesMap.putIfAbsent(FILENAME_NEWLAN205, new File(FILENAME_NEWLAN205));
        scanLanNamesFilesMap.putIfAbsent(FILENAME_NEWLAN205, new File(FILENAME_NEWLAN210));
        scanLanNamesFilesMap.putIfAbsent(FILENAME_NEWLAN213, new File(FILENAME_NEWLAN213));
        scanLanNamesFilesMap.putIfAbsent(FILENAME_NEWLAN220, new File(FILENAME_NEWLAN220));
    
        scanLanNamesFilesMap.putIfAbsent(FILENAME_OLDLANTXT0, new File(FILENAME_OLDLANTXT0));
        scanLanNamesFilesMap.putIfAbsent(FILENAME_OLDLANTXT1, new File(FILENAME_OLDLANTXT1));
    
        scanLanNamesFilesMap.putIfAbsent(FILENAME_SERVTXT_10SRVTXT, new File(FILENAME_SERVTXT_10SRVTXT));
        scanLanNamesFilesMap.putIfAbsent(FILENAME_SERVTXT_21SRVTXT, new File(FILENAME_SERVTXT_21SRVTXT));
        scanLanNamesFilesMap.putIfAbsent(FILENAME_SERVTXT_31SRVTXT, new File(FILENAME_SERVTXT_31SRVTXT));
        return scanLanNamesFilesMap;
    }
    
    private void checkAlreadyExistingFiles(Map<String, File> scanLanNamesFilesMap) {
        try {
            for (File scanFile : Objects.requireNonNull(new File(ConstantsFor.ROOT_PATH_WITH_SEPARATOR).listFiles())) {
                if (scanFile.getName().contains("lan_")) {
                    scanLanNamesFilesMap.put(scanFile.getName(), scanFile);
                }
            }
        }
        catch (NullPointerException e) {
            System.err.println("scanLanNamesFilesMap.size() = " + scanLanNamesFilesMap.size());
        }
    }
    
    private void theNewLan() {
        Runnable execScan200205 = new ExecScan(200, 205, "10.200.", scanFiles.get(FILENAME_NEWLAN205));
        Runnable execScan205210 = new ExecScan(205, 210, "10.200.", scanFiles.get(FILENAME_NEWLAN210));
        Runnable execScan210220 = new ExecScan(210, 213, "10.200.", scanFiles.get(FILENAME_NEWLAN213));
        Runnable execScan213220 = new ExecScan(213, 219, "10.200.", scanFiles.get(FILENAME_NEWLAN220));
    
        thrConfig.execByThreadConfig(execScan200205);
        thrConfig.execByThreadConfig(execScan205210);
        
        thrConfig.execByThreadConfig(execScan210220);
        thrConfig.execByThreadConfig(execScan213220);
    }
    
    private void setScanInMin() {
        if (allDevLocalDeq.remainingCapacity() > 0 && TimeUnit.MILLISECONDS.toMinutes(getRunMin()) > 0 && allDevLocalDeq.size() > 0) {
            long scansItMin = allDevLocalDeq.size() / TimeUnit.MILLISECONDS.toMinutes(getRunMin());
            Preferences pref = AppComponents.getUserPref();
            pref.put(ConstantsFor.PR_SCANSINMIN, String.valueOf(scansItMin));
            messageToUser.warn(getClass().getSimpleName(), "scansItMin", " = " + scansItMin);
            try {
                pref.sync();
            }
            catch (BackingStoreException e) {
                messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".setScanInMin", e));
            }
        }
    }
    
    private void startDo() {
        if (allDevLocalDeq.remainingCapacity() == 0) {
            scanFiles.values().stream().forEach(this::copyOldScans);
            allDevLocalDeq.clear();
        }
    
        thrConfig.execByThreadConfig(this::theNewLan);
        thrConfig.execByThreadConfig(this::scanServers);
        thrConfig.execByThreadConfig(this::scanOldLan);
        
        thrConfig.getTaskScheduler().getScheduledThreadPoolExecutor().scheduleAtFixedRate(this::setScanInMin, 3, 5, TimeUnit.MINUTES);
    }
    
    /**
     @return {@link ExecScan} (from [10,21,31,41] to [20,31,41,51]) запрос из {@link #theInfoToString()}
     */
    private static long getRunMin() {
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
    
    /**
     192.168.11-14.254
     */
    @SuppressWarnings("MagicNumber")
    private void scanOldLan() {
        Runnable execScanOld0 = new ExecScan(11, 16, "192.168.", scanFiles.get(FILENAME_OLDLANTXT0));
        Runnable execScanOld1 = new ExecScan(16, 21, "192.168.", scanFiles.get(FILENAME_OLDLANTXT1));
        
        AppComponents.threadConfig().execByThreadConfig(execScanOld0);
        AppComponents.threadConfig().execByThreadConfig(execScanOld1);
    }
    
    /**
     Скан подсетей 10.10.xx.xxx
     */
    private void scanServers() {
        for (ExecScan r : getRunnables()) {
            thrConfig.execByThreadConfig(r);
        }
    }
    
    private void copyOldScans(File oldScanFile) {
        String newName = ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + COMPILE.matcher(oldScanFile.getName())
            .replaceAll(Matcher.quoteReplacement("_" + LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(3)))) + ".scan";
        
        File newFile = new File(newName);
        FileSystemWorker.copyOrDelFile(oldScanFile, Paths.get(newFile.getAbsolutePath()).toAbsolutePath().normalize(), true);
        messageToUser.info(getClass().getSimpleName() + ".startDo", "newFile", " = " + newFile.getAbsolutePath());
    }
}
