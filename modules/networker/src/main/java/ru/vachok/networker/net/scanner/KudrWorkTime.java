// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.abstr.monitors.NetScanService;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.DataConnectToAdapter;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;


/**
 @see ru.vachok.networker.net.scanner.KudrWorkTimeTest
 @since 12.07.2019 (0:46) */
public class KudrWorkTime implements NetScanService {
    
    
    public static final String STARTING = "Starting monitor! {0} time now";
    
    public final File logFile = new File(this.getClass().getSimpleName() + ".res");
    
    private Map<String, Object> mapOfConditionsTypeNameTypeCondition = new ConcurrentHashMap<>();
    
    private int monitoringCycleDelayInSeconds = ConstantsFor.ONE_DAY_HOURS;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private List<String> execList = NetKeeper.getKudrWorkTime();
    
    public KudrWorkTime() {
    }
    
    @Contract(pure = true)
    protected KudrWorkTime(Map<String, Object> mapOfConditionsTypeNameTypeCondition) {
        this.mapOfConditionsTypeNameTypeCondition = mapOfConditionsTypeNameTypeCondition;
    }
    
    public Map<String, Object> getMapOfConditionsTypeNameTypeCondition() {
        return Collections.unmodifiableMap(mapOfConditionsTypeNameTypeCondition);
    }
    
    public boolean isReach(String inetAddrStr) {
        throw new InvokeEmptyMethodException(getClass().getTypeName());
    }
    
    @Override
    public List<String> pingDevices(@NotNull Map<InetAddress, String> ipAddressAndDeviceNameToShow) {
        List<String> retList = new ArrayList<>();
        for (Map.Entry<InetAddress, String> addressNameEntry : ipAddressAndDeviceNameToShow.entrySet()) {
            boolean isDeviceOn = isReach(addressNameEntry.getKey());
            retList.add(MessageFormat.format("Pinging {1}, with timeout {2} seconds - {0}", isDeviceOn, addressNameEntry.getValue(), monitoringCycleDelayInSeconds));
        }
        mapOfConditionsTypeNameTypeCondition.put("pingDevList", retList);
        return retList;
    }
    
    @Override
    public boolean isReach(@NotNull InetAddress inetAddrStr) {
        try {
            return inetAddrStr.isReachable((int) TimeUnit.SECONDS.toMillis(monitoringCycleDelayInSeconds));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("Kudr.pingOneDevice threw away: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return false;
        }
    }
    
    @Override
    public void run() {
        FileSystemWorker.appendObjectToFile(logFile, "\n" + new Date() + " starting...\n");
        FileSystemWorker.appendObjectToFile(logFile, getExecution());
    }
    
    @Override
    public String getExecution() {
        execList.add(MessageFormat.format(KudrWorkTime.STARTING, LocalTime.now()));
        Future<?> submit = Executors.newSingleThreadExecutor().submit(this::monitorAddress);
        try {
            submit.get((LocalTime.parse("18:30").toSecondOfDay() - LocalTime.parse("07:30").toSecondOfDay()), TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException e) {
            messageToUser.error(MessageFormat
                .format("KudrWorkTime.getExecution {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        catch (TimeoutException e) {
            FileSystemWorker.appendObjectToFile(logFile, writeLogToFile());
        }
        return new TForms().fromArray(execList);
    }
    
    @Override
    public String writeLogToFile() {
        final String sql = "INSERT INTO `u0466446_velkom`.`worktime` (`Date`, `Timein`, `Timeout`) VALUES (?, ?, ?);";
        try (Connection c = DataConnectToAdapter.getRegRuMysqlLibConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                String dateToDB = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                p.setString(1, dateToDB);
                if (LocalTime.now().toSecondOfDay() >= LocalTime.parse("17:30").toSecondOfDay()) {
                    p.setLong(2, 0);
                    p.setLong(3, System.currentTimeMillis());
                }
                else {
                    p.setLong(2, System.currentTimeMillis());
                    p.setLong(3, 0);
                }
                int updDB = p.executeUpdate();
                String logStr = MessageFormat.format("{0} database updated. {1} time now", updDB, LocalTime.now().toString());
                execList.add(logStr);
                return logStr;
            }
        }
        catch (SQLException e) {
            return e.getMessage();
        }
    }
    
    @Override
    public String getPingResultStr() {
        return FileSystemWorker.readFile(this.getClass().getSimpleName() + ".res");
    }
    
    @Override
    public Runnable getMonitoringRunnable() {
        return this;
    }
    
    @Override
    public String getStatistics() {
        return new TForms().fromArray(mapOfConditionsTypeNameTypeCondition);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Kudr{");
        sb.append("mapOfConditionsTypeNameTypeCondition=").append(new TForms().fromArray(mapOfConditionsTypeNameTypeCondition));
        sb.append(", monitoringCycleDelayInSeconds=").append(monitoringCycleDelayInSeconds);
        sb.append('}');
        return sb.toString();
    }
    
    private void monitorAddress() {
        final int start = LocalTime.now().toSecondOfDay();
        FileSystemWorker.appendObjectToFile(logFile, writeLogToFile());
        while (true) {
            try {
                if (isReach(InetAddress.getByAddress(InetAddress.getByName("10.200.214.80").getAddress()))) {
                    writeLogToFile();
                }
                break;
            }
            catch (UnknownHostException e) {
                messageToUser.error(MessageFormat.format("KudrWorkTime.monitorAddress: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
        }
        while (!isReach(getInetAddress())) {
            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(5));
            }
            catch (InterruptedException e) {
                FileSystemWorker.appendObjectToFile(logFile, MessageFormat.format("KudrWorkTime.monitorAddress: {0}, ({1})", e.getMessage(), e.getClass().getName()));
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
            }
            if (isReach(getInetAddress())) {
                execList.add(getInetAddress().toString() + " is online!");
                break;
            }
        }
        while (isReach(getInetAddress())) {
            try {
                Thread.sleep(1001);
            }
            catch (InterruptedException e) {
                FileSystemWorker.appendObjectToFile(logFile, MessageFormat.format("KudrWorkTime.getExecution {0} - {1}\nStack:\n{2}",
                    e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
            }
            execList.remove(execList.size() - 1);
            execList.add(MessageFormat.format("{1} | {0} seconds elapsed.", (start - LocalTime.now().toSecondOfDay()), new Date()));
            if (!isReach(getInetAddress())) {
                FileSystemWorker.appendObjectToFile(logFile, writeLogToFile());
                break;
            }
        }
    }
    
    private InetAddress getInetAddress() {
        InetAddress retAddr = InetAddress.getLoopbackAddress();
        try {
            retAddr = InetAddress.getByAddress(InetAddress.getByName("10.200.213.85").getAddress());
        }
        catch (UnknownHostException e) {
            messageToUser.error(MessageFormat
                .format("KudrWorkTime.getInetAddress {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        return retAddr;
    }
}