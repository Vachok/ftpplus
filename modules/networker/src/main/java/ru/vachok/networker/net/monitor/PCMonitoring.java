package ru.vachok.networker.net.monitor;


import ru.vachok.networker.AbstractNetworkerFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.monitors.NetScanService;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 @since 01.08.2019 (9:00) */
public class PCMonitoring implements NetScanService {
    
    
    private String inetAddressStr;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private List<String> results = new ArrayList<>();
    
    private int runningDurationMin;
    
    public PCMonitoring(String inetAddressStr, int runningDurationSec) {
        this.inetAddressStr = inetAddressStr;
        this.runningDurationMin = runningDurationSec;
    }
    
    public void setRunningDurationMin(int runningDurationMin) {
        this.runningDurationMin = runningDurationMin;
    }
    
    @Override
    public void run() {
        final long start = System.currentTimeMillis();
        String thrName = inetAddressStr + "-m";
        if ((start + TimeUnit.SECONDS.toMillis(runningDurationMin)) > System.currentTimeMillis()) {
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::writeLog, 1, 5, TimeUnit.SECONDS);
            Thread.currentThread().setName(thrName);
            messageToUser.warn(thrName);
            do {
                getExecution();
            } while ((start + TimeUnit.MINUTES.toMillis(runningDurationMin)) > System.currentTimeMillis());
        }
    }
    
    @Override
    public String getExecution() {
        NameOrIPChecker nameOrIP = new NameOrIPChecker(inetAddressStr);
        try {
            InetAddress inetAddress = nameOrIP.resolveIP();
            boolean reach = isReach(inetAddress);
            String lastResult = MessageFormat.format("{0}| IP: {1} is {2}", LocalDateTime.now().toString(), inetAddress.toString(), reach);
            if (!reach) {
                results.add(lastResult);
            }
            return lastResult;
        }
        catch (UnknownHostException e) {
            return MessageFormat.format("PCMonitoring.run: {0}, ({1})", e.getMessage(), e.getClass().getName());
        }
    }
    
    @Override
    public String getPingResultStr() {
        return results.get(results.size() - 1);
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
            printStream.println(new TForms().fromArray(results));
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
        return MessageFormat.format("Недоступность ПК {0}:<br> {1}", inetAddressStr, new TForms().fromArray(this.results, true));
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PCMonitoring{");
        sb.append("inetAddressStr='").append(inetAddressStr).append('\'');
        
        sb.append(", results=").append(results.size());
        sb.append(", runningDurationMin=").append(runningDurationMin);
        sb.append('}');
        return sb.toString();
    }
}
