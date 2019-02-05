package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.MessageToTray;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;


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

    private static final NetListKeeper NET_LIST_KEEPER = NetListKeeper.getI();

    private static final String CLASS_NAME = "ScanOnline";

    /**
     new {@link ScanOnline}
     */
    private static final ScanOnline SCAN_ONLINE = new ScanOnline();

    /**
     {@link MessageToTray} with {@link ru.vachok.networker.systray.ActionDefault}
     */
    private MessageToUser messageToUser = new MessageToTray((ActionEvent e) -> {
        try{
            Desktop.getDesktop().browse(URI.create(ConstantsFor.SHOWALLDEV_NEEDSOPEN));
        }
        catch(IOException ignore){
            //
        }
    });

    public static ScanOnline getI() {
        new MessageLocal().errorAlert("ScanOnline.getI");
        return SCAN_ONLINE;
    }

    private ScanOnline() {
        new MessageCons().errorAlert("ScanOnline.ScanOnline");
    }

    private void runPing(List<InetAddress> onList) {
        String clMt = "ScanOnline.runPing";
        LOGGER.warn(clMt);
        for(InetAddress inetAddress : onList){
            pingAddr(inetAddress);
        }
        ThreadConfig.executeAsThread(this::offlineNotEmptyActions);
        messageToUser.info(getClass().getSimpleName(), ConstantsFor.getUpTime(), onList.size() +
            " online. Scanned: " + ConstantsFor.ALL_DEVICES.size() + "/" + ConstantsFor.IPS_IN_VELKOM_VLAN);
    }

    private void offlineNotEmptyActions() {
        SwitchesAvailability switchesAvailability = new SwitchesAvailability();
        switchesAvailability.run();
        Set<String> availabilityOkIP = switchesAvailability.getOkIP();
        availabilityOkIP.forEach(x -> NET_LIST_KEEPER.getOnLinesResolve().put(x, LocalDateTime.now().toString()));
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
        LOGGER.warn(classMeth);
        try{
            messageToUser = new MessageCons();
            boolean xReachable = inetAddress.isReachable(250);
            if(!xReachable){
                NET_LIST_KEEPER.getOffLines().put(inetAddress.toString(), LocalTime.now().toString());
                messageToUser.infoNoTitles(inetAddress.toString() + " is " + false);
            }
            else{
                NET_LIST_KEEPER.getOnLinesResolve().putIfAbsent(inetAddress.toString(), LocalTime.now().toString());
                messageToUser.info(CLASS_NAME, STR_RUN_PING, inetAddress.toString() + " " + true);
            }
        }
        catch(IOException e){
            new MessageCons().errorAlert(CLASS_NAME, "pingAddr", e.getMessage());
            FileSystemWorker.error(classMeth, e);
        }
        String valStr = "inetAddress = " + inetAddress + " ScanOnline.pingAddr";
        messageToUser = new MessageLocal();
        messageToUser.infoNoTitles(valStr);
    }

    @Override
    public void run() {
        LOGGER.warn("ScanOnline.run");
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
        final StringBuilder sb = new StringBuilder("ScanOnline{");
        sb.append("offPc=<font color=\"red\">").append(new TForms().fromArray(NET_LIST_KEEPER.getOffLines(), true));
        sb.append("</font>, onPc=<font color=\"#00ff69\">").append(new TForms().fromArray(NET_LIST_KEEPER.getOnLinesResolve(), true));
        sb.append("</font>}");
        return sb.toString();
    }
}