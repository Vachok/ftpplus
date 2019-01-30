package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalTime;


/**
 Сканирование офлайн ПК, выключенных недавно
 <p>

 @since 28.01.2019 (16:48) */
public class ScanOffline implements Runnable {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final String SCAN_OFFLINE = "ScanOffline";

    private static ScanOffline scanOffline = new ScanOffline();

    private static final int TIMEOUT = 500;

    public static ScanOffline getI() {
        return scanOffline;
    }

    private ScanOffline() {
        LOGGER.warn("ScanOffline.ScanOffline");
    }

    @Override
    public void run() {
        scanComps();
    }


    private void scanComps() {
        PingListKeeper.getOffLines().forEach(this::offlinesActions);

        PingListKeeper.getOnLinesResolve().forEach(this::onlinesActions);

    }

    private void offlinesActions(String x, String y) {
        String classMeth = "ScanOffline.offlinesActions";
        new MessageCons().infoNoTitles(classMeth);
        try {
            byte[] address = InetAddress.getByName(x.replaceFirst("\\Q/\\E", "")).getAddress();
            InetAddress byAddress = InetAddress.getByAddress(address);
            if(byAddress.isReachable(TIMEOUT)){
                PingListKeeper.getOffLines().remove(x);
                PingListKeeper.getOnLinesResolve().put(x, LocalTime.now().toString());

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
            if(!inetAddress.isReachable(TIMEOUT)){
                PingListKeeper.getOnLinesResolve().remove(x);
                PingListKeeper.getOffLines().put(x, LocalTime.now().toString());
            }
        } catch (IOException e) {
            new MessageCons().errorAlert(SCAN_OFFLINE, "onlinesActions", e.getMessage());
            FileSystemWorker.error(classMeth, e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ScanOffline{");
        sb.append("toString()").append(" is running...\n");
        sb.append(PingListKeeper.getOffLines().size()).append(" offLines.size()");
        sb.append(PingListKeeper.getOnLinesResolve().size()).append("onLinesResolve.size()");
        sb.append('}');
        return sb.toString();
    }
}
