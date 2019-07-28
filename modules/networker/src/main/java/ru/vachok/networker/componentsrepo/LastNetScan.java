// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 @see ru.vachok.networker.componentsrepo.LastNetScanTest
 @since 22.09.2018 (13:36) */
@Component(ConstantsNet.BEANNAME_LASTNETSCAN)
@Scope(ConstantsFor.SINGLETON)
public class LastNetScan implements Serializable {
    
    
    private static final long serialVersionUID = 1984L;
    
    private static final MessageToUser MESSAGE_TO_USER = new MessageLocal(LastNetScan.class.getSimpleName());
    
    private Lock resourceLock = new ReentrantLock();
    
    private static LastNetScan lastNetScan = new LastNetScan();
    
    private Date timeLastScan = new Date();
    
    private LastNetScan() {
        MESSAGE_TO_USER.info(this.getClass().getSimpleName());
    }
    
    @Contract(pure = true)
    public static LastNetScan getLastNetScan() {
        return lastNetScan;
    }
    
    public Date getTimeLastScan() {
        return timeLastScan;
    }
    
    /**
     @param timeLastScan дата последнего скана
     */
    public void setTimeLastScan(@NotNull Date timeLastScan) {
        this.timeLastScan = timeLastScan;
        MESSAGE_TO_USER.warn(MessageFormat.format("TimeLastScan {0} is set", timeLastScan.toString()));
    }
    
    public ConcurrentNavigableMap<String, Boolean> getNetWork() {
        return NetKeeper.getNetwork();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LastNetScan{");
        sb.append("NET_WORK=").append(NetKeeper.getNetwork().size());
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append(", timeLastScan=").append(timeLastScan);
        sb.append('}');
        return sb.toString();
    }
    
    private void readObject(@NotNull ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
    
    private void writeObject(@NotNull ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
}
