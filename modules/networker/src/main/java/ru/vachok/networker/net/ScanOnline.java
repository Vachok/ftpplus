package ru.vachok.networker.net;


import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.InfoWorker;
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
import java.util.concurrent.*;


/**
 Сканирование только тех, что он-лайн
 <p>

 @see DiapazonedScan
 @since 26.01.2019 (11:18) */
@Service
public class ScanOnline implements Runnable {


    /**
     {@link NetListKeeper#getI()}
     */
    private static final NetListKeeper NET_LIST_KEEPER = NetListKeeper.getI();

    /**
     {@link NetListKeeper#getOnLinesResolve()}
     */
    private ConcurrentMap<String, String> onLinesResolve = NET_LIST_KEEPER.getOnLinesResolve();

    /**
     {@link MessageLocal}
     */
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private InfoWorker tvInfo = new MoreInfoWorker("tv");


    @Override
    public void run() {
        AppComponents.threadConfig().execByThreadConfig(this::offlineNotEmptyActions);
        try {
            List<InetAddress> onList = NET_LIST_KEEPER.onlinesAddressesList();
            runPing(onList);
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".run" , e));
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<b>Since ").append("<i>").append(new Date(NetScanFileWorker.getI().getLastStamp())).append("</i>").append(tvInfo.getInfoAbout()).append("</b><br><br>");
        sb.append("Offline pc is <font color=\"red\"><b>").append(NET_LIST_KEEPER.getOffLines().size()).append(":</b></font><br>");
        sb.append("Online  pc is<font color=\"#00ff69\"> <b>").append(onLinesResolve.size()).append(":</b><br>").append(new TForms().fromArray(onLinesResolve, true)).append("</font><br>");
        return sb.toString();
    }


    private void offlineNotEmptyActions() {
        AppComponents.threadConfig().thrNameSet("scOffNE");
        SwitchesAvailability switchesAvailability = new SwitchesAvailability();
        final Future<?> submit = AppComponents.threadConfig().getTaskExecutor().submit(switchesAvailability);
        try {
            submit.get(ConstantsFor.DELAY * 2, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }

        Set<String> availabilityOkIP = switchesAvailability.getOkIP();
        availabilityOkIP.forEach(x->{
            onLinesResolve.put(x, LocalDateTime.now().toString());
        });
    }


    private void runPing(List<InetAddress> onList) {
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
    ThreadConfig#execByThreadConfig(java.lang.Runnable)}. {@link #offlineNotEmptyActions()} <br>
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
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".pingAddr" , e));
        }
    }
}