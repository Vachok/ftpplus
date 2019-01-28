package ru.vachok.networker.net;


import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.logging.Logger;

/**
 Сканирование офлайн ПК, выключенных недавно
 <p>

 @since 28.01.2019 (16:48) */
public class ScanOffline implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ScanOffline.class.getSimpleName());

    private ScanOnline scanOnline = ScanOnline.getI();

    private static ScanOffline scanOffline = new ScanOffline();

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

    private void scanOff() {
        scanOnline.getOffLines().forEach((x, y) -> {
            try {
                byte[] address = InetAddress.getByName(x.replaceFirst("\\Q/\\E", "")).getAddress();
                InetAddress byAddress = InetAddress.getByAddress(address);
                if (byAddress.isReachable(500)) {
                    scanOnline.getOffLines().remove(x);
                    scanOnline.getOnLinesResolve().put(x, LocalTime.now().toString());
                }
            } catch (IOException e) {
                LOGGER.throwing("ScanOffline", "scanOff", e);
            }
        });
        scanOnline.getOnLinesResolve().forEach((x, y) -> {
            try{
                byte[] aBytes = InetAddress.getByName(x).getAddress();
                InetAddress inetAddress = InetAddress.getByAddress(aBytes);
                if(!inetAddress.isReachable(500)){
                    scanOnline.getOnLinesResolve().remove(x);
                    scanOnline.getOffLines().put(x, LocalTime.now().toString());
                }
            }
            catch(IOException e){
                LOGGER.throwing(getClass().getSimpleName(), e.getMessage(), e);
            }
        });
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ScanOffline{");
        sb.append(getClass().getSimpleName()).append(" is running...\n");
        sb.append(scanOnline.getOffLines().size()).append("scanOnline.getOffLines().size()");
        sb.append(scanOnline.getOnLinesResolve().size()).append("scanOnline.getOnLinesResolve().size()");
        sb.append('}');
        return sb.toString();
    }
}
