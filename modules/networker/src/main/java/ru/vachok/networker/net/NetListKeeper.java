// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.AccessListsCheckUniq;
import ru.vachok.networker.net.enums.OtherKnownDevices;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Deque;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
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
    
    private Map<String, String> inetUniqMap = new ConcurrentHashMap<>();
    
    private String nameOfExtObject = getClass().getSimpleName() + "onLinesResolve.map";
    
    private NetListKeeper() {
        AppComponents.threadConfig().getTaskScheduler().submitListenable(new ExitApp("on.map", this.onLinesResolve));
    }
    
    public Map<String, String> getInetUniqMap() {
        if (inetUniqMap.size() == 0) {
            new AccessListsCheckUniq().run();
        }
        return inetUniqMap;
    }
    
    public void setInetUniqMap(Map<String, String> inetUniqMap) {
        this.inetUniqMap = inetUniqMap;
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
    public static Map<InetAddress, String> getMapAddr() {
        Map<InetAddress, String> retDeq = new ConcurrentHashMap<>();
        Field[] fields = OtherKnownDevices.class.getFields();
        for (Field field : fields) {
            try {
                if (field.getName().contains("IP")) {
                    byte[] inetAddressBytes = InetAddress.getByName(field.get(field).toString()).getAddress();
                    retDeq.putIfAbsent(InetAddress.getByAddress(inetAddressBytes), field.getName());
                }
                else {
                    retDeq.putIfAbsent(InetAddress.getByName(field.get(field).toString()), field.getName());
                }
            }
            catch (IOException | IllegalAccessException e) {
                messageToUser.error(e.getMessage());
            }
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
    
    private class ChkOnlinesSizeChange implements Runnable {
    
    
        private int currentSize = onLinesResolve.size();
        
        private int wasSize;
        
        public ChkOnlinesSizeChange() {
            Properties properties = AppComponents.getProps();
            this.wasSize = Integer.parseInt(properties.getProperty("onsize", "0"));
        }
        
        @Override
        public void run() {
            AppComponents.threadConfig().thrNameSet(String.valueOf(new File(nameOfExtObject).exists()) + nameOfExtObject.substring(0, 3));
            if (wasSize < currentSize) {
                boolean ownObject = new ExitApp(nameOfExtObject, onLinesResolve).writeOwnObject();
            }
            else {
                readMap();
            }
        }
    }
}
