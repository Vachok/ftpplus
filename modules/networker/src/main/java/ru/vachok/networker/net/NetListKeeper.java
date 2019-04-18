package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.OtherKnownDevices;
import ru.vachok.networker.services.MessageLocal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 Создание списков адресов, на все случаи жизни
 <p>
 
 @since 30.01.2019 (17:02) */
@SuppressWarnings("StaticMethodOnlyUsedInOneClass") public class NetListKeeper {
    
    
    /**
     {@link MessageLocal}
     */
    private static final MessageToUser messageToUser = new MessageLocal(NetListKeeper.class.getSimpleName());
    
    private static NetListKeeper netListKeeper = new NetListKeeper();
    
    private static String ptvTime;
    
    private ConcurrentMap<String, String> onLinesResolve = new ConcurrentHashMap<>();
    
    private ConcurrentMap<String, String> offLines = new ConcurrentHashMap<>();
    
    private String nameOfExtObject = getClass().getSimpleName() + "onLinesResolve.map";
    
    private NetListKeeper() {
        AppComponents.threadConfig().getTaskScheduler().submitListenable(new ExitApp("on.map", this.onLinesResolve));
    }
    
    public static NetListKeeper getI() {
        return netListKeeper;
    }
    
    public static String getPtvTime() {
        return ptvTime;
    }
    
    public static void setPtvTime(String ptvTime) {
        NetListKeeper.ptvTime = ptvTime;
    }
    
    public ConcurrentMap<String, String> getOnLinesResolve() {
        readMap();
        AppComponents.threadConfig().getTaskScheduler().scheduleAtFixedRate(new ChkOnlinesSizeChange(), TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        return this.onLinesResolve;
    }
    
    /**
     ИП-адреса, которые проверяются в момент входа на <a href="http://rups00.eatmeat.ru:8880/ping" target=_blank>http://rups00.eatmeat.ru:8880/ping</a>
     
     @return {@link Deque} {@link InetAddress}
     */
    public static Deque<InetAddress> getDeqAddr() {
        Deque<InetAddress> retDeq = new ConcurrentLinkedDeque<>();
        Field[] fields = OtherKnownDevices.class.getFields();
        try {
            for (Field field : fields) {
                if (field.getName().contains("IP")) {
                    byte[] inetAddressBytes = InetAddress.getByName(field.get(field).toString()).getAddress();
                    retDeq.add(InetAddress.getByAddress(inetAddressBytes));
                }
                else {
                    retDeq.add(InetAddress.getByName(field.get(field).toString()));
                }
            }
        }
        catch (IOException | IllegalAccessException e) {
            messageToUser.error(FileSystemWorker.error(NetListKeeper.class.getSimpleName() + ".getDeqAddr", e));
        }
        return retDeq;
    }
    
    @Override
    public int hashCode() {
        int result = getOnLinesResolve().hashCode();
        result = 31 * result + getOffLines().hashCode();
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NetListKeeper)) {
            return false;
        }
        
        NetListKeeper that = (NetListKeeper) o;
        
        return getOnLinesResolve().equals(that.getOnLinesResolve()) && getOffLines().equals(that.getOffLines());
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
    
    void readMap() {
    
        try (InputStream inputStream = new FileInputStream(nameOfExtObject);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
        ) {
            Map<String, String> fromFileMap = (ConcurrentMap<String, String>) objectInputStream.readObject();
            onLinesResolve.putAll(fromFileMap);
        }
        catch (IOException | ClassNotFoundException ignore) {
            //
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
    
        while (!fileAsDeque.isEmpty()) {
            byte[] bytes = InetAddress.getByName(fileAsDeque.poll().split(" ")[1]).getAddress();
            onlineAddresses.add(InetAddress.getByAddress(bytes));
        }
        
        return onlineAddresses;
    }
    
    
    private class ChkOnlinesSizeChange implements Runnable {
    
    
        private int currentSize = onLinesResolve.size();
        
        private int wasSize;
        
        public ChkOnlinesSizeChange() {
            Properties properties = AppComponents.getOrSetProps();
            this.wasSize = Integer.parseInt(properties.getProperty("onsize", "0"));
        }
        
        @Override
        public void run() {
            AppComponents.threadConfig().thrNameSet(getClass().getSimpleName());
            if (wasSize < currentSize) {
                boolean ownObject = new ExitApp(nameOfExtObject, onLinesResolve).writeOwnObject();
            }
            else {
                readMap();
            }
        }
    }
}
