package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 Создание списков адресов, на все случаи жизни
 <p>

 @since 30.01.2019 (17:02) */
public class NetListKeeper {


    /**
     {@link MessageLocal}
     */
    private static final MessageToUser messageToUser = new MessageLocal(NetListKeeper.class.getSimpleName());

    private static NetListKeeper netListKeeper = new NetListKeeper();

    private ConcurrentMap<String, String> onLinesResolve = new ConcurrentHashMap<>();

    private ConcurrentMap<String, String> offLines = new ConcurrentHashMap<>();

    private NetListKeeper() {
        AppComponents.threadConfig().getTaskScheduler().submitListenable(new ExitApp("on.map", this.onLinesResolve));
    }

    public static NetListKeeper getI() {
        return netListKeeper;
    }

    public ConcurrentMap<String, String> getOnLinesResolve() {
        readMap();
        AppComponents.threadConfig().getTaskScheduler().scheduleAtFixedRate(new ChkOnlinesSizeChange(), TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        return this.onLinesResolve;
    }

    void readMap() {
        try (InputStream inputStream = new FileInputStream("on.map");
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            Map<String, String> fromFileMap = (ConcurrentMap<String, String>) objectInputStream.readObject();
            onLinesResolve.putAll(fromFileMap);
        } catch (IOException | ClassNotFoundException e) {
            FileSystemWorker.error("NetListKeeper.readMap", e);
        }
    }

    ConcurrentMap<String, String> getOffLines() {
        return this.offLines;
    }

    /**
     @return {@link List} of {@link InetAddress}, из
     @throws IOException файловая система
     */
    List<InetAddress> onlinesAddressesList() throws IOException {
        AppComponents.threadConfig().thrNameSet("LsIP");
        String classMeth = "NetListKeeper.onlinesAddressesList";
        List<InetAddress> onlineAddresses = new ArrayList<>();
        Deque<String> fileAsDeque = NetScanFileWorker.getI().getListOfOnlineDev();
        fileAsDeque.forEach(x -> {
            try {
                byte[] bytes = InetAddress.getByName(x.split(" ")[1]).getAddress();
                onlineAddresses.add(InetAddress.getByAddress(bytes));
            } catch (UnknownHostException e) {
                messageToUser.errorAlert("NetListKeeper", "onlinesAddressesList", e.getMessage());
                FileSystemWorker.error(classMeth, e);
            }
        });
        messageToUser.info(classMeth, "returning: " + onlineAddresses.size(), " onlineAddresses");
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

    private class ChkOnlinesSizeChange implements Runnable {


        private Properties properties = AppComponents.getOrSetProps();

        private int currentSize = onLinesResolve.size();

        private int wasSize;

        public ChkOnlinesSizeChange() {
            this.wasSize = Integer.parseInt(properties.getProperty("onsize", "0"));
        }

        @Override
        public void run() {
            AppComponents.threadConfig().thrNameSet(getClass().getSimpleName());
            final String classMeth = "ChkOnlinesSizeChange.run";
            messageToUser.info(classMeth, "wasSize", " = " + wasSize);
            messageToUser.info(classMeth, "currentSize", " = " + currentSize);
            if (wasSize < currentSize) {
                boolean ownObject = new ExitApp("on.map", onLinesResolve).writeOwnObject();
                messageToUser.info("ChkOnlinesSizeChange.call", "ownObject", " = " + ownObject);
                properties.setProperty("onsize", String.valueOf(currentSize));
            } else {
                readMap();
                messageToUser.info(classMeth, "currentSize", " = " + currentSize);
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetListKeeper{");
        sb.append("offLines=").append(new TForms().fromArray(offLines, true));
        sb.append("<br><br>");
        sb.append(", onLinesResolve=").append(new TForms().fromArray(onLinesResolve, false));
        sb.append('}');
        return sb.toString();
    }
}
