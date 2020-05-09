// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.inet.TemporaryFullInternet;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.database.DataConnectToAdapter;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 @see KudrWorkTimeTest
 @since 12.07.2019 (0:46) */
public class KudrWorkTime implements NetScanService {


    public static final String STARTING = "Starting monitor! {0} time now";

    private static final String INIT_STRING = "KudrWorkTime.KudrWorkTime: {0}, ({1})";

    private final File logFile = new File(this.getClass().getSimpleName() + ".res");

    private Map<String, Object> mapOfConditionsTypeNameTypeCondition = new ConcurrentHashMap<>();

    private int startPlus9Hours = LocalTime.parse("17:30").toSecondOfDay();

    private int start = LocalTime.now().toSecondOfDay();

    private final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());

    private final List<String> execList = NetKeeper.getKudrWorkTime();

    private InetAddress samsIP;

    private InetAddress do0213IP;

    private boolean isTest;

    public Map<String, Object> getMapOfConditionsTypeNameTypeCondition() {
        return Collections.unmodifiableMap(mapOfConditionsTypeNameTypeCondition);
    }

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

    protected KudrWorkTime(Map<String, Object> mapOfConditionsTypeNameTypeCondition) {
        this.mapOfConditionsTypeNameTypeCondition = mapOfConditionsTypeNameTypeCondition;
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
        Thread.currentThread().setName(this.getClass().getSimpleName());
        if (LocalTime.now().toSecondOfDay() > LocalTime.of(7, 30).toSecondOfDay() & LocalTime.of(18, 30).toSecondOfDay() > LocalTime.now().toSecondOfDay()) {
            FileSystemWorker.appendObjectToFile(logFile, "\n" + new Date() + " starting...\n");
            FileSystemWorker.appendObjectToFile(logFile, getExecution());
        }
    }

    @Override
    public String getPingResultStr() {
        return FileSystemWorker.readFile(this.getClass().getSimpleName() + ".res");
    }

    @Override
    public String getExecution() {
        execList.add(MessageFormat.format(KudrWorkTime.STARTING, LocalTime.now()));
        int timeout = LocalTime.parse("18:30").toSecondOfDay() - LocalTime.parse("07:30").toSecondOfDay();
        try {
            if (NetScanService.isReach(do0213IP.getHostAddress())) {
                AppConfigurationLocal.getInstance().execute(()->{
                    try {
                        monitorAddress();
                    }
                    catch (InvokeIllegalException e) {
                        messageToUser.warn(KudrWorkTime.class.getSimpleName(), e.getMessage(), " see line: 184 ***");
                    }
                }, timeout);
            }
            else {
                doIsReach();
            }
        }
        catch (InvokeIllegalException e) {
            messageToUser.warn(KudrWorkTime.class.getSimpleName(), e.getMessage(), " see line: 193 ***");
        }
        return AbstractForms.fromArray(execList);
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
            doIsReach0();
            return e.getMessage();
        }
    }

    @Override
    public Runnable getMonitoringRunnable() {
        return this;
    }

    @Override
    public String getStatistics() {
        return new TForms().fromArray(mapOfConditionsTypeNameTypeCondition);
    }

    private void doIsReach0() {
        try {
            doIsReach();
        }
        catch (InvokeIllegalException e) {
            messageToUser.warn(KudrWorkTime.class.getSimpleName(), e.getMessage(), " see line: 169 ***");
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(logFile, mapOfConditionsTypeNameTypeCondition, startPlus9Hours, start, messageToUser, execList, samsIP, do0213IP, isTest);
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
        return startPlus9Hours == time.startPlus9Hours &&
            start == time.start &&
            isTest == time.isTest &&
            Objects.equals(logFile, time.logFile) &&
            mapOfConditionsTypeNameTypeCondition.equals(time.mapOfConditionsTypeNameTypeCondition) &&
            messageToUser.equals(time.messageToUser) &&
            Objects.equals(execList, time.execList) &&
            Objects.equals(samsIP, time.samsIP) &&
            Objects.equals(do0213IP, time.do0213IP);
    }

    private void monitorAddress() throws InvokeIllegalException {
        if (ConstantsFor.noRunOn(ConstantsFor.REGRUHOSTING_PC)) {
            throw new InvokeIllegalException();
        }
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

    private void doIsReach() throws InvokeIllegalException {
        if (ConstantsFor.noRunOn(ConstantsFor.REGRUHOSTING_PC)) {
            throw new InvokeIllegalException();
        }
        int timeout = (int) TimeUnit.SECONDS.toMillis(150);
        boolean isDOOnline = NetScanService.isReach(do0213IP.getHostAddress(), timeout);
        if (!isDOOnline) {
            do {
                isDOOnline = NetScanService.isReach(do0213IP.getHostAddress(), timeout);
            } while (!isDOOnline);
        }
        do {
            isDOOnline = NetScanService.isReach(do0213IP.getHostAddress(), timeout);
            if (!isDOOnline) {
                FileSystemWorker.appendObjectToFile(logFile, writeLog());
                break;
            }
        } while (true);
    }
}