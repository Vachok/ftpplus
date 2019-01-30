package ru.vachok.networker.net;


import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 Создание списков адресов, на все случаи жизни
 <p>

 @since 30.01.2019 (17:02) */
class PingListCreator {

    private static final MessageLocal LOGGER = new MessageLocal();

    private static ConcurrentMap<String, String> onLinesResolve = new ConcurrentSkipListMap<>();

    private static ConcurrentMap<String, String> offLines = new ConcurrentSkipListMap<>();

    public static ConcurrentMap<String, String> getOffLines() {
        return offLines;
    }

    public static void setOffLines(ConcurrentMap<String, String> offLines) {
        PingListCreator.offLines = offLines;
    }

    static ConcurrentMap<String, String> getOnLinesResolve() {
        return onLinesResolve;
    }

    public static void setOnLinesResolve(ConcurrentMap<String, String> onLinesResolve) {
        PingListCreator.onLinesResolve = onLinesResolve;
    }

    static List<InetAddress> onlinesAddressesList() throws IOException {
        LOGGER.warning("PingListCreator.onlinesAddressesList");
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
}
