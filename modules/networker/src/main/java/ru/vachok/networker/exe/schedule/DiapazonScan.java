// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.monitors.Pinger;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.exceptions.ScanFilesException;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.ExecScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.restapi.message.DBMessenger;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.sysinfo.ServiceInfoCtrl;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import static ru.vachok.networker.net.enums.ConstantsNet.*;


/**
 Скан диапазона адресов
 <p>
 
 @see ru.vachok.networker.exe.schedule.DiapazonScanTest
 @since 19.12.2018 (11:35) */
@SuppressWarnings("MagicNumber")
public class DiapazonScan implements Pinger {
    
    
    private static final MessageToUser messageToUser = new MessageLocal(DiapazonScan.class.getSimpleName());
    
    /**
     {@link ConstantsNet#getAllDevices()}
     */
    private final BlockingDeque<String> allDevLocalDeq = getAllDevices();
    
    private final ThreadConfig thrConfig = AppComponents.threadConfig();
    
    private final List<String> executionProcessLog = new ArrayList<>();
    
    /**
     Singleton inst
     */
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static DiapazonScan thisInst = new DiapazonScan();
    
    private long stopClassStampLong = NetScanFileWorker.getI().getLastStamp();
    
    protected DiapazonScan() {
    }
    
    public Map<String, File> editScanFiles() {
        if (DiapazonScan.ScanFilesWorker.scanFiles.size() != 9) {
            DiapazonScan.ScanFilesWorker.makeFilesMap();
        }
        return DiapazonScan.ScanFilesWorker.scanFiles;
    }
    
    /**
     SINGLETON
 
     @return single.
     */
    @Contract(pure = true)
    public static DiapazonScan getInstance() {
        return thisInst;
    }
    
    public long getStopClassStampLong() {
        return stopClassStampLong;
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
            for (Map.Entry<String, File> entry : DiapazonScan.ScanFilesWorker.scanFiles.entrySet()) {
                fileTimes.append(entry.getKey()).append(atStr).append(Paths.get(entry.getValue().getName()).toFile().length()).append("<br>\n");
            }
        }
        catch (NullPointerException e) {
            messageToUser.info("NO FILES!");
        }
        StringBuilder sb = new StringBuilder("DiapazonScan. Running ");
        sb.append(TimeUnit.MILLISECONDS.toMinutes(DiapazonScan.ScanFilesWorker.getRunMin()));
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
        sb.append(" ROOT_PATH_STR= ").append(DiapazonScan.ScanFilesWorker.ROOT_PATH_STR);
        sb.append("<br><b>\nfileTimes= </b><br>").append(fileTimes);
        return sb.toString();
    }
    
    @Override
    public Runnable getMonitoringRunnable() {
        return this;
    }
    
    @Override
    public String getStatistics() {
        throw new InvokeEmptyMethodException("15.07.2019 (15:28)");
    }
    
    @Override
    public String getExecution() {
        return new TForms().fromArray(executionProcessLog);
    }
    
    @Override
    public String getPingResultStr() {
        return theInfoToString();
    }
    
    @Override
    public boolean isReach(String inetAddrStr) {
        throw new InvokeEmptyMethodException("13.07.2019 (2:29)");
    }
    
    @Override
    public String writeLogToFile() {
        throw new InvokeEmptyMethodException("13.07.2019 (2:22)");
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("DiapazonScan{");
        sb.append(theInfoToString()).append("<p>").append(new AppComponents().scanOnline());
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public void run() {
        DiapazonScan.ScanFilesWorker.makeFilesMap();
        startDo();
    }
    
    protected BlockingDeque<String> getAllDevLocalDeq() {
        return allDevLocalDeq;
    }
    
    protected List<String> getExecutionProcessLog() {
        return executionProcessLog;
    }
    
    protected void checkAlreadyExistingFiles() {
        try {
            for (File scanFile : Objects.requireNonNull(new File(ConstantsFor.ROOT_PATH_WITH_SEPARATOR).listFiles())) {
                String scanFileName = scanFile.getName();
                if (scanFile.canWrite() & scanFileName.contains("lan_")) {
                    scanFileFound(scanFile);
                }
            }
        }
        catch (NullPointerException e) {
            throw new ScanFilesException();
        }
    }
    
    @Contract(" -> new")
    protected @NotNull ExecScan[] getRunnables() {
        return new ExecScan[]{
            new ExecScan(10, 20, "10.10.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_SERVTXT_10SRVTXT)),
            new ExecScan(21, 31, "10.10.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_SERVTXT_21SRVTXT)),
            new ExecScan(31, 41, "10.10.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_SERVTXT_31SRVTXT)),
        };
    }
    
    private void scanFileFound(@NotNull File scanFile) {
    
        StringBuilder sb = new StringBuilder();
        if (scanFile.length() < 3) {
            sb.append("File ").append(scanFile.getAbsolutePath()).append(" length is smaller that 10 bytes. Delete: ").append(scanFile.delete());
        }
        else {
            sb.append(copyToLanDir(scanFile));
        }
        messageToUser.info(this.getClass().getSimpleName(), "scanFileFound", sb.toString());
    
    }
    
    private @NotNull String copyToLanDir(@NotNull File scanFile) {
        StringBuilder sb = new StringBuilder();
        String scanCopyFileName = scanFile.getName().replace("\\Q.txt\\E", "_" + LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(3)) + ".scan");
        
        Path copyPath = Paths.get(ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + scanCopyFileName).toAbsolutePath();
        boolean isCopyOk = FileSystemWorker.copyOrDelFile(scanFile, copyPath, true);
        
        sb.append("->").append(scanFile.getAbsolutePath()).append(" (").append(scanFile.length() / ConstantsFor.KBYTE).append(" kilobytes)");
        sb.append(" copied: ").append(isCopyOk).append(" old must be delete!");
        return sb.toString();
    }
    
    private void theNewLan() {
        Runnable execScan200205 = new ExecScan(200, 205, "10.200.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_NEWLAN205));
        Runnable execScan205210 = new ExecScan(205, 210, "10.200.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_NEWLAN210));
        Runnable execScan210220 = new ExecScan(210, 215, "10.200.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_NEWLAN215));
        Runnable execScan213220 = new ExecScan(215, 219, "10.200.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_NEWLAN220));
    
        thrConfig.execByThreadConfig(execScan200205);
        thrConfig.execByThreadConfig(execScan205210);
        thrConfig.execByThreadConfig(execScan210220);
        thrConfig.execByThreadConfig(execScan213220);
    }
    
    private void setScanInMin() {
        if (allDevLocalDeq.remainingCapacity() > 0 && TimeUnit.MILLISECONDS.toMinutes(DiapazonScan.ScanFilesWorker.getRunMin()) > 0 && allDevLocalDeq.size() > 0) {
            long scansItMin = allDevLocalDeq.size() / TimeUnit.MILLISECONDS.toMinutes(DiapazonScan.ScanFilesWorker.getRunMin());
            Preferences pref = AppComponents.getUserPref();
            pref.put(ConstantsFor.PR_SCANSINMIN, String.valueOf(scansItMin));
            messageToUser.warn(getClass().getSimpleName(), DiapazonScan.ScanFilesWorker.METHNAME_SCANSINMIN, " = " + scansItMin);
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
            allDevLocalDeq.clear();
            DiapazonScan.ScanFilesWorker.makeFilesMap();
        }
        thrConfig.execByThreadConfig(this::theNewLan);
        thrConfig.execByThreadConfig(this::scanServers);
        thrConfig.execByThreadConfig(this::scanOldLan);
        
        thrConfig.getTaskScheduler().getScheduledThreadPoolExecutor().scheduleAtFixedRate(this::setScanInMin, 3, 5, TimeUnit.MINUTES);
    }
    
    /**
     192.168.11-14.254
     */
    @SuppressWarnings("MagicNumber")
    private void scanOldLan() {
        Runnable execScanOld0 = new ExecScan(11, 16, "192.168.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_OLDLANTXT0));
        Runnable execScanOld1 = new ExecScan(16, 21, "192.168.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_OLDLANTXT1));
        
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
    
    private static final class ScanFilesWorker {
        
        
        /**
         Корень директории.
         */
        private static final String ROOT_PATH_STR = Paths.get("").toAbsolutePath().toString();
        
        private static final Pattern COMPILE = Pattern.compile("\\Q.txt\\E", Pattern.LITERAL);
        
        private static final String METHNAME_SCANSINMIN = "scansItMin";
        
        private static Map<String, File> scanFiles = new ConcurrentHashMap<>();
        
        private static Map<String, File> getScanFiles() {
            return Collections.unmodifiableMap(scanFiles);
        }
        
        private static void makeFilesMap() {
            
            getInstance().checkAlreadyExistingFiles();
            
            scanFiles.put(FILENAME_NEWLAN205, new File(FILENAME_NEWLAN205));
            scanFiles.put(FILENAME_NEWLAN210, new File(FILENAME_NEWLAN210));
            scanFiles.put(FILENAME_NEWLAN215, new File(FILENAME_NEWLAN215));
            scanFiles.put(FILENAME_NEWLAN220, new File(FILENAME_NEWLAN220));
            
            scanFiles.put(FILENAME_OLDLANTXT0, new File(FILENAME_OLDLANTXT0));
            scanFiles.put(FILENAME_OLDLANTXT1, new File(FILENAME_OLDLANTXT1));
            
            scanFiles.put(FILENAME_SERVTXT_10SRVTXT, new File(FILENAME_SERVTXT_10SRVTXT));
            scanFiles.put(FILENAME_SERVTXT_21SRVTXT, new File(FILENAME_SERVTXT_21SRVTXT));
            scanFiles.put(FILENAME_SERVTXT_31SRVTXT, new File(FILENAME_SERVTXT_31SRVTXT));
            
            new DBMessenger(DiapazonScan.class.getSimpleName()).info(MessageFormat.format("ScanFiles initial: \n{0}\n", new TForms().fromArray(scanFiles)));
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
    }
}
