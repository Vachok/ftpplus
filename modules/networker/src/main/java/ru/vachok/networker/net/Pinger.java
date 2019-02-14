package ru.vachok.networker.net;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 Пинг-фейс

 @since 14.02.2019 (23:31) */
public interface Pinger {

    String getTimeToEnd();

    String getPingResult();

    /**
     Default метод пингер.

     @param devicesDeq {@link ConcurrentLinkedDeque} of {@link InetAddress}.
     @return {@link List} of {@link String} результатов.
     */
    default List<String> pingDev(Deque<InetAddress> devicesDeq) {
        String classMeth = "Pinger.pingDev";
        new MessageCons().errorAlert(classMeth);
        final Properties properties = ConstantsFor.getProps();
        long pingSleep = ConstantsFor.TIMEOUT_650;
        MessageToUser messageToUser = new MessageLocal();
        try{
            pingSleep = Long.parseLong(properties.getProperty(ConstantsNet.PROP_PINGSLEEP));
        }
        catch(Exception e){
            (( MessageLocal ) messageToUser).warn(pingSleep + " is " + ConstantsFor.TIMEOUT_650 + "\n" + e.getMessage());
        }
        List<String> resList = new ArrayList<>();
        properties.setProperty(ConstantsNet.PROP_PINGSLEEP, pingSleep + "");
        while(!devicesDeq.isEmpty()){
            try{
                InetAddress inetAddress = devicesDeq.removeFirst();
                resList.add(inetAddress.toString() + " is " + inetAddress.isReachable(ConstantsFor.TIMEOUT_650));
                Thread.sleep(pingSleep);
            }
            catch(IOException | InterruptedException e){
                messageToUser.errorAlert("Pinger", "pingDev", e.getMessage());
                FileSystemWorker.error(classMeth, e);
                Thread.currentThread().interrupt();
            }
        }
        return resList;
    }

    boolean isReach(String inetAddrStr);
}
