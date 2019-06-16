// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

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
public interface Pinger {

    String getTimeToEndStr();

    String getPingResultStr();
    
    default List<String> pingDev(Map<InetAddress, String> devicesDeq) {
        MessageToUser messageToUser = new MessageLocal(Pinger.class.getSimpleName() + " SAFE!");
        String classMeth = "Pinger.pingDev";
        Properties properties = AppComponents.getProps();
        long pingSleep = ConstantsFor.TIMEOUT_650;
        try {
            pingSleep = Long.parseLong(properties.getProperty(ConstantsNet.PROP_PINGSLEEP));
        } catch (Exception e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".pingDev");
        }
        List<String> resList = new ArrayList<>();
        properties.setProperty(ConstantsNet.PROP_PINGSLEEP, String.valueOf(pingSleep));
        long finalPingSleep = pingSleep;
        devicesDeq.forEach((devAdr, devName)->{
            try {
                boolean reachable = devAdr.isReachable(ConstantsFor.TIMEOUT_650);
                String msg;
                if (reachable) {
                    msg = "<font color=\"#00ff69\">" + devName + " = " + devAdr + " is " + true + "</font>";
                } else {
                    msg = "<font color=\"red\">" + devName + " = " + devAdr + " is " + false + "</font>";
                }
                resList.add(msg);
                Thread.sleep(finalPingSleep);
            } catch (IOException | InterruptedException e) {
                messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".pingDev", e));
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
            }
        });
        return resList;
    }

    boolean isReach(String inetAddrStr);
}
