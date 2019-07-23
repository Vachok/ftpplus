// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.abstr.monitors.PingerService;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.ExecScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

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
    
    private static NetKeeper scanFilesKeeper = new ScanFilesWorker();
    
    /**
     Singleton inst
     */
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static DiapazonScan thisInst = new DiapazonScan();
    
    private long stopClassStampLong = NetScanFileWorker.getI().getLastStamp();
    
    protected DiapazonScan() {
    }
    
    @Contract(pure = true)
    public static List<String> getCurrentPingStats() {
        return scanFilesKeeper.getCurrentScanLists();
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
            for (File scanFile : scanFilesKeeper.getCurrentScanFiles()) {
                fileTimes.append(scanFile.getAbsolutePath()).append(atStr).append(scanFile.length()).append("<br>\n");
            }
        }
        catch (NullPointerException e) {
            messageToUser.info("NO FILES!");
        }
        StringBuilder sb = new StringBuilder("DiapazonScan. Running ");
        sb.append(TimeUnit.MILLISECONDS.toMinutes(ScanFilesWorker.getRunMin()));
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
        sb.append(" ROOT_PATH_STR= ").append(ConstantsFor.ROOT_PATH_WITH_SEPARATOR);
        sb.append("<br><b>\nfileTimes= </b><br>").append(fileTimes).append("<br>");
        sb.append(ConstantsFor.TOSTRING_EXECUTOR).append(thrConfig.toString());
        return sb.toString();
    }
    
    @Override
    public String getPingResultStr() {
        List<String> lists = scanFilesKeeper.getCurrentScanLists();
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
        Map<String, File> scanFiles = ScanFilesWorker.getScanFiles();
        return new ExecScan[]{
            new ExecScan(10, 20, "10.10.", scanFiles.get(FILENAME_SERVTXT_10SRVTXT)),
            new ExecScan(21, 31, "10.10.", scanFiles.get(FILENAME_SERVTXT_21SRVTXT)),
            new ExecScan(31, 41, "10.10.", scanFiles.get(FILENAME_SERVTXT_31SRVTXT)),
            new ExecScan(11, 16, "192.168.", scanFiles.get(FILENAME_OLDLANTXT0)),
            new ExecScan(16, 21, "192.168.", scanFiles.get(FILENAME_OLDLANTXT1)),
            new ExecScan(200, 205, "10.200.", scanFiles.get(FILENAME_NEWLAN205)),
            new ExecScan(205, 210, "10.200.", scanFiles.get(FILENAME_NEWLAN210)),
            new ExecScan(210, 215, "10.200.", scanFiles.get(FILENAME_NEWLAN215)),
            new ExecScan(215, 219, "10.200.", scanFiles.get(FILENAME_NEWLAN220)),
        };
    }
    
    private void setScanInMin() {
        if (allDevLocalDeq.remainingCapacity() > 0 && TimeUnit.MILLISECONDS.toMinutes(ScanFilesWorker.getRunMin()) > 0 && allDevLocalDeq.size() > 0) {
            long scansItMin = allDevLocalDeq.size() / TimeUnit.MILLISECONDS.toMinutes(ScanFilesWorker.getRunMin());
            Preferences pref = AppComponents.getUserPref();
            pref.put(ConstantsFor.PR_SCANSINMIN, String.valueOf(scansItMin));
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
    
}
