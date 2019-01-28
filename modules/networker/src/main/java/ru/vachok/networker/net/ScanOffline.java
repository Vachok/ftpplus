package ru.vachok.networker.net;


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

    @Override
    public void run() {
        if (scanOnline != null && scanOnline.equals(ScanOnline.getI())) {
            scanOff();
        } else {
            throw new IllegalStateException("ScanOnline not match");
        }
    }

    private void scanOff() {
        ConcurrentMap<String, String> offLines = scanOnline.getOffLines();
        ConcurrentMap<String, String> onLinesResolve = scanOnline.getOnLinesResolve();
        offLines.forEach((x, y) -> {
            try {
                byte[] address = InetAddress.getByName(x.replaceFirst("\\Q/\\E", "")).getAddress();
                InetAddress byAddress = InetAddress.getByAddress(address);
                if (byAddress.isReachable(500)) {
                    offLines.remove(x);
                    onLinesResolve.put(x, LocalTime.now().toString());
                }
            } catch (IOException e) {
                LOGGER.throwing("ScanOffline", "scanOff", e);
            }
        });
    }
}
