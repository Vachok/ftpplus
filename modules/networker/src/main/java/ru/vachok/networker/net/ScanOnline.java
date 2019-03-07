package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;


/**
 Сканирование только тех, что он-лайн
 <p>
 
 @see DiapazonedScan
 @since 26.01.2019 (11:18) */
public class ScanOnline implements Runnable {
    
    
    /**
     {@link NetListKeeper#getI()}
     */
    private static final NetListKeeper NET_LIST_KEEPER = NetListKeeper.getI();
    
    /**
     * {@link NetListKeeper#getOnLinesResolve()}
     */
    private ConcurrentMap<String, String> onLinesResolve = NET_LIST_KEEPER.getOnLinesResolve();
    
    /**
     {@link MessageLocal}
     */
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private void offlineNotEmptyActions() {
        AppComponents.threadConfig().thrNameSet("scOffNE");
        SwitchesAvailability switchesAvailability = new SwitchesAvailability();
        switchesAvailability.run();
        
        Set<String> availabilityOkIP = switchesAvailability.getOkIP();
        availabilityOkIP.forEach(x -> {
            onLinesResolve.put(x, LocalDateTime.now().toString());
        });
    }
    
    private void runPing(List<InetAddress> onList) {
        AppComponents.threadConfig().executeAsThread(() -> NET_LIST_KEEPER.readMap());
        String clMt = "ScanOnline.runPing. IPs to ping: " + onList.size();
        messageToUser.infoNoTitles(clMt);
        for (InetAddress inetAddress : onList) {
            pingAddr(inetAddress);
        }
    }
    
    /**
     Пингует конкрктный {@link InetAddress}
     <p>
     TimeOut is 250 mSec.
     <p>
     <b>Схема:</b> <br>
     1. {@link NetListKeeper#getOffLines()}. Если нет пинга, добавляет {@code inetAddress.toString, LocalTime.now.toString} и запускает: <br> 2. {@link
    ThreadConfig#executeAsThread(java.lang.Runnable)}. {@link #offlineNotEmptyActions()} <br>
     <p>
     
     @param inetAddress {@link InetAddress}. 3. {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)}
     */
    private void pingAddr(InetAddress inetAddress) {
    
        try {
            boolean xReachable = inetAddress.isReachable(250);
            if (!xReachable) {
                NET_LIST_KEEPER.getOffLines().put(inetAddress.toString(), LocalTime.now().toString());
                if (onLinesResolve.containsKey(inetAddress.toString())) {
                    NET_LIST_KEEPER.getOffLines().remove(inetAddress.toString());
        
                }
            } else {
                onLinesResolve.putIfAbsent(inetAddress.toString(), LocalTime.now().toString());
    
            }
        } catch (IOException e) {
            messageToUser.errorAlert("ScanOnline", "pingAddr", e.getMessage());
            FileSystemWorker.error("ScanOnline.pingAddr", e);
        }
    
    }
    
    @Override
    public void run() {
        AppComponents.threadConfig().executeAsThread(this::offlineNotEmptyActions);
        try {
            List<InetAddress> onList = NET_LIST_KEEPER.onlinesAddressesList();
            runPing(onList);
        } catch (IOException e) {
            messageToUser.errorAlert("ScanOnline", "run", e.getMessage());
            FileSystemWorker.error("ScanOnline.run", e);
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<b>Since ").append(new Date(ConstantsFor.START_STAMP)).append(MoreInfoGetter.getTVNetInfo()).append("</b><br>");
        sb.append("Offline pc is <font color=\"red\">").append(new TForms().fromArray(NET_LIST_KEEPER.getOffLines(), true)).append("</font>");
        sb.append("Online  pc is<font color=\"#00ff69\"> ").append(new TForms().fromArray(onLinesResolve, true)).append("</font>");
        return sb.toString();
    }
}