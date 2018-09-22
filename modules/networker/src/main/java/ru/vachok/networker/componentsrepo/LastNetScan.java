package ru.vachok.networker.componentsrepo;


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

    private Date timeLastScan;

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
        String s = readObject();
        if(s.equalsIgnoreCase("no object")){
            netWork.put(s, true);
        }
    }

    private String readObject() {
        String s;
        try(FileInputStream inputStream = new FileInputStream(objFile);){
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            Object o = objectInputStream.readObject();
            s = o.toString();
            return s;
        }
        catch(IOException | ClassNotFoundException e){
            return e.getMessage();
        }
    }

    public String writeObject() {
        try(OutputStream outputStream = new FileOutputStream(objFile)){
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(this);
        }
        catch(IOException e){
            return e.getMessage();
        }
        return objFile.getAbsolutePath();
    }
}