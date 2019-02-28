package ru.vachok.networker.net;


import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
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

    private static NetListKeeper netListKeeper = new NetListKeeper();

    /**
     {@link MessageLocal}
     */
    private static final MessageLocal LOGGER = new MessageLocal();

    private ConcurrentMap<String, String> onLinesResolve = new ConcurrentHashMap<>();

    private ConcurrentMap<String, String> offLines = new ConcurrentHashMap<>();

    public static NetListKeeper getI() {
        try (InputStream inputStream = new FileInputStream(NetListKeeper.class.getSimpleName() + ".ser");
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            Object readObject = objectInputStream.readObject();
            return (NetListKeeper) readObject;
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.errorAlert("NetListKeeper", "getI", e.getMessage());
            FileSystemWorker.error("NetListKeeper.getI", e);
            return netListKeeper;
        }
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
        List<InetAddress> onlineAddresses = new ArrayList<>();
        Deque<String> fileAsDeque = NetScanFileWorker.getI().getListOfOnlineDev();
        fileAsDeque.forEach(x -> {
            try {
                byte[] bytes = InetAddress.getByName(x.split(" ")[1]).getAddress();
                onlineAddresses.add(InetAddress.getByAddress(bytes));
            } catch (UnknownHostException e) {
                LOGGER.errorAlert("NetListKeeper", "onlinesAddressesList", e.getMessage());
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
        sb.append(", netListKeeper=").append(netListKeeper.hashCode());
        sb.append(", LOGGER=").append(LOGGER.toString());
        sb.append(", onLinesResolve=").append(onLinesResolve);
        sb.append(", offLines=").append(offLines);
        sb.append('}');
        return sb.toString();
    }

}
