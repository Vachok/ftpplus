// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.net.InetAddress;
import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 @see PCMonitoringTest
 @since 01.08.2019 (9:00) */
public class PCMonitoring implements NetScanService {
    
    
    private String inetAddressStr;
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PCMonitoring.class.getSimpleName());
    
    private int runningDurationMin;
    
    private static final long START_MSEC = System.currentTimeMillis();
    
    private int noPingsCounter;
    
    private List<String> monitorLog = NetKeeper.getOnePcMonitor();
    
    public PCMonitoring(String inetAddressStr, int runningDurationSec) {
        this.inetAddressStr = inetAddressStr;
        this.runningDurationMin = (int) TimeUnit.SECONDS.toMinutes(runningDurationSec);
    }
    
    public void setRunningDurationMin(int runningDurationMin) {
        this.runningDurationMin = runningDurationMin;
    }
    
    @Override
    public void run() {
        final long start = START_MSEC;
        String thrName = inetAddressStr + "-m";
        if ((start + TimeUnit.MINUTES.toMillis(runningDurationMin)) > System.currentTimeMillis()) {
            AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor().scheduleAtFixedRate(this::writeLog, 1, 5, TimeUnit.SECONDS);
            Thread.currentThread().setName(thrName);
            do {
                getExecution();
            } while ((start + TimeUnit.MINUTES.toMillis(runningDurationMin)) > System.currentTimeMillis());
        }
    }
    
    @Override
    public String getExecution() {
        NameOrIPChecker nameOrIP = new NameOrIPChecker(inetAddressStr);
        InetAddress inetAddress = nameOrIP.resolveInetAddress();
        boolean reach = NetScanService.isReach(inetAddress.getHostAddress());
        nameOrIP.setNameOrIpStr(OtherKnownDevices.DO0213_KUDR);
        InetAddress controlAddress = nameOrIP.resolveInetAddress();
        boolean isOnControl = NetScanService.isReach(controlAddress.getHostAddress());
        String lastResult = MessageFormat.format("{0}| IP: {1} is {2} control: {3} is {4}",
                LocalDateTime.now().toString(), inetAddress.toString(), reach, controlAddress.getHostAddress(), isOnControl);
        JsonObject result = new JsonObject();
        result.add("ip", inetAddress.getHostAddress());
        result.add(ConstantsFor.DBFIELD_CONTROLIP, controlAddress.getHostAddress());
        result.add(ConstantsFor.DBFIELD_ONLINE, reach);
        result.add(ConstantsFor.DBFIELD_ONLINECONROL, isOnControl);
        monitorLog.add(result.toString());
        this.noPingsCounter++;
        return lastResult;
    }
    
    @Override
    public String getPingResultStr() {
        return monitorLog.get(monitorLog.size() - 1);
    }
    
    @Override
    public String writeLog() {
        final String sql = "INSERT INTO pcmonitoring (`ip`, `controlIp`, `online`, `onlineConrol`) VALUES (?, ?, ?, ?);";
        int execUp = 0;
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("lan.PCMonitoring".toLowerCase())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                for (String logRec : monitorLog) {
                    JsonObject jsonObject = Json.parse(logRec).asObject();
                    preparedStatement.setString(1, jsonObject.get("ip").toString());
                    preparedStatement.setString(2, jsonObject.get(ConstantsFor.DBFIELD_CONTROLIP).toString());
                    preparedStatement.setString(3, jsonObject.get(ConstantsFor.DBFIELD_ONLINE).toString());
                    preparedStatement.setString(4, jsonObject.get(ConstantsFor.DBFIELD_ONLINECONROL).toString());
                    execUp = preparedStatement.executeUpdate();
                }
            }
            catch (com.eclipsesource.json.ParseException e) {
                messageToUser.warn("PCMonitoring", "writeLog", e.getMessage() + " see line: 111");
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("PCMonitoring.writeLog", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
        }
        return MessageFormat.format("Updated: {0} by {1}", execUp, sql);
    }
    
    @Override
    public Runnable getMonitoringRunnable() {
        return this;
    }
    
    @Override
    public String getStatistics() {
    
        return MessageFormat
            .format("Недоступность ПК {0}: {1}@{2} мин.", inetAddressStr, noPingsCounter, (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - START_MSEC)));
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PCMonitoring{");
        sb.append("inetAddressStr='").append(inetAddressStr).append('\'');
    
        sb.append(", results=").append(monitorLog.size());
        sb.append(", runningDurationMin=").append(runningDurationMin);
        sb.append('}');
        return sb.toString();
    }
}
