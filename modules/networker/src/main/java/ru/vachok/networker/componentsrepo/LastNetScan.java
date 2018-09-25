package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 @since 22.09.2018 (13:36) */
@Component ("lastnetscan")
@Scope ("singleton")
public class LastNetScan implements Serializable {

    private static File objFile = new File(LastNetScan.class.getSimpleName() + ".obj");

    private static final long serialVersionUID = 1984L;

    private Date timeLastScan;

    private static final Logger LOGGER = LoggerFactory.getLogger(LastNetScan.class.getName());

    private Map<String, Boolean> netWork = new ConcurrentHashMap<>();

    public Date getTimeLastScan() {
        return timeLastScan;
    }

    public void setTimeLastScan(Date timeLastScan) {
        this.timeLastScan = timeLastScan;
    }

    public Map<String, Boolean> getNetWork() {
        return netWork;
    }

    public void setNetWork(Map<String, Boolean> netWork) {
        this.netWork = netWork;
    }

    /*Instances*/
    public LastNetScan() {
    }

    private void readObject(ObjectInputStream in) {
        try {
            in.defaultReadObject();
        }
        catch(IOException | ClassNotFoundException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void writeObject(ObjectOutputStream out) {
        try {
            out.defaultWriteObject();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}