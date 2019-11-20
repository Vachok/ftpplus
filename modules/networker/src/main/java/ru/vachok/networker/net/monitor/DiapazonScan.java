// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AbstractForms;
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
import ru.vachok.networker.info.stats.Stats;
import ru.vachok.networker.restapi.props.InitProperties;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


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
    
    private final File inetUniqCSV = new File(FileNames.INETSTATSIP_CSV);
    
    
    private final AppConfigurationLocal thrConfig;
    
    /**
     Singleton inst
     */
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static DiapazonScan thisInst = new DiapazonScan();
    
    private long stopClassStampLong = ru.vachok.networker.restapi.props.InitProperties.getUserPref().getLong(this.getClass().getSimpleName(), System.currentTimeMillis());
    
    /**
     SINGLETON
 
     @return single.
     */
    @Contract(pure = true)
    public static DiapazonScan getInstance() {
        return thisInst;
    }
    
    private long getStopClassStampLong() {
        return stopClassStampLong;
    }
    
    BlockingDeque<String> getAllDevLocalDeq() {
        return allDevLocalDeq;
    }
    
    protected DiapazonScan() {
        thrConfig = AppComponents.threadConfig();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DiapazonScan{");
        sb.append(getExecution()).append("<p>").append(NetScanService.getInstance(NetScanService.SCAN_ONLINE).toString());
        try {
            sb.append(new Date(getStopClassStampLong()));
        }
        catch (RuntimeException e) {
            sb.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
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
        sb.append(TimeUnit.MILLISECONDS.toMinutes(InitProperties.getUserPref().getLong(ExecScan.class.getSimpleName(), 0)));
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
    
    @Override
    public String getPingResultStr() {
        List<String> lists = NetKeeper.getCurrentScanLists();
        Collections.sort(lists);
        return new TForms().fromArray(lists, true);
    }
    
    @Override
    public String writeLog() {
        return FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".log", MessageFormat.format("{0}\n{1}", this.getPingResultStr(), this.getStatistics()));
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
    public void run() {
        AppConfigurationLocal.getInstance().execute(this::startDo, TimeUnit.MINUTES.toSeconds(61));
    }
    
    private void startDo() {
        synchronized(inetUniqCSV) {
            if (!inetUniqCSV.exists()) {
                Stats.getIpsInet();
            }
        }
        if (allDevLocalDeq.remainingCapacity() == 0) {
            allDevLocalDeq.clear();
        }
        AppConfigurationLocal threadExecutor = AppConfigurationLocal.getInstance();
        BlockingQueue<Runnable> queueExec = ((ThreadConfig) threadExecutor).getTaskExecutor().getThreadPoolExecutor().getQueue();
        for (Runnable runnable : queueExec) {
            if (runnable instanceof ExecScan) {
                queueExec.remove(runnable);
            }
        }
        
        @NotNull ExecScan[] newRunnables = getRunnables();
        for (ExecScan execScan : newRunnables) {
            threadExecutor.execute(execScan, TimeUnit.HOURS.toSeconds(1));
        }
        scheduleTimeSetterScanMin();
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
    
    private void scheduleTimeSetterScanMin() {
        AppConfigurationLocal.getInstance().schedule(this::setScanInMin, 5);
    }
    
    private void setScanInMin() {
        if (allDevLocalDeq.remainingCapacity() > 0 && TimeUnit.MILLISECONDS.toMinutes(InitProperties.getUserPref().getLong(ExecScan.class.getSimpleName(), 0)) > 0 && allDevLocalDeq
                .size() > 0) {
            long scansItMin = allDevLocalDeq.size() / TimeUnit.MILLISECONDS.toMinutes(InitProperties.getUserPref().getLong(ExecScan.class.getSimpleName(), 0));
            InitProperties.setPreference(PropertiesNames.SCANSINMIN, String.valueOf(scansItMin));
        }
    }
}
