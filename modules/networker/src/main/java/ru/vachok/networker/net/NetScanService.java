// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.enums.PropertiesNames;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.net.monitor.KudrWorkTime;
import ru.vachok.networker.net.monitor.NetMonitorPTV;
import ru.vachok.networker.net.scanner.ScanOnline;
import ru.vachok.networker.restapi.MessageToUser;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 Пинг-фейс
 
 @see ru.vachok.networker.net.NetScanServiceTest
 @since 14.02.2019 (23:31) */
@SuppressWarnings("unused")
public interface NetScanService extends Runnable {
    
    
    String PTV = "ptv";
    
    String WORK_SERVICE = "KudrWorkTime";
    
    default List<String> pingDevices(Map<InetAddress, String> ipAddressAndDeviceNameToShow) {
        MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.TRAY, this.getClass().getSimpleName());
        System.out.println("AppComponents.ipFlushDNS() = " + UsefulUtilities.ipFlushDNS());
        Properties properties = AppComponents.getProps();
        long pingSleep = 250;
        try {
            pingSleep = Long.parseLong(properties.getProperty(PropertiesNames.PR_PINGSLEEP, "250"));
        }
        catch (NumberFormatException e) {
            messageToUser.error(MessageFormat.format("NetScanService.pingDevices: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        
        List<String> resList = new ArrayList<>();
        long finalPingSleep = pingSleep;
        ipAddressAndDeviceNameToShow.forEach((devAdr, devName)->{
            try {
                boolean reachable = devAdr.isReachable((int) finalPingSleep);
                String msg;
                if (reachable) {
                    msg = "<font color=\"#00ff69\">" + devName + " = " + devAdr + " is " + true + "</font>";
                } else {
                    msg = "<font color=\"red\">" + devName + " = " + devAdr + " is " + false + "</font>";
                }
                resList.add(msg);
                Thread.sleep(finalPingSleep);
            }
            catch (IOException e) {
                messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".pingDevices", e));
            }
            catch (InterruptedException e) {
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
            }
        });
        return resList;
    }
    
    String getExecution();
    
    String getPingResultStr();
    
    static boolean isReach(String inetAddrStr) {
        InetAddress byName;
        try {
            byName = InetAddress.getByName(inetAddrStr);
        }
        catch (UnknownHostException e) {
            byName = getByName(inetAddrStr);
            if (byName.equals(InetAddress.getLoopbackAddress())) {
                System.err.println(e.getMessage());
                return false;
            }
        }
        try {
            return byName.isReachable(100);
        }
        catch (IOException e) {
            return false;
        }
    }
    
    static InetAddress getByName(String inetAddrStr) {
        InetAddress inetAddress = InetAddress.getLoopbackAddress();
        try {
            inetAddress = InetAddress.getByAddress(InetAddress.getByName(inetAddrStr).getAddress());
        }
        catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        }
        return inetAddress;
    }
    
    String writeLog();
    
    Runnable getMonitoringRunnable();
    
    String getStatistics();
    
    @Contract("_ -> new")
    static @NotNull NetScanService getI(@NotNull String type) {
        if (type.equals(PTV)) {
            return new NetMonitorPTV();
        }
        else if (type.equals(WORK_SERVICE)) {
            return new KudrWorkTime();
        }
        else {
            return new ScanOnline();
        }
    }
}