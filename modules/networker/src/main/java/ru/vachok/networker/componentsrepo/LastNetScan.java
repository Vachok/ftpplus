// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.Keeper;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 @see ru.vachok.networker.componentsrepo.LastNetScanTest
 @since 22.09.2018 (13:36) */
@Component(ConstantsNet.BEANNAME_LASTNETSCAN)
@Scope(ConstantsFor.SINGLETON)
public class LastNetScan implements Keeper, Serializable {
    
    
    private static final long serialVersionUID = 1984L;
    
    private static final MessageToUser MESSAGE_TO_USER = new MessageLocal(LastNetScan.class.getSimpleName());
    
    private Lock resourceLock = new ReentrantLock();
    
    private static LastNetScan lastNetScan = new LastNetScan();
    
    private Date timeLastScan = new Date();
    
    private ConcurrentNavigableMap<String, Boolean> netWork = new ConcurrentSkipListMap<>();
    
    private LastNetScan() {
        MESSAGE_TO_USER.info(this.getClass().getSimpleName());
    }
    
    public static LastNetScan getLastNetScan() {
        return lastNetScan;
    }
    
    public Date getTimeLastScan() {
        return timeLastScan;
    }
    
    /**
     @param timeLastScan дата последнего скана
     */
    public void setTimeLastScan(Date timeLastScan) {
        this.timeLastScan = timeLastScan;
        MESSAGE_TO_USER.warn(MessageFormat.format("TimeLastScan {0} is set", timeLastScan.toString()));
    }
    
    public ConcurrentNavigableMap<String, Boolean> getNetWork() {
        return netWork;
    }
    
    public void setNetWork(ConcurrentNavigableMap<String, Boolean> netWork) {
        this.netWork = netWork;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LastNetScan{");
        sb.append("netWork=").append(netWork);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append(", timeLastScan=").append(timeLastScan);
        sb.append('}');
        return sb.toString();
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
}
