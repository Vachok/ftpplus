package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.vachok.networker.ConstantsFor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 @since 22.09.2018 (13:36) */
@Component("lastnetscan")
@Scope (ConstantsFor.SINGLETON)
public class LastNetScan implements Serializable {

    private static final long serialVersionUID = 1984L;

    private static final transient Logger LOGGER = LoggerFactory.getLogger(LastNetScan.class.getName());

    private Date timeLastScan;

    private static LastNetScan lastNetScan = new LastNetScan();

    private ConcurrentMap<String, Boolean> netWork = new ConcurrentHashMap<>();

    private LastNetScan() {
        LOGGER.info(this.getClass().getSimpleName());
    }

    public static LastNetScan getLastNetScan() {
        return lastNetScan;
    }

    public Date getTimeLastScan() {
        return timeLastScan;
    }

    public void setTimeLastScan(Date timeLastScan) {
        this.timeLastScan = timeLastScan;
    }

    ConcurrentMap<String, Boolean> getNetWork() {
        return netWork;
    }

    public void setNetWork(ConcurrentMap<String, Boolean> netWork) {
        this.netWork = netWork;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
}
