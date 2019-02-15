package ru.vachok.networker.net;


import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 Создание списков адресов, на все случаи жизни
 <p>

 @since 30.01.2019 (17:02) */
public class NetListKeeper implements Serializable {

    private static final long serialVersionUID = 42L;

    private static final NetListKeeper NET_LIST_KEEPER = new NetListKeeper();

    /**
     {@link MessageLocal}
     */
    private static final MessageLocal LOGGER = new MessageLocal();

    private ConcurrentMap<String, String> onLinesResolve = new ConcurrentHashMap<>();

    private ConcurrentMap<String, String> offLines = new ConcurrentHashMap<>();

    public static NetListKeeper getI() {
        return NET_LIST_KEEPER;
    }

    ConcurrentMap<String, String> getOnLinesResolve() {
        return this.onLinesResolve;
    }

    ConcurrentMap<String, String> getOffLines() {
        return this.offLines;
    }

    private NetListKeeper() {
    }

    /**
     @return {@link List} of {@link InetAddress}, из
     @throws IOException файловая система
     */
    List<InetAddress> onlinesAddressesList() throws IOException {
        String classMeth = "NetListKeeper.onlinesAddressesList";
        new MessageCons().info(classMeth, ConstantsFor.STR_RETURNS, "java.util.List<java.net.InetAddress>");
        List<InetAddress> onlineAddresses = new ArrayList<>();
        Deque<String> fileAsDeque = NetScanFileWorker.getI().getListOfOnlineDev();
        fileAsDeque.forEach(x -> {
            try {
                byte[] bytes = InetAddress.getByName(x.split(" ")[1]).getAddress();
                onlineAddresses.add(InetAddress.getByAddress(bytes));
            } catch (UnknownHostException e) {
                new MessageCons().errorAlert("NetListKeeper", "onlinesAddressesList", e.getMessage());
                FileSystemWorker.error(classMeth, e);
            }
        });
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

        return getOnLinesResolve().equals(that.getOnLinesResolve()) && getOffLines().equals(that.getOffLines());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetListKeeper{");
        sb.append("serialVersionUID=").append(serialVersionUID);
        sb.append(", NET_LIST_KEEPER=").append(NET_LIST_KEEPER.hashCode());
        sb.append(", LOGGER=").append(LOGGER.toString());
        sb.append(", onLinesResolve=").append(onLinesResolve);
        sb.append(", offLines=").append(offLines);
        sb.append('}');
        return sb.toString();
    }

}
