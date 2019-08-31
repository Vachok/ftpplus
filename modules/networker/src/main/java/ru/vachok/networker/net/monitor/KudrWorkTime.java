// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.NetKeeper;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.net.ssh.TemporaryFullInternet;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.DataConnectToAdapter;

import java.io.File;
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
    
    private static final String INIT_STRING = "KudrWorkTime.KudrWorkTime: {0}, ({1})";
    
    private final File logFile = new File(this.getClass().getSimpleName() + ".res");
    
    private Map<String, Object> mapOfConditionsTypeNameTypeCondition = new ConcurrentHashMap<>();
    
    private int startPlus9Hours = LocalTime.parse("17:30").toSecondOfDay();
    
    private int start = LocalTime.now().toSecondOfDay();
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    private List<String> execList = NetKeeper.getKudrWorkTime();
    
    private InetAddress samsIP;
    
    private InetAddress do0213IP;
    
    private boolean isTest;
    
    public KudrWorkTime() {
        try {
            this.samsIP = InetAddress.getByAddress(InetAddress.getByName("10.200.214.80").getAddress());
            this.do0213IP = InetAddress.getByAddress(InetAddress.getByName("10.200.213.85").getAddress());
        }
        catch (UnknownHostException e) {
            messageToUser.error(MessageFormat.format(INIT_STRING, e.getMessage(), e.getClass().getName()));
        }
    }
    
    protected KudrWorkTime(boolean isTest) {
        this.isTest = isTest;
        try {
            this.samsIP = InetAddress.getByAddress(InetAddress.getByName("10.200.214.80").getAddress());
            this.do0213IP = InetAddress.getByAddress(InetAddress.getByName("10.200.213.85").getAddress());
        }
        catch (UnknownHostException e) {
            messageToUser.error(MessageFormat.format(INIT_STRING, e.getMessage(), e.getClass().getName()));
        }
        
    }
    
    @Contract(pure = true)
    protected KudrWorkTime(Map<String, Object> mapOfConditionsTypeNameTypeCondition) {
        this.mapOfConditionsTypeNameTypeCondition = mapOfConditionsTypeNameTypeCondition;
    }
    
    public Map<String, Object> getMapOfConditionsTypeNameTypeCondition() {
        return Collections.unmodifiableMap(mapOfConditionsTypeNameTypeCondition);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        KudrWorkTime time = (KudrWorkTime) o;
        
        if (startPlus9Hours != time.startPlus9Hours) {
            return false;
        }
        if (start != time.start) {
            return false;
        }
        if (isTest != time.isTest) {
            return false;
        }
        if (!logFile.equals(time.logFile)) {
            return false;
        }
        if (!mapOfConditionsTypeNameTypeCondition.equals(time.mapOfConditionsTypeNameTypeCondition)) {
            return false;
        }
        if (!messageToUser.equals(time.messageToUser)) {
            return false;
        }
        if (!execList.equals(time.execList)) {
            return false;
        }
        if (samsIP != null ? !samsIP.equals(time.samsIP) : time.samsIP != null) {
            return false;
        }
        return do0213IP != null ? do0213IP.equals(time.do0213IP) : time.do0213IP == null;
    }
    
    @Override
    public int hashCode() {
        int result = logFile.hashCode();
        result = 31 * result + mapOfConditionsTypeNameTypeCondition.hashCode();
        result = 31 * result + startPlus9Hours;
        result = 31 * result + start;
        result = 31 * result + messageToUser.hashCode();
        result = 31 * result + execList.hashCode();
        result = 31 * result + (samsIP != null ? samsIP.hashCode() : 0);
        result = 31 * result + (do0213IP != null ? do0213IP.hashCode() : 0);
        result = 31 * result + (isTest ? 1 : 0);
        return result;
    }
    
    @Override
    public List<String> pingDevices(@NotNull Map<InetAddress, String> ipAddressAndDeviceNameToShow) {
        List<String> retList = new ArrayList<>();
        for (Map.Entry<InetAddress, String> addressNameEntry : ipAddressAndDeviceNameToShow.entrySet()) {
            boolean isDeviceOn = NetScanService.isReach(addressNameEntry.getKey().getHostAddress());
            retList.add(MessageFormat.format("Pinging {1}, with timeout {2} seconds - {0}", isDeviceOn, addressNameEntry.getValue(), ConstantsFor.DELAY * 10));
        }
        mapOfConditionsTypeNameTypeCondition.put("pingDevList", retList);
        return retList;
    }
    
    @Override
    public void run() {
        FileSystemWorker.appendObjectToFile(logFile, "\n" + new Date() + " starting...\n");
        FileSystemWorker.appendObjectToFile(logFile, getExecution());
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("KudrWorkTime{");
        sb.append("logFile=").append(logFile.getAbsolutePath());
        sb.append(", mapOfConditionsTypeNameTypeCondition=").append(mapOfConditionsTypeNameTypeCondition);
        sb.append(", startPlus9Hours=").append(startPlus9Hours);
        sb.append(", execList=").append(execList.size());
        sb.append(", samsIP=").append(samsIP);
        sb.append(", do0213IP=").append(do0213IP);
        sb.append(", isTest=").append(isTest);
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String getExecution() {
        execList.add(MessageFormat.format(KudrWorkTime.STARTING, LocalTime.now()));
        Future<?> submit = Executors.newSingleThreadExecutor().submit(this::monitorAddress);
        try {
            int timeout = LocalTime.parse("18:30").toSecondOfDay() - LocalTime.parse("07:30").toSecondOfDay();
            if (NetScanService.isReach(do0213IP.getHostAddress())) {
                submit.get(timeout, TimeUnit.SECONDS);
            }
            else {
                doIsReach();
            }
        }
        catch (InterruptedException | ExecutionException e) {
            messageToUser.error(MessageFormat
                .format("KudrWorkTime.getExecution {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        catch (TimeoutException e) {
            FileSystemWorker.appendObjectToFile(logFile, writeLog());
        }
        return new TForms().fromArray(execList);
    }
    
    @Override
    public String writeLog() {
        String sql = "INSERT INTO `u0466446_velkom`.`worktime` (`Date`, `Timein`, `Timeout`) VALUES (?, ?, ?);";
        if (isTest) {
            sql = "INSERT INTO `u0466446_testing`.`worktime` (`Date`, `Timein`, `Timeout`) VALUES (?, ?, ?);";
        }
        final String trueSql = sql;
        try (Connection c = DataConnectToAdapter.getRegRuMysqlLibConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                String dateToDB = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                p.setString(1, dateToDB);
                if (LocalTime.now().toSecondOfDay() >= startPlus9Hours) {
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
                FileSystemWorker.appendObjectToFile(logFile, logStr);
                return logStr;
            }
        }
        catch (SQLException e) {
            execList.add(e.getMessage() + "\n");
            doIsReach();
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
    
    private void monitorAddress() {
        this.startPlus9Hours = LocalTime.parse("17:30").toSecondOfDay() - LocalTime.parse("08:30").toSecondOfDay();
        boolean isSamsOnline;
        do {
            isSamsOnline = NetScanService.isReach(samsIP.getHostAddress());
            if (isSamsOnline) {
                this.start = LocalTime.now().toSecondOfDay();
                this.startPlus9Hours = (int) (start + TimeUnit.HOURS.toSeconds(9));
                new Thread(()->new TemporaryFullInternet(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(9))).start();
                FileSystemWorker.appendObjectToFile(logFile, writeLog());
                break;
            }
        } while (true);
        doIsReach();
    }
    
    private void doIsReach() {
        boolean isDOOnline = NetScanService.isReach(do0213IP.getHostAddress());
        if (!isDOOnline) {
            do {
                isDOOnline = NetScanService.isReach(do0213IP.getHostAddress());
            } while (!isDOOnline);
        }
        do {
            isDOOnline = NetScanService.isReach(do0213IP.getHostAddress());
            if (!isDOOnline) {
                FileSystemWorker.appendObjectToFile(logFile, writeLog());
                break;
            }
        } while (true);
    }
}