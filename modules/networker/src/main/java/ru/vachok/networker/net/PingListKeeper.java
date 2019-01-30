package ru.vachok.networker.net;


import ru.vachok.networker.TForms;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 Создание списков адресов, на все случаи жизни
 <p>

 @since 30.01.2019 (17:02) */
class PingListKeeper {

    /**
     {@link MessageLocal}
     */
    private static final MessageLocal LOGGER = new MessageLocal();

    private static ConcurrentMap<String, String> onLinesResolve = new ConcurrentHashMap<>();

    private static ConcurrentMap<String, String> offLines = new ConcurrentHashMap<>();

    static ConcurrentMap<String, String> getOffLines() {
        int size = offLines.size();
        if(size > 0){
            LOGGER.infoNoTitles("PingListKeeper.getOffLines = " + size);
        }
        return offLines;
    }
    static ConcurrentMap<String, String> getOnLinesResolve() {
        int size = onLinesResolve.size();
        if(size > 0){
            LOGGER.infoNoTitles("PingListKeeper.getOnLinesResolve = " + size);
        }
        return onLinesResolve;
    }

    static List<InetAddress> onlinesAddressesList() throws IOException {
        String classMeth = "PingListKeeper.onlinesAddressesList";
        LOGGER.warn(classMeth);
        List<InetAddress> onlineAddresses = new ArrayList<>();
        Deque<String> fileAsDeque = NetScanFileWorker.getI().getListOfOnlineDev();
        Iterator<String> stringIterator = fileAsDeque.iterator();
        while(stringIterator.hasNext()){
            String[] sS = Objects.requireNonNull(fileAsDeque.pollFirst()).split(" ");
            byte[] inetBytesAddr = InetAddress.getByName(sS[1]).getAddress();
            onlineAddresses.add(InetAddress.getByAddress(inetBytesAddr));
            LOGGER.info(classMeth, onlineAddresses.size() + "onlineAddresses.size", fileAsDeque.size() + "fileAsDeque.size");
        }
        return onlineAddresses;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PingListKeeper{");
        sb.append("\noffLines=").append(new TForms().fromArray(offLines, false));
        sb.append("\n onLinesResolve=").append(new TForms().fromArray(onLinesResolve, false));
        sb.append('}');
        return sb.toString();
    }
}
