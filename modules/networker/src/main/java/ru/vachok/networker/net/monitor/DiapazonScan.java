// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ConstantsNet;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.info.NetScanService;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @see ru.vachok.networker.net.monitor.DiapazonScanTest
 @since 19.12.2018 (11:35) */
@SuppressWarnings("MagicNumber")
public class DiapazonScan implements NetScanService {
    
    
    private static final MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
        .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, DiapazonScan.class.getSimpleName());
    
    /**
     {@link NetKeeper#getAllDevices()}
     */
    private final BlockingDeque<String> allDevLocalDeq = NetKeeper.getAllDevices();
    
    private final ThreadConfig thrConfig;
    
    /**
     Singleton inst
     */
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static DiapazonScan thisInst = new DiapazonScan();
    
    private List<String> pingedDevices = new ArrayList<>();
    
    private long stopClassStampLong = AppComponents.getUserPref().getLong(this.getClass().getSimpleName(), System.currentTimeMillis());
    
    protected DiapazonScan() {
        thrConfig = AppComponents.threadConfig();
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
    public String writeLog() {
        return FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".log", MessageFormat.format("{0}\n{1}", this.getPingResultStr(), this.getStatistics()));
    }
    
    @Override
    public String getPingResultStr() {
        List<String> lists = NetKeeper.getCurrentScanLists();
        Collections.sort(lists);
        return new TForms().fromArray(lists, true);
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
    public String toString() {
        final StringBuilder sb = new StringBuilder("DiapazonScan{");
        sb.append(getExecution()).append("<p>").append(new AppComponents().scanOnline());
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String getExecution() {
        StringBuilder fileTimes = new StringBuilder();
        try {
            String atStr = " size in bytes: ";
            for (File scanFile : NetKeeper.getCurrentScanFiles()) {
                fileTimes.append(scanFile.getAbsolutePath()).append(atStr).append(scanFile.length()).append("<br>\n");
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
        sb.append(ConstantsNet.IPS_IN_VELKOM_VLAN);
        sb.append("(");
        try {
            sb.append(BigDecimal.valueOf(allDevLocalDeq.size()).divide((BigDecimal.valueOf((ConstantsNet.IPS_IN_VELKOM_VLAN) / 100)), 3, RoundingMode.HALF_DOWN));
        }
        catch (ArithmeticException e) {
            sb.append((float) (allDevLocalDeq.size()) / (float) (ConstantsNet.IPS_IN_VELKOM_VLAN / 100));
        }
        sb.append(" %)").append("</a>}");
        sb.append(" ROOT_PATH_STR= ").append(ConstantsFor.ROOT_PATH_WITH_SEPARATOR);
        sb.append("<br><b>\nfileTimes= </b><br>").append(fileTimes).append("<br>");
        sb.append(ConstantsFor.TOSTRING_EXECUTOR).append(thrConfig.toString());
        return sb.toString();
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
    
    @Override
    public void run() {
        Thread start = new Thread(this::startDo);
        start.run();
    }
    
    BlockingDeque<String> getAllDevLocalDeq() {
        return allDevLocalDeq;
    }
    
    @Contract(" -> new")
    private @NotNull ExecScan[] getRunnables() {
        Map<String, File> scanFiles = NetKeeper.getScanFiles();
        return new ExecScan[]{
            new ExecScan(10, 20, "10.10.", scanFiles.get(FileNames.NEWLAN215)),
            new ExecScan(21, 31, "10.10.", scanFiles.get(FileNames.SERVTXT_21SRVTXT)),
            new ExecScan(31, 41, "10.10.", scanFiles.get(FileNames.SERVTXT_31SRVTXT)),
            new ExecScan(11, 16, "192.168.", scanFiles.get(FileNames.OLDLANTXT0)),
            new ExecScan(16, 21, "192.168.", scanFiles.get(FileNames.OLDLANTXT1)),
            new ExecScan(200, 205, "10.200.", scanFiles.get(FileNames.NEWLAN205)),
            new ExecScan(205, 210, "10.200.", scanFiles.get(FileNames.NEWLAN210)),
            new ExecScan(210, 215, "10.200.", scanFiles.get(FileNames.NEWLAN215)),
            new ExecScan(215, 219, "10.200.", scanFiles.get(FileNames.NEWLAN220)),
        };
    }
    
    private void startDo() {
        if (allDevLocalDeq.remainingCapacity() == 0) {
            allDevLocalDeq.clear();
        }
        
        ThreadPoolExecutor threadExecutor = thrConfig.getTaskExecutor().getThreadPoolExecutor();
        BlockingQueue<Runnable> queueExec = threadExecutor.getQueue();
        for (Runnable runnable : queueExec) {
            if (runnable instanceof ExecScan) {
                queueExec.poll();
            }
        }
        
        @NotNull ExecScan[] newRunnables = getRunnables();
        for (ExecScan execScan : newRunnables) {
            threadExecutor.submit(execScan);
        }
        scheduleTimeSetterScanMin();
    }
    
    private void scheduleTimeSetterScanMin() {
        ScheduledThreadPoolExecutor schedExecutor = thrConfig.getTaskScheduler().getScheduledThreadPoolExecutor();
        schedExecutor.scheduleAtFixedRate(this::setScanInMin, 3, 5, TimeUnit.MINUTES);
    }
    
    private void setScanInMin() {
        if (allDevLocalDeq.remainingCapacity() > 0 && TimeUnit.MILLISECONDS.toMinutes(getRunMin()) > 0 && allDevLocalDeq.size() > 0) {
            long scansItMin = allDevLocalDeq.size() / TimeUnit.MILLISECONDS.toMinutes(getRunMin());
            Preferences pref = AppComponents.getUserPref();
            pref.put(PropertiesNames.SCANSINMIN, String.valueOf(scansItMin));
            try {
                pref.sync();
            }
            catch (BackingStoreException e) {
                messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".setScanInMin", e));
            }
        }
    }
}
