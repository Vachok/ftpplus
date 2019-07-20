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
import java.nio.file.Files;
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
        checkAlreadyExistingFiles();
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
    public String getExecution() { //fixme 20.07.2019 (19:24)
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
        throw new InvokeEmptyMethodException("18.07.2019 (18:48)");
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
    
    protected static void checkAlreadyExistingFiles() {
        try {
            for (File scanFile : Objects.requireNonNull(new File(ConstantsFor.ROOT_PATH_WITH_SEPARATOR).listFiles())) {
                String scanFileName = scanFile.getName();
                if (scanFile.canWrite() & scanFileName.contains("lan_")) {
                    scanFileFound(scanFile);
                }
            }
        }
        catch (NullPointerException e) {
            throw new ScanFilesException("No lan_ files found");
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
    
    private static void scanFileFound(@NotNull File scanFile) {
        StringBuilder sb = new StringBuilder();
        if (scanFile.length() < 6) {
            sb.append("File ").append(scanFile.getAbsolutePath()).append(" length is smaller that 6 bytes. Delete: ").append(scanFile.delete());
        }
        else {
            sb.append(copyToLanDir(scanFile));
        }
        messageToUser.info(DiapazonScan.class.getSimpleName(), "scanFileFound", sb.toString());
        
    }
    
    private static @NotNull String copyToLanDir(@NotNull File scanFile) {
        StringBuilder sb = new StringBuilder();
        String scanCopyFileName = scanFile.getName().replace(".txt", "_" + LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(3)) + ".scan");
        
        Path copyPath = Paths.get(ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + scanCopyFileName).toAbsolutePath();
        boolean isCopyOk = FileSystemWorker.copyOrDelFile(scanFile, copyPath, true);
        
        sb.append("->").append(scanFile.getAbsolutePath()).append(" (").append(scanFile.length() / ConstantsFor.KBYTE).append(" kilobytes)");
        sb.append(" copied: ").append(isCopyOk).append(" old must be delete!");
        if (scanFile.exists()) {
            scanFile.deleteOnExit();
        }
        return sb.toString();
    }
    
    private void theNewLan() {
        ThreadPoolTaskExecutor executor = thrConfig.getTaskExecutor();
        Runnable execScan200205 = new ExecScan(200, 205, "10.200.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_NEWLAN205));
        Runnable execScan205210 = new ExecScan(205, 210, "10.200.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_NEWLAN210));
        Runnable execScan210220 = new ExecScan(210, 215, "10.200.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_NEWLAN215));
        Runnable execScan213220 = new ExecScan(215, 219, "10.200.", DiapazonScan.ScanFilesWorker.scanFiles.get(FILENAME_NEWLAN220));
    
        executor.execute(execScan200205);
        executor.execute(execScan205210);
        executor.execute(execScan210220);
        executor.execute(execScan213220);
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
        executor.execute(this::theNewLan);
        executor.execute(this::scanServers);
        executor.execute(this::scanOldLan);
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
    
        @Contract(pure = true)
        private static @NotNull Map<String, File> getScanFiles() {
            return Collections.unmodifiableMap(scanFiles);
        }
    
        private static void makeFilesMap() throws IOException {
            checkAlreadyExistingFiles();
        
            File lan205 = new File(FILENAME_NEWLAN205);
            Files.createFile(lan205.toPath().toAbsolutePath().normalize());
            scanFiles.put(FILENAME_NEWLAN205, lan205);
        
            File lan210 = new File(FILENAME_NEWLAN210);
            Files.createFile(lan210.toPath().toAbsolutePath().normalize());
            scanFiles.put(FILENAME_NEWLAN210, lan210);
        
            File lan215 = new File(FILENAME_NEWLAN215);
            scanFiles.put(FILENAME_NEWLAN215, lan215);
            Files.createFile(lan215.toPath().toAbsolutePath().normalize());
        
            File lan220 = new File(FILENAME_NEWLAN220);
            scanFiles.put(FILENAME_NEWLAN220, lan220);
            Files.createFile(lan220.toPath().toAbsolutePath().normalize());
        
            File oldLan0 = new File(FILENAME_OLDLANTXT0);
            scanFiles.put(FILENAME_OLDLANTXT0, oldLan0);
            Files.createFile(oldLan0.toPath().toAbsolutePath().normalize());
        
            File oldLan1 = new File(FILENAME_OLDLANTXT1);
            scanFiles.put(FILENAME_OLDLANTXT1, oldLan1);
            Files.createFile(oldLan1.toPath().toAbsolutePath().normalize());
        
            File srv10 = new File(FILENAME_SERVTXT_10SRVTXT);
            scanFiles.put(FILENAME_SERVTXT_10SRVTXT, srv10);
            Files.createFile(srv10.toPath().toAbsolutePath().normalize());
        
            File srv21 = new File(FILENAME_SERVTXT_21SRVTXT);
            scanFiles.put(FILENAME_SERVTXT_21SRVTXT, srv21);
            Files.createFile(srv21.toPath().toAbsolutePath().normalize());
        
            File srv31 = new File(FILENAME_SERVTXT_31SRVTXT);
            scanFiles.put(FILENAME_SERVTXT_31SRVTXT, srv31);
            Files.createFile(srv31.toPath().toAbsolutePath().normalize());
            
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
    }
}
