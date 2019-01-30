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
            List<InetAddress> onList = PingListCreator.onlinesAddressesList();
            runPing(onList);
        } catch (IOException e) {
            messageToUser = new MessageCons();
            messageToUser.errorAlert(getClass().getSimpleName(), e.getMessage(), new TForms().fromArray(e, false));
        }
    }

    private void runPing(List<InetAddress> onList) {
        LOGGER.warn("ScanOnline.runPing");
        onList.forEach((InetAddress x) -> {
            try {
                messageToUser = new MessageCons();
                boolean xReachable = x.isReachable(250);
                messageToUser.info(
                    getClass().getSimpleName(),
                    x.toString(),
                    "is online: " + xReachable);
                if (!xReachable) {
                    messageToUser.infoNoTitles(PingListCreator.getOffLines().putIfAbsent(x.toString(), LocalTime.now().toString()));
                }
            } catch (IOException e) {
                messageToUser = new MessageToTray();
                FileSystemWorker.recFile(
                    getClass().getSimpleName() + ConstantsFor.LOG,
                    e.getMessage() + "\n" + new TForms().fromArray(e, false));
                messageToUser.errorAlert(getClass().getSimpleName(), STR_RUN_PING, e.getMessage());
            }
        });
        if (!PingListCreator.getOffLines().isEmpty()) {
            ThreadConfig.executeAsThread(() -> {
                AppComponents.getLogger().warn("ScanOnline.runPing");
                SwitchesAvailability switchesAvailability = new SwitchesAvailability();
                switchesAvailability.run();
                Set<String> availabilityOkIP = switchesAvailability.getOkIP();
                this.okIP = new ArrayList<>();
                boolean addAll = okIP.addAll(availabilityOkIP);
                messageToUser.info(getClass().getSimpleName(), okIP.size() + " sw ips", addAll + " added");
            });
        }
        messageToUser.info(getClass().getSimpleName(), ConstantsFor.getUpTime(), onList.size() +
            " online. Scanned: " + ConstantsFor.ALL_DEVICES.size() + "/" + ConstantsFor.IPS_IN_VELKOM_VLAN);
    }

    @Override
    public String toString() {
        if (!PingListCreator.getOffLines().isEmpty()) {
            final StringBuilder sb = new StringBuilder("ScanOnOffline{");
            ConcurrentMap<String, String> offLines = PingListCreator.getOffLines();
            ConcurrentMap<String, String> onLinesResolve = PingListCreator.getOnLinesResolve();
            sb.append(" OffLines (").append(offLines.size()).append(") = <font color=\"red\">")
                .append(new TForms().fromArray(offLines, true)).append("</font><br>");
            sb.append(" OnLineAgain (").append(onLinesResolve.size()).append(") = <font color=\"green\">")
                .append(new TForms().fromArray(onLinesResolve, true)).append("</font><br>")
                .append("<font color=\"gray\">SwitchesWiFi: ")
                .append(new TForms().fromArray(okIP, true)).append("</font>");
            return sb.toString();
        } else {
            return "<font color=\"green\">NO</font>";
        }
    }

}