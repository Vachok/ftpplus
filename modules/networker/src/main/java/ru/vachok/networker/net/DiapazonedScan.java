package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.TimeChecker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.event.ActionEvent;
import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

import static ru.vachok.networker.componentsrepo.AppComponents.getLogger;


/**
 Скан диапазона адресов

 @since 19.12.2018 (11:35) */
@SuppressWarnings({"FieldNotUsedInToString", "DoubleCheckedLocking"})
public class DiapazonedScan implements Runnable, Externalizable {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = getLogger();

    /**
     Повторения.
     */
    private static final String FONT_BR_STR = "</font><br>";

    private static Map<String, String> nameAddr = new HashMap<>();

    static Map<String, String> getNameAddr() {
        LOGGER.warn("DiapazonedScan.getNameAddr");
        return nameAddr;
    }

    /**
     Корень директории.
     */
    private static final String ROOT_PATH_STR = Paths.get(".").toAbsolutePath().toString();

    /**
     {@link NetScanFileWorker#getI()}
     */
    private static final NetScanFileWorker NET_SCAN_FILE_WORKER = NetScanFileWorker.getI();

    private static final int MAX_IN_VLAN = 255;

    /**
     Singleton inst
     */
    private static volatile DiapazonedScan ourInstance = null;

    private BlockingDeque<String> allDevices = ConstantsFor.ALL_DEVICES;

    /**
     Время файлов

     @see #scanNew()
     @see #scanOldLan(long)
     */
    private String fileTimes = "";

    private long stopClass = System.currentTimeMillis();

    public long getStopClass() {
        return stopClass;
    }

    /**
     SINGLETON

     @return single.
     */
    public static DiapazonedScan getInstance() {
        if (ourInstance == null) {
            synchronized (DiapazonedScan.class) {
                if (ourInstance == null) {
                    ourInstance = new DiapazonedScan();
                }
            }
        }
        return ourInstance;
    }

    /**
     @return {@link #NET_SCAN_FILE_WORKER}
     */
    static NetScanFileWorker getNetScanFileWorker() {
        return NET_SCAN_FILE_WORKER;
    }

    /**
     Добавляет в {@link ConstantsFor#ALL_DEVICES} адреса <i>10.200.200-217.254</i>
     */
    @SuppressWarnings("all")
    private void scanNew() {
        final long stArt = System.currentTimeMillis();
        Path p = Paths.get(ROOT_PATH_STR + "\\lan\\200_" + System.currentTimeMillis() / 1000 + ".scan");
        String msg1 = "DiapazonedScan.scanNew " + p.toAbsolutePath().toString();
        LOGGER.warn(msg1);
        File newLanFile = new File(ConstantsFor.AVAILABLE_LAST_TXT);
        try (OutputStream outputStream = new FileOutputStream(newLanFile);
             PrintWriter printWriter = new PrintWriter(outputStream, true)) {
            if(allDevices.remainingCapacity()==0){
                MessageToUser messageToUser = new MessageCons();
                messageToUser.infoNoTitles(new TForms().fromArray(allDevices));
                allDevices.clear();
                scanLan(printWriter, 200, 218, stArt, "10.200.");
            } else {
                scanLan(printWriter, 200, 218, stArt, "10.200.");
            }
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            String msg = "Vlans 200-217 completed.\nTime spend: " +
                TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - stArt) + "\n\n";
            LOGGER.warn(msg);
        }
        boolean b = FileSystemWorker.copyOrDelFile(newLanFile, p.toAbsolutePath().toString(), false);
        NET_SCAN_FILE_WORKER.setNewLanLastScan(p.toFile());
        scanOldLan(stArt);
    }

    /**
     {@link #scanNew()}
     */
    @Override
    public void run() {
        LOGGER.warn("DiapazonedScan.run");
        scanNew();
    }

    /**
     Сканер локальной сети

     @param printWriter Запись в лог
     @param fromVlan    начало с 3 октета IP
     @param toVlan      конец с 3 октета IP
     @param stArt       таймер
     @param whatVlan    первый 2 октета, с точкоё в конце.
     @throws IOException запись в файл
     */
    @SuppressWarnings("MethodWithMultipleLoops")
    private void scanLan(PrintWriter printWriter, int fromVlan, int toVlan, long stArt, String whatVlan) throws IOException {
        String msg1 = "DiapazonedScan.scanLan " + whatVlan;
        LOGGER.warn(msg1);
        try{
            String lasiIP = allDevices.getLast().split(">/")[1];
            lasiIP = lasiIP.split("</f")[0] + " last ip";
            new MessageSwing().infoTimer(lasiIP + "\n" + msg1);
        }
        catch(NoSuchElementException e){
            new MessageSwing((ActionEvent e1) -> {
                LOGGER.warn(new Date(e1.getWhen()).toString());
                AppInfoOnLoad.diaScanReader();
            }).infoTimer(e.getMessage());
        }
        for (int i = fromVlan; i < toVlan; i++) {
            StringBuilder msgBuild = new StringBuilder();
            for (int j = 0; j < MAX_IN_VLAN; j++) {
                msgBuild = new StringBuilder();
                byte[] aBytes = InetAddress.getByName(whatVlan + i + "." + j).getAddress();
                InetAddress byAddress = InetAddress.getByAddress(aBytes);
                int t = 100;
                if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
                    t = 400;
                }
                String toString = byAddress.toString();
                if (byAddress.isReachable(t)) {
                    String hostName = byAddress.getHostName();
                    Thread.currentThread().setName(ConstantsFor.getUpTime());
                    printWriter.println(hostName + " " + byAddress.getHostAddress());
                    NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
                    allDevices.add("<font color=\"green\">" + toString + FONT_BR_STR);
                } else {
                    Thread.currentThread().setName(allDevices.size() + " of " + ConstantsFor.IPS_IN_VELKOM_VLAN);
                    allDevices.add("<font color=\"red\">" + toString + FONT_BR_STR);
                }
                msgBuild.append("IP was ").append(whatVlan).append(i).append("<-i.j->").append(j).append("\n")
                    .append(j).append(" was j\n");
            }
            msgBuild
                .append(i).append(" was i. Total time: ")
                .append(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - stArt))
                .append("min\n").append(allDevices.size()).append(" ALL_DEVICES.size()");
            String msg = msgBuild.toString();
            LOGGER.warn(msg);
        }
    }

    /**
     @return /showalldev = {@link NetScanCtr#allDevices(Model, HttpServletRequest, HttpServletResponse)}
     */
    @SuppressWarnings ("StringConcatenation")
    @Override
    public String toString() {
        try{
            fileTimes = ConstantsFor.AVAILABLE_LAST_TXT + " " +
                Paths.get(ConstantsFor.AVAILABLE_LAST_TXT).toFile().lastModified() + "\n" +
                ConstantsFor.OLD_LAN_TXT +
                " " +
                Paths.get(ConstantsFor.OLD_LAN_TXT).toFile().lastModified();
        }
        catch(NullPointerException e){
            LOGGER.info("NO FILES!");
        }
        LOGGER.info("DiapazonedScan.toString");
        final StringBuilder sb = new StringBuilder("DiapazonedScan{ ");
        sb
            .append("<a href=\"/showalldev\">ALL_DEVICES ")
            .append(allDevices.size())
            .append("/5610 (")
            .append(( float ) allDevices.size() / ( float ) (ConstantsFor.IPS_IN_VELKOM_VLAN / 100))
            .append(" %)");
        sb.append("</a>}");
        sb.append(" ROOT_PATH_STR= ").append(ROOT_PATH_STR);
        sb.append(" fileTimes= ").append(fileTimes);
        sb.append("<br>NetScanFileWorker hash= ").append(NET_SCAN_FILE_WORKER.hashCode());

        return sb.toString();
    }

    /**
     Пингует в 200х VLANах девайсы с 10.200.x.250 по 10.200.x.254
     <p>
     Свичи начала сегментов. Вкл. в оптическое ядро.

     @return лист важного оборудования
     @throws IllegalAccessException swF.get(swF).toString()
     */
    List<String> pingSwitch() throws IllegalAccessException {
        LOGGER.warn("DiapazonedScan.pingSwitch");
        Field[] swFields = SwitchesWiFi.class.getFields();
        List<String> swList = new ArrayList<>();
        for(Field swF : swFields){
            String ipAddrStr = swF.get(swF).toString();
            swList.add(ipAddrStr);
            nameAddr.put(swF.getName(), ipAddrStr);
        }
        return swList;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(allDevices);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Object readObject = in.readObject();
        ConstantsFor.setAllDevices(( BlockingDeque<String> ) readObject);
        this.allDevices = ConstantsFor.ALL_DEVICES;
    }

    /**
     192.168.11-14.254

     @param stArt таймер начала общего скана
     @see #scanNew()
     */
    @SuppressWarnings("MagicNumber")
    private void scanOldLan(long stArt) {
        File oldLANFile = new File(ConstantsFor.OLD_LAN_TXT);
        Path p = Paths.get(new StringBuilder()
            .append(ROOT_PATH_STR)
            .append("\\lan\\192_")
            .append(System.currentTimeMillis() / 1000)
            .append(".scan").toString());
        String msg1 = new StringBuilder()
            .append("scanOldLan ")
            .append(p.toAbsolutePath().toString()).toString();
        LOGGER.warn(msg1);
        try (OutputStream outputStream = new FileOutputStream(oldLANFile);
             PrintWriter printWriter = new PrintWriter(outputStream, true)) {
            scanLan(printWriter, 11, 15, stArt, "192.168.");
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        boolean b = FileSystemWorker.copyOrDelFile(oldLANFile, p.toAbsolutePath().toString(), false);
        NET_SCAN_FILE_WORKER.setOldLanLastScan(p.toFile());
        String msg = "Vlans 11-14 completed.\nTime spend: " +
            TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - stArt) + "\n" + b + " copyOrDelFile.\n\n";
        LOGGER.warn(msg);
        ScanOnline.getI().getOffLines().clear();
        ScanOnline.getI().getOnLinesResolve().clear();
        this.stopClass = new TimeChecker().call().getReturnTime();
    }

}
