package ru.vachok.networker.net;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
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
import java.util.concurrent.TimeUnit;


/**
 Сканирование только тех, что он-лайн
 <p>

 @see DiapazonedScan
 @since 26.01.2019 (11:18) */
public class ScanOnline implements Runnable {

    /**
     new {@link ScanOnline}
     */
    private static ScanOnline scanOnline = new ScanOnline();

    /**
     {@link MessageToTray} with {@link ru.vachok.networker.systray.ActionDefault}
     */
    private MessageToUser messageToUser = new MessageToTray((ActionEvent e) -> {
        try {
            Desktop.getDesktop().browse(URI.create("http://localhost:8880/showalldev?needsopen"));
        } catch (IOException ignore) {
            //
        }
    });

    private ConcurrentMap<String, String> offLines = new ConcurrentHashMap<>();

    @Override
    public String toString() {
        if (!offLines.isEmpty()) {
            final StringBuilder sb = new StringBuilder("ScanOnline{");
            sb.append("Minutes past=")
                .append(TimeUnit.MILLISECONDS
                    .toMinutes(System.currentTimeMillis() - NetScanFileWorker.getI().getLastStamp()));
            sb.append(" OffLines=<font color=\"red\">").append(new TForms().fromArray(offLines, true));
            sb.append("</font>}");
            return sb.toString();
        } else {
            return "<font color=\"green\">NO</font>";
        }
    }

    private ScanOnline() {
    }

    public static ScanOnline getI() {
        return scanOnline;
    }

    @Override
    public int hashCode() {
        int result = messageToUser != null ? messageToUser.hashCode() : 0;
        result = 31 * result + (offLines != null ? offLines.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScanOnline)) return false;

        ScanOnline that = (ScanOnline) o;

        if (messageToUser != null ? !messageToUser.equals(that.messageToUser) : that.messageToUser != null) return false;
        return offLines != null ? offLines.equals(that.offLines) : that.offLines == null;
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
                    offLines.put(x.toString(), LocalTime.now().toString());
                    new Thread(() -> new SwitchesAvailability().run()).start();
                }

            } catch (IOException e) {
                messageToUser = new MessageToTray();
                FileSystemWorker.recFile(
                    getClass().getSimpleName() + ConstantsFor.LOG,
                    e.getMessage() + "\n" + new TForms().fromArray(e, false));
                messageToUser.errorAlert(getClass().getSimpleName(), "runPing", e.getMessage());
            }
        });
        messageToUser.info(
            getClass().getSimpleName(),
            ConstantsFor.getUpTime(),
            onList.size() + " online. Scanned: " + ConstantsFor.ALL_DEVICES.size() + "/" + ConstantsFor.IPS_IN_VELKOM_VLAN);
    }
}