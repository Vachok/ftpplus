// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.net.monitor.*;
import ru.vachok.networker.net.scanner.PcNamesScanner;
import ru.vachok.networker.net.scanner.ScanOnline;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.*;


/**
 Пинг-фейс
 
 @see ru.vachok.networker.info.NetScanServiceTest
 @since 14.02.2019 (23:31) */
@SuppressWarnings({"unused", "MethodWithMultipleReturnPoints"})
public interface NetScanService extends Runnable {
    
    
    String PTV = "ptv";
    
    String WORK_SERVICE = "KudrWorkTime";
    
    String DIAPAZON = "DiapazonScan";
    
    String PINGER_FILE = "PingerFromFile";
    
    String PCNAMESSCANNER = "PcNamesScanner";
    
    String SCAN_ONLINE = "ScanOnline";
    
    default List<String> pingDevices(Map<InetAddress, String> ipAddressAndDeviceNameToShow) {
        MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.TRAY, this.getClass().getSimpleName());
        System.out.println("AppComponents.ipFlushDNS() = " + UsefulUtilities.ipFlushDNS());
        Properties properties = InitProperties.getTheProps();
        long pingSleep = 250;
        try {
            pingSleep = Long.parseLong(properties.getProperty(PropertiesNames.PINGSLEEP, "250"));
        }
        catch (NumberFormatException e) {
            messageToUser.error(MessageFormat.format("NetScanService.pingDevices: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        List<String> resList = new ArrayList<>();
        long finalPingSleep = pingSleep;
        for (Map.Entry<InetAddress, String> entry : ipAddressAndDeviceNameToShow.entrySet()) {
            InetAddress devAdr = entry.getKey();
            String devName = entry.getValue();
            try {
                boolean reachable = devAdr.isReachable((int) finalPingSleep);
                String msg;
                if (reachable) {
                    msg = "<font color=\"#00ff69\">" + devName + " = " + devAdr + " is " + true + "</font>";
                }
                else {
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
        }
        return resList;
    }
    
    String getExecution();
    
    String getPingResultStr();
    
    static boolean isReach(String inetAddrStr) {
        return isReach(inetAddrStr, 100);
    }
    
    static InetAddress getByName(String inetAddrStr) {
        InetAddress inetAddress = InetAddress.getLoopbackAddress();
        try {
            inetAddress = InetAddress.getByAddress(InetAddress.getByName(inetAddrStr).getAddress());
        }
        catch (UnknownHostException ignore) {
            //30.09.2019 (16:44)
        }
        return inetAddress;
    }
    
    String writeLog();
    
    Runnable getMonitoringRunnable();
    
    String getStatistics();
    
    @Contract("_ -> new")
    static @NotNull NetScanService getInstance(@NotNull String type) {
        switch (type) {
            case PTV:
                return new NetMonitorPTV();
            case WORK_SERVICE:
                return new KudrWorkTime();
            case DIAPAZON:
                return DiapazonScan.getInstance();
            case PINGER_FILE:
                return new PingerFromFile();
            case PCNAMESSCANNER:
                return new PcNamesScanner();
            default:
                return new ScanOnline();
        }
    }
    
    static boolean writeUsersToDBFromSET() {
        return UserInfo.writeUsersToDBFromSET();
    }
    
    static boolean isReach(String inetAddrStr, int timeout) {
        Thread.currentThread().checkAccess();
        Thread.currentThread().setPriority(1);
        Thread.currentThread().setName("ping" + inetAddrStr);
        InetAddress byName;
        try {
            byName = InetAddress.getByName(inetAddrStr);
        }
        catch (UnknownHostException e) {
            byName = getByName(inetAddrStr);
            if (byName.equals(InetAddress.getLoopbackAddress())) {
                return false;
            }
        }
        try {
            return byName.isReachable(timeout);
        }
        catch (IOException e) {
            return false;
        }
    }
}