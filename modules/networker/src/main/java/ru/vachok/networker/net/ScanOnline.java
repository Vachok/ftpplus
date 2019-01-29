package ru.vachok.networker.net;


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
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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

    private ConcurrentMap<String, String> offLines = new ConcurrentHashMap<>();

    private ConcurrentMap<String, String> onLinesResolve = new ConcurrentHashMap<>();

    private List<String> okIP = new ArrayList<>();

    @Override
    public int hashCode() {
        int result = messageToUser.hashCode();
        result = 31 * result + getOffLines().hashCode();
        result = 31 * result + getOnLinesResolve().hashCode();
        return result;
    }

    private ScanOnline() {
    }

    public static ScanOnline getI() {
        return scanOnline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScanOnline)) return false;

        ScanOnline that = (ScanOnline) o;

        if (!messageToUser.equals(that.messageToUser)) return false;
        if (!getOffLines().equals(that.getOffLines())) return false;
        return getOnLinesResolve().equals(that.getOnLinesResolve());
    }

    @Override
    public String toString() {
        if (!offLines.isEmpty()) {
            final StringBuilder sb = new StringBuilder("ScanOnOffline{");
            sb.append(" OffLines (").append(offLines.size()).append(") = <font color=\"red\">")
                .append(new TForms().fromArray(offLines, true)).append("</font><br>");
            sb.append(" OnLineAgain (").append(onLinesResolve.size()).append(") = <font color=\"green\">")
                .append(new TForms().fromArray(onLinesResolve, true)).append("</font><br>")
                .append("<font color=\"gray\">Switches: ")
                .append(new TForms().fromArray(okIP, true)).append("</font>");
            return sb.toString();
        } else {
            return "<font color=\"green\">NO</font>";
        }
    }

    ConcurrentMap<String, String> getOnLinesResolve() {
        return onLinesResolve;
    }

    ConcurrentMap<String, String> getOffLines() {
        return offLines;
    }

    @Override
    public void run() {
        messageToUser.infoNoTitles("ScanOnline.run");
        try {
            List<InetAddress> onList = onlinesAddressesList();
            runPing(onList);
        } catch (IOException e) {
            messageToUser = new MessageCons();
            messageToUser.errorAlert(getClass().getSimpleName(), e.getMessage(), new TForms().fromArray(e, false));
        }
    }

    private List<InetAddress> onlinesAddressesList() throws IOException {
        messageToUser = new MessageCons();
        List<InetAddress> onlineAddresses = new ArrayList<>();
        List<String> fileAsList = NetScanFileWorker.getI().getListOfOnlineDev();
        fileAsList.forEach((String x) -> {
            try {
                String[] sS = x.split(" ");
                byte[] inetBytesAddr = InetAddress.getByName(sS[1]).getAddress();
                onlineAddresses.add(InetAddress.getByAddress(inetBytesAddr));
            } catch (ArrayIndexOutOfBoundsException | UnknownHostException ignore) {
                //
            }
        });
        return onlineAddresses;
    }

    private void runPing(List<InetAddress> onList) {
        onList.forEach((InetAddress x) -> {
            try {
                messageToUser = new MessageCons();
                boolean xReachable = x.isReachable(250);
                messageToUser.info(
                    getClass().getSimpleName(),
                    x.toString(),
                    "is online: " + xReachable);
                if (!xReachable) {
                    messageToUser.infoNoTitles(offLines.putIfAbsent(x.toString(), LocalTime.now().toString()));
                }
            } catch (IOException e) {
                messageToUser = new MessageToTray();
                FileSystemWorker.recFile(
                    getClass().getSimpleName() + ConstantsFor.LOG,
                    e.getMessage() + "\n" + new TForms().fromArray(e, false));
                messageToUser.errorAlert(getClass().getSimpleName(), STR_RUN_PING, e.getMessage());
            }
        });
        if(!offLines.isEmpty()){
            ThreadConfig.executeAsThread(() -> {
                AppComponents.getLogger().warn("ScanOnline.runPing");
                SwitchesAvailability switchesAvailability = new SwitchesAvailability();
                switchesAvailability.run();
                okIP.addAll(switchesAvailability.getOkIP());
            });
        }
        messageToUser.info(
            getClass().getSimpleName(),
            ConstantsFor.getUpTime(),
            onList.size() + " online. Scanned: " + ConstantsFor.ALL_DEVICES.size() + "/" + ConstantsFor.IPS_IN_VELKOM_VLAN);
    }
}