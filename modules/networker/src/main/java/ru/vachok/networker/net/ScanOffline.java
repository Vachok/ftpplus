package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentMap;


/**
 Сканирование офлайн ПК, выключенных недавно
 <p>

 @since 28.01.2019 (16:48) */
public class ScanOffline implements Runnable {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final String SCAN_OFFLINE = "ScanOffline";

    private static final int TIMEOUT = 500;

    private static final NetListKeeper NET_LIST_KEEPER = new NetListKeeper();

    private static ScanOffline scanOffline = new ScanOffline();

    private ConcurrentMap<String, String> offPC = NET_LIST_KEEPER.getOffLines();

    private ConcurrentMap<String, String> onPc = NET_LIST_KEEPER.getOnLinesResolve();

    static NetListKeeper getNetListKeeper() {
        return NET_LIST_KEEPER;
    }

    public static ScanOffline getI() {
        new MessageCons().errorAlert("ScanOffline.getI");
        new MessageCons().info(ConstantsFor.STR_INPUT_OUTPUT, scanOffline.hashCode() + " hash", "ru.vachok.networker.net.ScanOffline");
        return scanOffline;
    }

    private ScanOffline() {
        LOGGER.warn("ScanOffline.ScanOffline");
    }

    private void scanComps() {
        if (offPC.equals(NET_LIST_KEEPER.getOffLines())) {
            offPC.forEach(this::offlinesActions);
        } else {
            throw new IllegalComponentStateException(offPC.hashCode() + " this offPC\n" + NET_LIST_KEEPER.getOffLines().hashCode() + " " + NET_LIST_KEEPER.getClass().getSimpleName());
        }
        if (onPc.equals(NET_LIST_KEEPER.getOnLinesResolve())) {
            onPc.forEach(this::onlinesActions);
        } else {
            throw new IllegalComponentStateException(onPc.hashCode() + " this onPc\n" + NET_LIST_KEEPER.getOffLines().hashCode() + " " + NET_LIST_KEEPER.getClass().getSimpleName());

        }
    }

    private void offlinesActions(String x, String y) {
        String classMeth = "ScanOffline.offlinesActions";
        new MessageCons().infoNoTitles(classMeth);
        try {
            byte[] address = InetAddress.getByName(x.replaceFirst("\\Q/\\E", "")).getAddress();
            InetAddress byAddress = InetAddress.getByAddress(address);
            if (byAddress.isReachable(TIMEOUT)) {
                offPC.remove(x);
                onPc.put(x, LocalTime.now().toString());

            }
        } catch (IOException e) {
            new MessageCons().errorAlert(SCAN_OFFLINE, "offlinesActions", e.getMessage());
            FileSystemWorker.error(classMeth, e);
        }
    }

    private void onlinesActions(String x, String y) {
        String classMeth = "ScanOffline.onlinesActions";
        LOGGER.warn(classMeth);
        new MessageCons().infoNoTitles("x = [" + x + "], y = [" + y + "]");

        try {
            byte[] aBytes = InetAddress.getByName(x).getAddress();
            InetAddress inetAddress = InetAddress.getByAddress(aBytes);
            if (!inetAddress.isReachable(TIMEOUT)) {
                onPc.remove(x);
                offPC.put(x, LocalTime.now().toString());
            }
        } catch (IOException e) {
            new MessageCons().errorAlert(SCAN_OFFLINE, "onlinesActions", e.getMessage());
            FileSystemWorker.error(classMeth, e);
        }
    }

    @Override
    public int hashCode() {
        int result = offPC.hashCode();
        result = 31 * result + onPc.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScanOffline)) return false;

        ScanOffline that = (ScanOffline) o;

        if (!offPC.equals(that.offPC)) return false;
        return onPc.equals(that.onPc);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("<font color=\"red\">ScanOffline{").append(this.hashCode()).append("<br>");
        sb.append("NET_LIST_KEEPER=").append(NET_LIST_KEEPER.hashCode());
        sb.append(", <b>offPC=").append(offPC.size());
        sb.append(", onPc=").append(onPc.size());
        sb.append(", </b>SCAN_OFFLINE='").append(SCAN_OFFLINE).append('\'');
        sb.append(", scanOffline=").append(scanOffline.hashCode());
        sb.append(", TIMEOUT=").append(TIMEOUT);
        sb.append("}</font>");
        return sb.toString();
    }

    @Override
    public void run() {
        LOGGER.warn("ScanOffline.run");
        scanComps();
    }
}
