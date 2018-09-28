package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.vachok.networker.ConstantsFor;

import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 @since 22.09.2018 (13:36) */
@Component ("lastnetscan")
@Scope ("singleton")
public class LastNetScan implements Serializable {

    private static final long serialVersionUID = 1984L;

    /*Fields*/
    private static final transient Logger LOGGER = LoggerFactory.getLogger(LastNetScan.class.getName());

    private Date timeLastScan;

    private transient Object o;

    private Map<String, Boolean> netWork = new ConcurrentHashMap<>();

    private transient ObjectInputStream in;

    private transient ObjectOutputStream out;

    public Date getTimeLastScan() {
        return timeLastScan;
    }

    public void setTimeLastScan(Date timeLastScan) {
        this.timeLastScan = timeLastScan;
    }

    public Map<String, Boolean> getNetWork() {
        return this.netWork;
    }

    public void setNetWork(Map<String, Boolean> netWork) {
        this.netWork = netWork;
        writeObject(out);
    }

    private void writeObject(ObjectOutputStream out) {
        this.out = out;
        try (OutputStream outputStream = new FileOutputStream(ConstantsFor.LASTNETSCAN_FILE)) {
            this.out = new ObjectOutputStream(outputStream);
            this.out.writeObject(this);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /*Instances*/
    public LastNetScan() {
        LOGGER.info("Serial. Understand 25.09.2018 (20:45)");
    }

    private void readObject(ObjectInputStream in) {
        try{
            in.defaultReadObject();
        }
        catch(IOException | ClassNotFoundException e){
            LOGGER.error(e.getMessage(), e);
            return new LastNetScan();
        }
        return (LastNetScan) o;
    }

    private void writeObject(ObjectOutputStream out) {
        try{
            out.defaultWriteObject();
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    public boolean saveState() {
        writeObject(out);
        File file = new File(ConstantsFor.LASTNETSCAN_FILE);
        return file.canRead() && file.isFile() && file.lastModified() < System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1);
    }
}