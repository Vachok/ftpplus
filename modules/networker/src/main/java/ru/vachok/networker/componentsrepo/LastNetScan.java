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

    /*Fields*/
    private static final long serialVersionUID = 1984L;

    private static final transient Logger LOGGER = LoggerFactory.getLogger(LastNetScan.class.getName());

    private Date timeLastScan;

    private transient Object o;

    private Map<String, Boolean> netWork = new ConcurrentHashMap<>();

    private transient ObjectInputStream in;

    private transient ObjectOutputStream out;

    private LastNetScan lastNetScan;

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
        writeObject(out);
    }

    public LastNetScan getLastNetScan() {
        this.lastNetScan = new LastNetScan();
        try{
            readObject(in);
        }
        catch(ClassNotFoundException e){
            LOGGER.error(e.getMessage(), e);
            return new LastNetScan();
        }
        return ( LastNetScan ) o;
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException {
        this.in = in;
        try(InputStream inputStream = new FileInputStream(ConstantsFor.LASTNETSCAN_FILE)){
            in = new ObjectInputStream(inputStream);
            this.o = in.readObject();
        }
        catch(IOException e){
            LoggerFactory.getLogger(MailMessage.class.getSimpleName());
        }
    }

    /*Instances*/
    public boolean saveState() {
        try{
            writeObject(out);
        }
        catch(Exception e){
            LOGGER.error(e.getMessage(), e);
        }
        File file = new File(ConstantsFor.LASTNETSCAN_FILE);
        return file.canRead() && file.isFile() && file.lastModified() < System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1);
    }

    private void writeObject(ObjectOutputStream out) {
        this.out = out;
        try(OutputStream outputStream = new FileOutputStream(ConstantsFor.LASTNETSCAN_FILE)){
            this.out = new ObjectOutputStream(outputStream);
            out.writeObject(this);
        }
        catch(Exception e){
            LOGGER.error(e.getMessage(), e);
        }
    }
}