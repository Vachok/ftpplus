package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.systray.MessageToTray;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.time.LocalTime;
import java.util.ArrayList;
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
     <i>Boiler Plate</i>
     */
    private static final String STR_RUN_PING = "runPing";

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final NetListKeeper NET_LIST_KEEPER = ScanOffline.getNetListKeeper();

    private static final String CLASS_NAME = "ScanOnline";

    /**
     new {@link ScanOnline}
     */
    private static ScanOnline scanOnline = new ScanOnline();

    private ConcurrentMap<String, String> offPc = NET_LIST_KEEPER.getOffLines();

    private ConcurrentMap<String, String> onPc = NET_LIST_KEEPER.getOnLinesResolve();

    /**
     {@link MessageToTray} with {@link ru.vachok.networker.systray.ActionDefault}
     */
    private MessageToUser messageToUser = new MessageToTray((ActionEvent e) -> {
        try {
            Desktop.getDesktop().browse(URI.create(ConstantsFor.SHOWALLDEV_NEEDSOPEN));
        } catch (IOException ignore) {
            //
        }
    });

    private List<String> okIP = new ArrayList<>();

    public static ScanOnline getI() {
        return scanOnline;
    }

    private ScanOnline() {
        LOGGER.warn("ScanOnline.ScanOnline");
    }

    private void runPing(List<InetAddress> onList) {
        String clMt = "ScanOnline.runPing";
        LOGGER.warn(clMt);
        for (InetAddress inetAddress : onList) {
            pingAddr(inetAddress);
        }
        messageToUser.info(getClass().getSimpleName(), ConstantsFor.getUpTime(), onList.size() +
            " online. Scanned: " + ConstantsFor.ALL_DEVICES.size() + "/" + ConstantsFor.IPS_IN_VELKOM_VLAN);
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
        String classMeth = "ScanOnline.pingAddr";
        ConcurrentMap<String, String> offLines = new NetListKeeper().getOffLines();
        LOGGER.warn(classMeth);
        try {
            messageToUser = new MessageCons();
            boolean xReachable = inetAddress.isReachable(250);
            if (!xReachable) {
                offLines.put(inetAddress.toString(), LocalTime.now().toString());
                ThreadConfig.executeAsThread(this::offlineNotEmptyActions);
                messageToUser.infoNoTitles(inetAddress.toString() + " is " + false);
            } else {
                messageToUser.info(CLASS_NAME, STR_RUN_PING, inetAddress.toString() + " " + true);
            }
        } catch (IOException e) {
            new MessageCons().errorAlert(CLASS_NAME, "pingAddr", e.getMessage());
            FileSystemWorker.error(classMeth, e);
        }
        String valStr = "inetAddress = " + inetAddress + " ScanOnline.pingAddr";
        java.util.logging.Logger.getGlobal().warning(valStr);
    }

    private void offlineNotEmptyActions() {
        SwitchesAvailability switchesAvailability = new SwitchesAvailability();
        switchesAvailability.run();
        Set<String> availabilityOkIP = switchesAvailability.getOkIP();
        this.okIP = new ArrayList<>();
        boolean addAllavailabilityOkIP = okIP.addAll(availabilityOkIP);
        String valStr = "addAllavailabilityOkIP = " + addAllavailabilityOkIP + " ScanOnline.offlineNotEmptyActions";
        java.util.logging.Logger.getGlobal().warning(valStr);
    }

    @Override
    public void run() {
        LOGGER.warn("ScanOnline.run");
        try {
            List<InetAddress> onList = NetListKeeper.onlinesAddressesList();
            runPing(onList);
        } catch (IOException e) {
            messageToUser = new MessageCons();
            messageToUser.errorAlert(getClass().getSimpleName(), e.getMessage(), new TForms().fromArray(e, false));
        }
    }

    @Override
    public int hashCode() {
        int result = offPc.hashCode();
        result = 31 * result + onPc.hashCode();
        result = 31 * result + messageToUser.hashCode();
        result = 31 * result + okIP.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScanOnline)) return false;

        ScanOnline that = (ScanOnline) o;

        if (!offPc.equals(that.offPc)) return false;
        if (!onPc.equals(that.onPc)) return false;
        if (!messageToUser.equals(that.messageToUser)) return false;
        return okIP.equals(that.okIP);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("<font color=\"green\">ScanOnline{").append(this.hashCode()).append("<br>");
        sb.append("CLASS_NAME='").append(CLASS_NAME).append('\'');
        sb.append(", messageToUser=").append(messageToUser.getClass().getSimpleName());
        sb.append(", NET_LIST_KEEPER=").append(NET_LIST_KEEPER.hashCode());
        sb.append(", <b>offPc=").append(offPc.size());
        sb.append(", okIP=").append(okIP.size());
        sb.append(", onPc=").append(onPc.size());
        sb.append(", </b>scanOnline=").append(scanOnline.hashCode());
        sb.append(", STR_RUN_PING='").append(STR_RUN_PING).append('\'');
        sb.append("}</font>");
        return sb.toString();
    }
}