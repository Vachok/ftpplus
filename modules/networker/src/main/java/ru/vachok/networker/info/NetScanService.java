// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ad.inet.InternetUse;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.net.monitor.*;
import ru.vachok.networker.net.scanner.ScanOnline;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.*;


/**
 Пинг-фейс
 
 @see ru.vachok.networker.info.NetScanServiceTest
 @since 14.02.2019 (23:31) */
@SuppressWarnings("unused")
public interface NetScanService extends Runnable {
    
    
    String PTV = "ptv";
    
    String WORK_SERVICE = "KudrWorkTime";
    
    String DIAPAZON = "DiapazonScan";
    
    String PINGER_FILE = "PingerFromFile";
    
    @Contract(pure = true)
    static Map<String, String> get24hrsTempInetList() {
        return InternetUse.get24hrsTempInetList();
    }
    
    @Contract(pure = true)
    static Map<String, String> getInetUniqMap() {
        return InternetUse.getInetUniqMap();
    }
    
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
        Thread.currentThread().checkAccess();
        Thread.currentThread().setPriority(2);
        Thread.currentThread().setName("isReach" + inetAddrStr);
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
            default:
                return new ScanOnline();
        }
    }
    
    static void autoResolvedUsersRecord(String pcName, String lastFile) {
        UserInfo.autoResolvedUsersRecord(pcName, lastFile);
    }
}