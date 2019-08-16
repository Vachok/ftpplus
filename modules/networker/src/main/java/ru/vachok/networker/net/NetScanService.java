// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.monitor.NetMonitorPTV;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 Пинг-фейс
 
 @see ru.vachok.networker.abstr.monitors.NetScanServiceTest
 @since 14.02.2019 (23:31) */
@SuppressWarnings("unused")
public interface NetScanService extends Runnable {
    
    
    String TYPE_PTV = "ptv";
    
    
    default List<String> pingDevices(Map<InetAddress, String> ipAddressAndDeviceNameToShow) {
        MessageToUser messageToUser = new MessageLocal(NetScanService.class.getSimpleName() + " SAFE!");
        System.out.println("AppComponents.ipFlushDNS() = " + UsefulUtilities.ipFlushDNS());
        Properties properties = AppComponents.getProps();
        long pingSleep;
        try {
            pingSleep = Long.parseLong(properties.getProperty(PropertiesNames.PR_PINGSLEEP));
        } catch (Exception e) {
            pingSleep = ConstantsFor.TIMEOUT_650;
        }
        List<String> resList = new ArrayList<>();
        long finalPingSleep = pingSleep;
        long finalPingSleep1 = pingSleep;
        ipAddressAndDeviceNameToShow.forEach((devAdr, devName)->{
            try {
                boolean reachable = devAdr.isReachable((int) finalPingSleep1);
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
    
    @Override
    default void run() {
        getExecution();
    }
    
    boolean isReach(InetAddress inetAddrStr);
    
    String writeLog();
    
    Runnable getMonitoringRunnable();
    
    String getStatistics();
    
    static NetScanService getI(String type) {
        if (type.equals(TYPE_PTV)) {
            return new NetMonitorPTV();
        }
        else {
            throw new TODOException("17.08.2019 (1:48)");
        }
    }
    
}