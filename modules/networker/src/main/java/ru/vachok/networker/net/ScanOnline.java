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

    private static final String CLASS_NAME = "ScanOnline";

    /**
     new {@link ScanOnline}
     */
    private static ScanOnline scanOnline = new ScanOnline();

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

    private ScanOnline() {
    }

    public static ScanOnline getI() {
        return scanOnline;
    }

    @Override
    public void run() {
        messageToUser.infoNoTitles("ScanOnline.run");
        try {
            List<InetAddress> onList = PingListKeeper.onlinesAddressesList();
            runPing(onList);
        } catch (IOException e) {
            messageToUser = new MessageCons();
            messageToUser.errorAlert(getClass().getSimpleName(), e.getMessage(), new TForms().fromArray(e, false));
        }
    }

    private void runPing(List<InetAddress> onList) {
        String clMt = "ScanOnline.runPing";
        LOGGER.warn(clMt);
        for(InetAddress inetAddress : onList){
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
     1. {@link PingListKeeper#getOffLines()}. Если нет пинга, добавляет {@code inetAddress.toString, LocalTime.now.toString} и запускает: <br>
     2. {@link ThreadConfig#executeAsThread(java.lang.Runnable)}. {@link #offlineNotEmptyActions()} <br>
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
                PingListKeeper.getOffLines().put(inetAddress.toString(), LocalTime.now().toString());
                ThreadConfig.executeAsThread(this::offlineNotEmptyActions);
                messageToUser.infoNoTitles(inetAddress.toString() + " is " + false);
            }
            else{
                messageToUser.info(CLASS_NAME, STR_RUN_PING, inetAddress.toString() + " " + true);
            }
        }
        catch(IOException e){
            new MessageCons().errorAlert(CLASS_NAME, "pingAddr", e.getMessage());
            FileSystemWorker.error(classMeth, e);
        }
    }

    private void offlineNotEmptyActions() {
        SwitchesAvailability switchesAvailability = new SwitchesAvailability();
        switchesAvailability.run();
        Set<String> availabilityOkIP = switchesAvailability.getOkIP();
        this.okIP = new ArrayList<>();
        boolean addAll = okIP.addAll(availabilityOkIP);
        messageToUser.info(getClass().getSimpleName(), okIP.size() + " sw ips", addAll + " added");
    }

    @Override
    public String toString() {
        new MessageCons().infoNoTitles("ScanOnline.toString");
        if(!PingListKeeper.getOffLines().isEmpty()){
            final StringBuilder sb = new StringBuilder("ScanOnOffline{");
            sb.append(" OffLines (").append(PingListKeeper.getOffLines().size()).append(") = <font color=\"red\">")
                .append(new TForms().fromArray(PingListKeeper.getOffLines(), true)).append("</font><br>");
            sb.append(" OnLineAgain (").append(PingListKeeper.getOnLinesResolve().size()).append(") = <font color=\"green\">")
                .append(new TForms().fromArray(PingListKeeper.getOnLinesResolve(), true)).append("</font><br>")
                .append("<font color=\"gray\">SwitchesWiFi: ")
                .append(new TForms().fromArray(okIP, true)).append("</font>");
            return sb.toString();
        } else {
            return "<font color=\"green\">NO</font>";
        }
    }

}