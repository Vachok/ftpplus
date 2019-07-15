// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.net.enums.ConstantsNet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 @since 22.09.2018 (13:36) */
@Component(ConstantsNet.BEANNAME_LASTNETSCAN)
@Scope(ConstantsFor.SINGLETON)
public class LastNetScan implements Serializable {

    private static final long serialVersionUID = 1984L;

    private static final transient Logger LOGGER = LoggerFactory.getLogger(LastNetScan.class.getName());

    private Date timeLastScan = new Date();

    private static LastNetScan lastNetScan = new LastNetScan();
    
    private ConcurrentNavigableMap<String, Boolean> netWork = new ConcurrentSkipListMap<>();

    private LastNetScan() {
        LOGGER.info(this.getClass().getSimpleName());
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
        new MessageCons(getClass().getSimpleName()).infoNoTitles("LastNetScan.setTimeLastScan\n" + timeLastScan);
    }
    
    public ConcurrentNavigableMap<String, Boolean> getNetWork() {
        return netWork;
    }
    
    public void setNetWork(ConcurrentNavigableMap<String, Boolean> netWork) {
        this.netWork = netWork;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
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
}
