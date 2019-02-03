package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.net.NetScanCtr;
import ru.vachok.networker.net.enums.ConstantsNet;

import java.io.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 @since 22.09.2018 (13:36) */
@Component (ConstantsNet.STR_LASTNETSCAN)
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

    /**
     @param timeLastScan дата последнего скана
     @see NetScanCtr#netScan(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.ui.Model)
     */
    public void setTimeLastScan(Date timeLastScan) {
        this.timeLastScan = timeLastScan;
        new MessageCons().infoNoTitles("LastNetScan.setTimeLastScan\n" + timeLastScan.toString());
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
