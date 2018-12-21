package ru.vachok.networker.net;


import org.slf4j.Logger;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 Скан диапазона адресов

 @since 19.12.2018 (11:35) */
public class DiapazonedScan implements Runnable {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    private static volatile DiapazonedScan ourInstance;

    public static DiapazonedScan getInstance() {
        if(ourInstance==null){
            synchronized(DiapazonedScan.class) {
                if(ourInstance==null){
                    ourInstance = new DiapazonedScan();

                }
            }

        }
        return ourInstance;
    }

    /*Instances*/
    private DiapazonedScan() {
    }

    @Override
    public void run() {
        LOGGER.warn("DiapazonedScan.run");
        try{
            scanAll();
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     Добавляет в {@link ConstantsFor#ALL_DEVICES} адреса 10.200.200-217.254
     <p>

     @throws IOException если адрес недоступен.
     */
    @SuppressWarnings ("all")
    private void scanAll() throws IOException {
        long stArt = System.currentTimeMillis();
        LOGGER.info("DiapazonedScan.scanAll");
        String avaPathStr = Paths.get(".").toFile().getCanonicalPath();
        Path logPath = Paths.get(avaPathStr + "\\modules\\networker\\src\\main\\resources\\static\\texts\\available_last.txt");
        if(!logPath.toFile().isFile()){
            logPath = Paths.get("available_last.txt");
        }
        try(OutputStream outputStream = new FileOutputStream(logPath.toFile());
            PrintWriter printWriter = new PrintWriter(outputStream, true);){
            for(int i = 200; i < 218; i++){
                StringBuilder msgBuild = new StringBuilder();
                for(int j = 0; j < 255; j++){
                    msgBuild = new StringBuilder();
                    byte[] aBytes = InetAddress.getByName("10.200." + i + "." + j).getAddress();
                    InetAddress byAddress = InetAddress.getByAddress(aBytes);
                    int t = 100;
                    if(ConstantsFor.thisPC().toLowerCase().contains("home")){
                        t = 400;
                    }
                    if(byAddress.isReachable(t)){
                        printWriter.println(byAddress.getHostName() + " " + byAddress.getHostAddress());
                        ConstantsFor.ALL_DEVICES.add("<font color=\"green\">" + byAddress.toString() + "</font><br>");
                    }
                    else{
                        ConstantsFor.ALL_DEVICES.add("<font color=\"red\">" + byAddress.toString() + "</font><br>");
                    }
                    msgBuild.append("IP was ").append(" 10.200.").append(i).append("<-i.j->").append(j).append("\n")
                        .append(j).append(" was j\n");
                    String msg = msgBuild.toString();
                    LOGGER.info(msg);
                }
                msgBuild
                    .append(i).append(" was i. Total time: ")
                    .append(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - stArt)).append("min\n")
                    .append(ConstantsFor.ALL_DEVICES.size() + " ALL_DEVICES.size()");
                String msg = msgBuild.toString();
                LOGGER.warn(msg);
                printWriter.println(msg);
            }
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
        String msg = "\nTime spend: " + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - stArt);
        LOGGER.warn(msg);
    }

    @Override
    public String toString() {
        LOGGER.info("DiapazonedScan.toString");
        final StringBuilder sb = new StringBuilder("DiapazonedScan{");
        sb.append("<a href=\"/showalldev\">ALL_DEVICES=").append(ConstantsFor.ALL_DEVICES.size()).append("/4591");
        sb.append("</a>}");
        return sb.toString();
    }

    List<String> pingSwitch() throws IllegalAccessException {
        LOGGER.info("DiapazonedScan.pingSwitch");
        StringBuilder stringBuilder = new StringBuilder();
        Field[] swFields = Switches.class.getFields();
        List<String> swList = new ArrayList<>();
        for(Field swF : swFields){
            swList.add("\n" + swF.get(swF).toString());
        }
        swList.forEach(stringBuilder::append);
        String retMe = stringBuilder.toString();
        LOGGER.warn(retMe);
        return swList;
    }

}
