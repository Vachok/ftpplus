// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.TForms;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.net.AccessListsCheckUniq;
import ru.vachok.networker.net.enums.OtherKnownDevices;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.prefs.Preferences;


/**
 Создание списков адресов, на все случаи жизни
 <p>
 @see ru.vachok.networker.net.scanner.NetListKeeperTest
 @since 30.01.2019 (17:02) */
public class NetListKeeper {
    
    
    /**
     {@link MessageLocal}
     */
    private static final MessageToUser messageToUser = new MessageLocal(NetListKeeper.class.getSimpleName());
    
    private final ThreadConfig threadConfig = AppComponents.threadConfig();
    
    private static NetListKeeper netListKeeper = new NetListKeeper();
    
    private ConcurrentMap<String, String> onLinesResolve = new ConcurrentHashMap<>();
    
    private Map<String, String> offLines = new ConcurrentHashMap<>();
    
    private Map<String, String> inetUniqMap = new ConcurrentHashMap<>();
    
    private String nameOfExtObject = getClass().getSimpleName() + "onLinesResolve.map";
    
    private NetListKeeper() {
    }
    
    public Map<String, String> getOffLines() {
        Map<String, String> offLinesRO = offLines;
        return Collections.unmodifiableMap(offLinesRO);
    }
    
    public void setOffLines(Map<String, String> offLines) {
        this.offLines = offLines;
    }
    
    public Map<String, String> editOffLines() {
        return this.offLines;
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
    
    public ConcurrentMap<String, String> getOnLinesResolve() {
        readMap();
        try {
            threadConfig.getTaskScheduler().scheduleAtFixedRate(new ChkOnlinePCsSizeChange(), TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        }
        catch (RejectedExecutionException e) {
            messageToUser.error(MessageFormat.format("NetListKeeper.getOnLinesResolve threw away: {0}, ({1})", e.getMessage(), e.getLocalizedMessage()));
        }
        return this.onLinesResolve;
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
    
    void checkSwitchesAvail() {
        SwitchesAvailability switchesAvailability = new SwitchesAvailability();
        Future<?> submit = threadConfig.getTaskExecutor().submit(switchesAvailability);
        try {
            submit.get(ConstantsFor.DELAY * 2, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        Set<String> availabilityOkIP = switchesAvailability.getOkIP();
        availabilityOkIP.forEach(x->onLinesResolve.put(x, LocalDateTime.now().toString()));
    }
    
    private void readMap() {
        try (InputStream inputStream = new FileInputStream(nameOfExtObject);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
        ) {
            Map<String, String> fromFileMap = (Map<String, String>) objectInputStream.readObject();
            onLinesResolve.putAll(fromFileMap);
        }
        catch (IOException | ClassNotFoundException ignore) {
            //
        }
    }
    
    private class ChkOnlinePCsSizeChange implements Runnable {
    
    
        private Preferences userPref = AppComponents.getUserPref();
        
        private int currentSize = onLinesResolve.size();
        
        private int wasSize;
        
        ChkOnlinePCsSizeChange() {
            this.wasSize = Integer.parseInt(userPref.get("onLinesResolve", "0"));
        }
        
        @Override
        public void run() {
            threadConfig.thrNameSet(String.valueOf(new File(nameOfExtObject).exists()) + nameOfExtObject.substring(0, 3));
            if (wasSize < currentSize) {
                boolean ownObject = new ExitApp(nameOfExtObject, onLinesResolve).writeOwnObject();
                userPref.put("onLinesResolve", String.valueOf(onLinesResolve.size()));
            }
            else {
                readMap();
            }
        }
    }
}
