// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.Keeper;
import ru.vachok.networker.abstr.monitors.PingerService;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.exceptions.ScanFilesException;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.ExecScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.restapi.message.DBMessenger;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
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
public class DiapazonScan implements PingerService {
    
    
    private static final MessageToUser messageToUser = new MessageLocal(DiapazonScan.class.getSimpleName());
    
    /**
     {@link ConstantsNet#getAllDevices()}
     */
    private final BlockingDeque<String> allDevLocalDeq = getAllDevices();
    
    private final ThreadConfig thrConfig = AppComponents.threadConfig();
    
    private static List<String> currentPingStats = new DiapazonScan.ScanFilesWorker().getCurrentScanLists();
    
    /**
     Singleton inst
     */
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static DiapazonScan thisInst = new DiapazonScan();
    
    private long stopClassStampLong = NetScanFileWorker.getI().getLastStamp();
    
    protected DiapazonScan() {
        if (DiapazonScan.ScanFilesWorker.scanFiles.size() != 9) {
            try {
                DiapazonScan.ScanFilesWorker.makeFilesMap();
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
    }
    
    @Contract(pure = true)
    public static List<String> getCurrentPingStats() {
        return currentPingStats;
    }
    
    public Map<String, File> editScanFiles() {
        if (DiapazonScan.ScanFilesWorker.scanFiles.size() != 9) {
            try {
                DiapazonScan.ScanFilesWorker.makeFilesMap();
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
        return DiapazonScan.ScanFilesWorker.scanFiles;
    }
    
    public static @NotNull Map<String, File> getScanFiles() {
        if (DiapazonScan.ScanFilesWorker.scanFiles.size() != 9) {
            try {
    
                DiapazonScan.ScanFilesWorker.makeFilesMap();
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
        return Collections.unmodifiableMap(DiapazonScan.ScanFilesWorker.scanFiles);
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
    
    @Override
    public Runnable getMonitoringRunnable() {
        return this;
    }
    
    @Override
    public String getStatistics() {
        StringBuilder stringBuilder = new StringBuilder();
    
        RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
        stringBuilder.append(mxBean.getClass().getTypeName()).append(":\n");
        stringBuilder.append(TimeUnit.MILLISECONDS.toMinutes(mxBean.getUptime())).append(" Uptime\n");
        stringBuilder.append(mxBean.getName()).append(" Name\n");
        stringBuilder.append(mxBean.getInputArguments()).append(" InputArguments\n");
        stringBuilder.append(mxBean.getSpecName()).append(" SpecName\n");
        stringBuilder.append(mxBean.getSpecVersion()).append(" SpecVersion\n");
        stringBuilder.append(mxBean.getVmVersion()).append(" VmVersion\n");
        stringBuilder.append(mxBean.getManagementSpecVersion()).append(" ManagementSpecVersion\n");
        stringBuilder.append(mxBean.getVmName()).append(" VmName\n");
        stringBuilder.append(mxBean.getVmVendor()).append(" VmVendor\n");
    
        return stringBuilder.toString();
    }
    
    @Override
    public String getExecution() {
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
        sb.append("<br><b>\nfileTimes= </b><br>").append(fileTimes).append("<br>");
        sb.append(ConstantsFor.TOSTRING_EXECUTOR).append(thrConfig.toString());
        return sb.toString();
    }
    
    @Override
    public String getPingResultStr() {
        Keeper keeper = new NetScanFileWorker();
        List<String> lists = keeper.getCurrentScanLists();
        Collections.sort(lists);
        return new TForms().fromArray(lists, true);
    }
    
    @Override
    public List<String> pingDevices(Map<InetAddress, String> ipAddressAndDeviceNameToShow) {
        List<String> pingedDevices = new ArrayList<>(ipAddressAndDeviceNameToShow.size());
        ipAddressAndDeviceNameToShow.keySet().forEach(key->{
            boolean reachKey = isReach(key);
            if (reachKey) {
                pingedDevices.add(MessageFormat.format("Computer {0} is reachable. Timeout {1}",
                    ipAddressAndDeviceNameToShow.get(key), ConstantsFor.TIMEOUT_650));
            }
            else {
                pingedDevices.add(MessageFormat.format("Computer {0} is UNREACHABLE. Timeout {1}",
                    ipAddressAndDeviceNameToShow.get(key), ConstantsFor.TIMEOUT_650));
            }
    
        });
        return pingedDevices;
    }
    
    @Override
    public boolean isReach(InetAddress inetAddrStr) {
        try {
            return inetAddrStr.isReachable(ConstantsFor.TIMEOUT_650);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("DiapazonScan.isReach: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return false;
        }
    }
    
    @Override
    public String writeLogToFile() {
        throw new InvokeEmptyMethodException("13.07.2019 (2:22)");
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DiapazonScan{");
        sb.append(getExecution()).append("<p>").append(new AppComponents().scanOnline());
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
    
    @Contract(" -> new")
    protected @NotNull ExecScan[] getRunnables() {
        return new ExecScan[]{
            new ExecScan(10, 20, "10.10.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_SERVTXT_10SRVTXT)),
            new ExecScan(21, 31, "10.10.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_SERVTXT_21SRVTXT)),
            new ExecScan(31, 41, "10.10.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_SERVTXT_31SRVTXT)),
            new ExecScan(11, 16, "192.168.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_OLDLANTXT0)),
            new ExecScan(16, 21, "192.168.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_OLDLANTXT1)),
            new ExecScan(200, 205, "10.200.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_NEWLAN205)),
            new ExecScan(205, 210, "10.200.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_NEWLAN210)),
            new ExecScan(210, 215, "10.200.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_NEWLAN215)),
            new ExecScan(215, 219, "10.200.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_NEWLAN220)),
        };
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
        ThreadPoolTaskExecutor executor = thrConfig.getTaskExecutor();
        if (allDevLocalDeq.remainingCapacity() == 0) {
            allDevLocalDeq.clear();
        }
        scanServers();
    }
    
    /**
     Скан подсетей 10.10.xx.xxx
     */
    private void scanServers() {
        thrConfig.getTaskScheduler().getScheduledThreadPoolExecutor().scheduleAtFixedRate(this::setScanInMin, 3, 5, TimeUnit.MINUTES);
        for (ExecScan r : getRunnables()) {
            thrConfig.getTaskExecutor().execute(r);
        }
    }
    
    protected static final class ScanFilesWorker implements Keeper {
        
        
        /**
         Корень директории.
         */
        private static final String ROOT_PATH_STR = Paths.get("").toAbsolutePath().toString();
        
        private static final Pattern COMPILE = Pattern.compile("\\Q.txt\\E", Pattern.LITERAL);
        
        private static final String METHNAME_SCANSINMIN = "scansItMin";
        
        private static Map<String, File> scanFiles = new ConcurrentHashMap<>();
        
        @Override
        public Deque<InetAddress> getOnlineDevicesInetAddress() {
            return new NetScanFileWorker().getOnlineDevicesInetAddress();
        }
        
        @Override
        public List<String> getCurrentScanLists() {
            try {
                if (scanFiles.size() != 9) {
                    makeFilesMap();
                }
            }
            catch (IOException e) {
                messageToUser.error(MessageFormat
                    .format("ScanFilesWorker.getCurrentScanLists {0} - {1}\nParameters: []\nReturn: java.util.List<java.lang.String>\nStack:\n{2}", e.getClass()
                        .getTypeName(), e.getMessage(), new TForms().fromArray(e)));
            }
            List<String> currentScanFiles = new ArrayList<>();
            for (File file : scanFiles.values()) {
                currentScanFiles.addAll(FileSystemWorker.readFileToList(file.getAbsolutePath()));
            }
            return currentScanFiles;
        }
        
        private static void checkAlreadyExistingFiles() {
            try {
                for (File scanFile : Objects.requireNonNull(new File(ConstantsFor.ROOT_PATH_WITH_SEPARATOR).listFiles())) {
                    String scanFileName = scanFile.getName();
                    if (scanFile.length() > 0 & scanFileName.contains("lan_")) {
                        messageToUser.info(copyToLanDir(scanFile));
                    }
                }
            }
            catch (NullPointerException e) {
                throw new ScanFilesException("No lan_ files found");
            }
        }
    
        @Contract(pure = true)
        private static @NotNull Map<String, File> getScanFiles() {
            return Collections.unmodifiableMap(scanFiles);
        }
        
        private static void makeFilesMap() throws IOException {
            checkAlreadyExistingFiles();
            
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
            
            new DBMessenger(DiapazonScan.class.getSimpleName()).info(MessageFormat.format("ScanFiles initial: \n{0}\n", new TForms().fromArray(scanFiles)));
        }
        
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
}
