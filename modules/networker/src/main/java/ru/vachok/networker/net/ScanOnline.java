package ru.vachok.networker.net;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.MessageToTray;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 Сканирование только тех, что он-лайн
 <p>

 @see DiapazonedScan
 @since 26.01.2019 (11:18) */
public class ScanOnline implements Runnable {

    private static final NetListKeeper NET_LIST_KEEPER = NetListKeeper.getI();

    /**
     new {@link ScanOnline}
     */
    private static final ScanOnline SCAN_ONLINE = new ScanOnline();

    /**
     {@link MessageToTray} with {@link ru.vachok.networker.systray.ActionDefault}
     */
    private MessageToUser messageToUser = new MessageLocal();

    public static ScanOnline getI() {
        new MessageLocal().warning("ScanOnline.getI");
        return SCAN_ONLINE;
    }

    private ScanOnline() {
        new MessageCons().errorAlert("ScanOnline.ScanOnline");
    }

    private void offlineNotEmptyActions() {
        SwitchesAvailability switchesAvailability = new SwitchesAvailability();
        switchesAvailability.run();
        Set<String> availabilityOkIP = switchesAvailability.getOkIP();
        availabilityOkIP.forEach(x -> NET_LIST_KEEPER.getOnLinesResolve().put(x, LocalDateTime.now().toString()));
    }

    private void runPing(List<InetAddress> onList) {
        String clMt = "ScanOnline.runPing. IPs to ping: " + onList.size();
        messageToUser.infoNoTitles(clMt);
        for (InetAddress inetAddress : onList) {
            pingAddr(inetAddress);
        }
        messageToUser.info(getClass().getSimpleName(), ConstantsFor.getUpTime(), onList.size() +
            " online. Scanned: " + ConstantsFor.ALL_DEVICES.size() + "/" + ConstantsFor.IPS_IN_VELKOM_VLAN);
        new MessageLocal().warning(NET_LIST_KEEPER.getOffLines().size() + " off; " + NET_LIST_KEEPER.getOnLinesResolve().size() + " on");
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
                if (NET_LIST_KEEPER.getOnLinesResolve().containsKey(inetAddress.toString())) {
                    NET_LIST_KEEPER.getOffLines().remove(inetAddress.toString());
                }
            } else {
                NET_LIST_KEEPER.getOnLinesResolve().putIfAbsent(inetAddress.toString(), LocalTime.now().toString());
            }
        } catch (IOException e) {
            FileSystemWorker.error("ScanOnline.pingAddr", e);
        }
    }

    @Override
    public void run() {
        ThreadConfig.executeAsThread(this::offlineNotEmptyActions);
        try {
            List<InetAddress> onList = NET_LIST_KEEPER.onlinesAddressesList();
            runPing(onList);
        } catch (IOException e) {
            messageToUser = new MessageCons();
            messageToUser.errorAlert(getClass().getSimpleName(), e.getMessage(), new TForms().fromArray(e, false));
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<b>Since ").append(new Date(ConstantsFor.START_STAMP)).append(MoreInfoGetter.getTVNetInfo()).append("</b><br>");
        sb.append("Offline pc is <font color=\"red\">").append(new TForms().fromArray(NET_LIST_KEEPER.getOffLines(), true)).append("</font>");
        sb.append("Online  pc is<font color=\"#00ff69\"> ").append(new TForms().fromArray(NET_LIST_KEEPER.getOnLinesResolve(), true)).append("</font>");
        sb.append("NET_LIST_KEEPER=").append(NET_LIST_KEEPER.hashCode());
        sb.append(", SCAN_ONLINE=").append(SCAN_ONLINE.hashCode());
        sb.append(ConstantsFor.TOSTRING_MESSAGE_TO_USER).append(messageToUser.toString());
        return sb.toString();
    }
}