package ru.vachok.networker.net;


import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 Создание списков адресов, на все случаи жизни
 <p>

 @since 30.01.2019 (17:02) */
class NetListKeeper implements Serializable {

    private static final long serialVersionUID = 42L;

    /**
     {@link MessageLocal}
     */
    private static final MessageLocal LOGGER = new MessageLocal();

    private ConcurrentMap<String, String> onLinesResolve = new ConcurrentHashMap<>();

    private ConcurrentMap<String, String> offLines = new ConcurrentHashMap<>();

    ConcurrentMap<String, String> getOnLinesResolve() {
        return this.onLinesResolve;
    }

    ConcurrentMap<String, String> getOffLines() {
        return this.offLines;
    }

    /**
     @return {@link List} of {@link InetAddress}, из
     @throws IOException файловая система
     */
    static List<InetAddress> onlinesAddressesList() throws IOException {
        String classMeth = "NetListKeeper.onlinesAddressesList";
        new MessageCons().info(classMeth, "returns:", "java.util.List<java.net.InetAddress>");
        List<InetAddress> onlineAddresses = new ArrayList<>();
        Deque<String> fileAsDeque = NetScanFileWorker.getI().getListOfOnlineDev();
        Iterator<String> stringIterator = fileAsDeque.iterator();
        while (stringIterator.hasNext()) {
            String s = fileAsDeque.pollFirst();
            if (s != null) {
                byte[] inetBytesAddr = InetAddress.getByName(s.split(" ")[1]).getAddress();
                onlineAddresses.add(InetAddress.getByAddress(inetBytesAddr));
            }
        }
        LOGGER.info(classMeth, "returning: " + onlineAddresses.size(), " onlineAddresses");
        return onlineAddresses;
    }

    @Override
    public int hashCode() {
        int result = getOnLinesResolve().hashCode();
        result = 31 * result + getOffLines().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetListKeeper)) return false;

        NetListKeeper that = (NetListKeeper) o;

        if (!getOnLinesResolve().equals(that.getOnLinesResolve())) return false;
        return getOffLines().equals(that.getOffLines());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetListKeeper{");
        sb.append("LOGGER=").append(LOGGER.toString());
        sb.append(", offLines=").append(offLines);
        sb.append(", onLinesResolve=").append(onLinesResolve);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append('}');
        return sb.toString();
    }
}
