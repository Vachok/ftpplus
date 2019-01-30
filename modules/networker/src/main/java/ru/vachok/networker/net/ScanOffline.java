package ru.vachok.networker.net;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 Сканирование офлайн ПК, выключенных недавно
 <p>

 @since 28.01.2019 (16:48) */
public class ScanOffline implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ScanOffline.class.getSimpleName());

    private ScanOnline scanOnline = ScanOnline.getI();

    private static MessageToUser messageToUser = new MessageCons();

    private static ScanOffline scanOffline = new ScanOffline();

    private ConcurrentMap<String, String> onLinesResolve = PingListCreator.getOnLinesResolve();

    private ConcurrentMap<String, String> offLines = PingListCreator.getOffLines();

    public static ScanOffline getI() {
        return scanOffline;
    }

    private ScanOffline() {
    }

    @Override
    public void run() {
        if (scanOnline != null && scanOnline.equals(ScanOnline.getI())) {
            scanOff();
        } else {
            throw new IllegalStateException("ScanOnline not match");
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ScanOffline{");
        sb.append(getClass().getSimpleName()).append(" is running...\n");
        sb.append(PingListCreator.getOffLines().size()).append("scanOnline.getOffLines().size()");
        sb.append(onLinesResolve.size()).append("scanOnline.getOnLinesResolve().size()");
        sb.append('}');
        return sb.toString();
    }

    private void scanOff() {
        this.offLines.forEach(this::offlinesActions);
        this.onLinesResolve.forEach(this::onlinesActions);
        PingListCreator.setOnLinesResolve(this.onLinesResolve);
        PingListCreator.setOffLines(this.onLinesResolve);
    }

    private void offlinesActions(String x, String y) {
        LOGGER.info("ScanOffline.offlinesActions");
        try {
            byte[] address = InetAddress.getByName(x.replaceFirst("\\Q/\\E", "")).getAddress();
            InetAddress byAddress = InetAddress.getByAddress(address);
            if (byAddress.isReachable(500)) {
                offLines.remove(x);
                messageToUser.infoNoTitles(onLinesResolve.putIfAbsent(x, LocalTime.now().toString()));
            }
        } catch (IOException e) {
            LOGGER.throwing("ScanOffline", "scanOff", e);
        }
    }

    private void onlinesActions(String x, String y) {
        LOGGER.info("ScanOffline.onlinesActions");
        try {
            byte[] aBytes = InetAddress.getByName(x).getAddress();
            InetAddress inetAddress = InetAddress.getByAddress(aBytes);
            if (!inetAddress.isReachable(500)) {
                onLinesResolve.remove(x);
                messageToUser.infoNoTitles(offLines.putIfAbsent(x, LocalTime.now().toString()));
            }
        } catch (IOException e) {
            new MessageCons().errorAlert("ScanOffline", "onlinesActions", e.getMessage());
            FileSystemWorker.error("ScanOffline.onlinesActions", e);
        }
    }
}
