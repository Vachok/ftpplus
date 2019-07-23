// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr.monitors;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 Пинг-фейс

 @since 14.02.2019 (23:31) */
@SuppressWarnings("unused")
public interface NetScanService extends Runnable {
    
    
    String getExecution();

    String getPingResultStr();
    
    default List<String> pingDevices(Map<InetAddress, String> ipAddressAndDeviceNameToShow) {
        MessageToUser messageToUser = new MessageLocal(NetScanService.class.getSimpleName() + " SAFE!");
        String classMeth = "Pinger.pingDevices";
        Properties properties = AppComponents.getProps();
        long pingSleep;
        try {
            pingSleep = Long.parseLong(properties.getProperty(ConstantsFor.PR_PINGSLEEP));
        } catch (Exception e) {
            pingSleep = ConstantsFor.TIMEOUT_650;
        }
        List<String> resList = new ArrayList<>();
        long finalPingSleep = pingSleep;
        long finalPingSleep1 = pingSleep;
        //noinspection OverlyLongLambda
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
    
    boolean isReach(InetAddress inetAddrStr);
    
    String writeLogToFile();
    
    Runnable getMonitoringRunnable();
    
    String getStatistics();
    
}