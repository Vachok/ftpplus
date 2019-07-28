// Copyright (c) all rights. http://networker.vachok.ru 2019.
package ru.vachok.networker.net;


import org.jetbrains.annotations.Contract;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.Serializable;
import java.util.Date;
import java.util.StringJoiner;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 Файловые работы.
 <p>
 @see ru.vachok.networker.net.NetScanFileWorkerTest
 @since 25.12.2018 (10:43) */
public class NetScanFileWorker implements Serializable {
    
    
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static final NetScanFileWorker NET_SCAN_FILE_WORKER = new NetScanFileWorker();
    
    private static MessageToUser messageToUser = new MessageLocal(NetScanFileWorker.class.getSimpleName());
    
    private long lastStamp = System.currentTimeMillis();
    
    public long getLastStamp() {
        return lastStamp;
    }
    
    public void setLastStamp(long lastStamp) {
        this.lastStamp = lastStamp;
        Preferences pref = AppComponents.getUserPref();
        pref.put("NetScanFileWorker.lastStamp", new Date(lastStamp).toString());
        try {
            pref.sync();
        }
        catch (BackingStoreException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    @Contract(pure = true)
    public static NetScanFileWorker getI() {
        return NET_SCAN_FILE_WORKER;
    }
    
    public void setLastStamp(long millis, String address) {
        setLastStamp(millis);
        Preferences pref = AppComponents.getUserPref();
        pref.put("lastIP", address);
        try {
            pref.sync();
        }
        catch (BackingStoreException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", NetScanFileWorker.class.getSimpleName() + "[\n", "\n]")
            .add("lastStamp = " + lastStamp)
            .toString();
    }
    
}
