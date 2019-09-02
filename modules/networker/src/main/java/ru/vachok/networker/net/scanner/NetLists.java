// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.data.Keeper;
import ru.vachok.networker.componentsrepo.data.enums.*;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;
import java.util.prefs.Preferences;


/**
 @see ru.vachok.networker.net.scanner.NetListsTest */
public class NetLists implements Keeper {
    
    
    /**
     {@link MessageLocal}
     */
    private static final MessageToUser messageToUser = ru.vachok.networker.restapi.MessageToUser
        .getInstance(ru.vachok.networker.restapi.MessageToUser.LOCAL_CONSOLE, NetLists.class.getSimpleName());
    
    private final ThreadConfig threadConfig = AppComponents.threadConfig();
    
    private static NetLists netLists = new NetLists();
    
    private ConcurrentMap<String, String> onLinesResolve = new ConcurrentHashMap<>();
    
    private Map<String, String> offLines = new ConcurrentHashMap<>();
    
    private String nameOfExtObject = getClass().getSimpleName() + FileNames.FILENALE_ONLINERES;
    
    private NetLists() {
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
    
    @Contract(pure = true)
    public static NetLists getI() {
        return netLists;
    }
    
    public static @NotNull Map<InetAddress, String> getMapAddr() {
        Map<InetAddress, String> retDeq = new ConcurrentHashMap<>();
        Field[] fields = OtherKnownDevices.class.getFields();
        for (Field field : fields) {
            try {
                String hostFromField = field.get(field).toString();
                if (field.getName().contains("IP")) {
                    byte[] inetAddressBytes = InetAddress.getByName(hostFromField).getAddress();
                    InetAddress addressResolved = InetAddress.getByAddress(inetAddressBytes);
                    retDeq.putIfAbsent(addressResolved, field.getName());
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
        Runnable onSizeChecker = new NetLists.ChkOnlinePCsSizeChange();
        
        try {
            threadConfig.getTaskScheduler().scheduleAtFixedRate(onSizeChecker, TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        }
        catch (RejectedExecutionException e) {
            messageToUser.error(MessageFormat.format("NetListKeeper.getOnLinesResolve threw away: {0}, ({1})", e.getMessage(), e.getClass().getName()));
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
                try (OutputStream outputStream = new FileOutputStream(nameOfExtObject)) {
                    new ExitApp(onLinesResolve).writeExternal(new ObjectOutputStream(outputStream));
                }
                catch (IOException e) {
                    messageToUser.error(e.getMessage() + " see line: 144");
                }
                userPref.put(RESOLVE, String.valueOf(onLinesResolve.size()));
            }
            else {
                readMap();
            }
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ChkOnlinePCsSizeChange{");
            sb.append(", currentSize=").append(currentSize);
            sb.append(", wasSize=").append(wasSize);
            sb.append('}');
            return sb.toString();
        }
    }
}
