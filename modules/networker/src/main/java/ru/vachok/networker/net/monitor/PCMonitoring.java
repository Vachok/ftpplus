// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import ru.vachok.networker.AbstractNetworkerFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.abstr.monitors.NetScanService;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.enums.OtherKnownDevices;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 @since 01.08.2019 (9:00) */
public class PCMonitoring implements NetScanService {
    
    
    private String inetAddressStr;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
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
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::writeLog, 1, 5, TimeUnit.SECONDS);
            Thread.currentThread().setName(thrName);
            messageToUser.warn(thrName);
            do {
                getExecution();
            } while ((start + TimeUnit.MINUTES.toMillis(runningDurationMin)) > System.currentTimeMillis());
        }
    }
    
    public String getExecution() {
        NameOrIPChecker nameOrIP = new NameOrIPChecker(inetAddressStr);
        try {
            InetAddress inetAddress = nameOrIP.resolveIP();
            InetAddress ctrlAddr = InetAddress.getByName(OtherKnownDevices.DO0213_KUDR);
            boolean reach = isReach(inetAddress);
            String lastResult = MessageFormat.format("{0}| IP: {1} is {2} control: {3} is {4}",
                LocalDateTime.now().toString(), inetAddress.toString(), reach, OtherKnownDevices.DO0213_KUDR, isReach(ctrlAddr));
            if (!reach) {
                monitorLog.add(lastResult);
                this.noPingsCounter++;
            }
            else if (inetAddress.equals(InetAddress.getLoopbackAddress())) {
                monitorLog.add(inetAddressStr + " test " + LocalDateTime.now().toString());
            }
            return lastResult;
        }
        catch (UnknownHostException e) {
            return MessageFormat.format("PCMonitoring.run: {0}, ({1})", e.getMessage(), e.getClass().getName());
        }
    }
    
    @Override
    public String getPingResultStr() {
        return monitorLog.get(monitorLog.size() - 1);
    }
    
    @Override
    public boolean isReach(InetAddress inetAddrStr) {
        return AbstractNetworkerFactory.getInstance().isReach(inetAddrStr);
    }
    
    @Override
    public String writeLog() {
        File logFile = new File(inetAddressStr + ".res");
        try (OutputStream outputStream = new FileOutputStream(logFile, true);
             PrintStream printStream = new PrintStream(outputStream, true)) {
            if (monitorLog.size() > 0) {
                printStream.println("*********");
                printStream.println(MessageFormat.format("At {0} no pings {1} times", new Date().toString(), monitorLog.size()));
                printStream.println(new TForms().fromArray(monitorLog));
                monitorLog.clear();
            }
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("PCMonitoring.writeLog: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return logFile.getAbsolutePath();
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
