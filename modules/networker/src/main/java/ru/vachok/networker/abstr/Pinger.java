// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.abstr;


import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Properties;


/**
 Пинг-фейс

 @since 14.02.2019 (23:31) */
@SuppressWarnings("unused")
public interface Pinger {

    String getTimeToEndStr();

    String getPingResultStr();
    
    default List<String> pingDev(Deque<InetAddress> devicesDeq) {
        MessageLocal messageToUser = new MessageLocal(Pinger.class.getSimpleName() + "SACE!");
        String classMeth = "Pinger.pingDev";
        Properties properties = AppComponents.getProps();
        long pingSleep = ConstantsFor.TIMEOUT_650;

        try {
            pingSleep = Long.parseLong(properties.getProperty(ConstantsNet.PROP_PINGSLEEP));
        } catch (Exception e) {
            messageToUser.warn(pingSleep + " is " + ConstantsFor.TIMEOUT_650 + "\n" + e.getMessage());
        }
        List<String> resList = new ArrayList<>();
        properties.setProperty(ConstantsNet.PROP_PINGSLEEP, pingSleep + "");

        while (!devicesDeq.isEmpty()) {
            try {
                InetAddress inetAddress = devicesDeq.removeFirst();
                boolean reachable = inetAddress.isReachable(ConstantsFor.TIMEOUT_650);
                String msg;
                if (reachable) {
                    msg = "<font color=\"#00ff69\">" + inetAddress + " is " + true + "</font>";
                } else {
                    msg = "<font color=\"red\">" + inetAddress + " is " + false + "</font>";
                }
                resList.add(msg);
                Thread.sleep(pingSleep);
            } catch (IOException | InterruptedException e) {
                messageToUser.errorAlert("Pinger", "pingDev", e.getMessage());
                FileSystemWorker.error(classMeth, e);
                Thread.currentThread().interrupt();
            }
        }
        return resList;
    }

    boolean isReach(String inetAddrStr);
}
