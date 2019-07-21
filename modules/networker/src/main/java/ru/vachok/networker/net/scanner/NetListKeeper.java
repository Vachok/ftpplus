// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.Keeper;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.net.AccessListsCheckUniq;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.net.enums.OtherKnownDevices;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.prefs.Preferences;


/**
 Создание списков адресов, на все случаи жизни
 <p>
 
 @see ru.vachok.networker.net.scanner.NetListKeeperTest
 @since 30.01.2019 (17:02) */
public class NetListKeeper implements Keeper {
    
    
    /**
     {@link MessageLocal}
     */
    private static final MessageToUser messageToUser = new MessageLocal(NetListKeeper.class.getSimpleName());
    
    private final ThreadConfig threadConfig = AppComponents.threadConfig();
    
    private static NetListKeeper netListKeeper = new NetListKeeper();
    
    private ConcurrentMap<String, String> onLinesResolve = new ConcurrentHashMap<>();
    
    private Map<String, String> offLines = new ConcurrentHashMap<>();
    
    private Map<String, String> inetUniqMap = new ConcurrentHashMap<>();
    
    @Override
    public List<String> getCurrentScanLists() {
        return DiapazonScan.getCurrentPingStats();
    }
    
    private String nameOfExtObject = getClass().getSimpleName() + ConstantsFor.FILENALE_ONLINERES;
    
    private NetListKeeper() {
    }
    
    public Map<String, String> getOffLines() {
        return Collections.unmodifiableMap(offLines);
    }
    
    public void setOffLines(Map<String, String> offLines) {
        this.offLines = offLines;
    }
    
    public Map<String, String> editOffLines() {
        return offLines;
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
    
    @Contract(pure = true)
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
                String hostFromField = field.get(field).toString();
                if (field.getName().contains("IP")) {
                    byte[] inetAddressBytes = InetAddress.getByName(hostFromField).getAddress();
                    InetAddress addressResolved = InetAddress.getByAddress(inetAddressBytes);
                    String putToMap = retDeq.put(addressResolved, field.getName());
                    System.out.println("putToMap = " + putToMap);
                }
                else {
                    retDeq.putIfAbsent(InetAddress.getByName(hostFromField), field.getName());
                }
            }
            catch (IOException | IllegalAccessException e) {
                messageToUser.error(e.getMessage());
            }
        }
        
        return retDeq;
    }
    
    public ConcurrentMap<String, String> getOnLinesResolve() {
        Runnable onSizeChecker = new NetListKeeper.ChkOnlinePCsSizeChange();
    
        try {
            threadConfig.getTaskScheduler().scheduleAtFixedRate(onSizeChecker, TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        }
        catch (RejectedExecutionException e) {
            messageToUser.error(MessageFormat.format("NetListKeeper.getOnLinesResolve threw away: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return this.onLinesResolve;
    }
    
    @Override
    public Deque<InetAddress> getOnlineDevicesInetAddress() {
        return new NetScanFileWorker().getOnlineDevicesInetAddress();
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
    
    
        protected static final String RESOLVE = "onLinesResolve";
    
        private Preferences userPref = AppComponents.getUserPref();
        
        private int currentSize = onLinesResolve.size();
        
        private int wasSize;
        
        ChkOnlinePCsSizeChange() {
            this.wasSize = Integer.parseInt(userPref.get(RESOLVE, "0"));
        }
        
        @Override
        public void run() {
            if (wasSize < currentSize) {
                boolean ownObject = new ExitApp(nameOfExtObject, onLinesResolve).writeOwnObject();
                userPref.put(RESOLVE, String.valueOf(onLinesResolve.size()));
            }
            else {
                readMap();
            }
        }
    }
}
