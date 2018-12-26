package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.DBMessenger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ru.vachok.networker.componentsrepo.AppComponents.getLogger;


/**
 Скан диапазона адресов

 @since 19.12.2018 (11:35) */
@SuppressWarnings({"FieldNotUsedInToString", "DoubleCheckedLocking"})
public class DiapazonedScan implements Runnable {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = getLogger();

    /**
     Повторения.
     */
    private static final String FONT_BR_STR = "</font><br>";

    /**
     Корень директории.
     */
    private static final String ROOT_PATH_STR = Paths.get(".").toAbsolutePath().toString();

    /**
     Singleton inst
     */
    private static volatile DiapazonedScan ourInstance;

    /**
     {@link NetScanFileWorker#getI()}
     */
    private static final NetScanFileWorker NET_SCAN_FILE_WORKER = NetScanFileWorker.getI();

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
     Singleton
     */
    private DiapazonedScan() {
    }

    /**
     @return {@link #NET_SCAN_FILE_WORKER}
     */
    static NetScanFileWorker getNetScanFileWorker() {
        return NET_SCAN_FILE_WORKER;
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
     @return /showalldev = {@link NetScanCtr#allDevices(Model, HttpServletRequest, HttpServletResponse)}
     */
    @Override
    public String toString() {
        LOGGER.info("DiapazonedScan.toString");
        final StringBuilder sb = new StringBuilder("DiapazonedScan{ ");
        sb
            .append("<a href=\"/showalldev\">ALL_DEVICES ")
            .append(ConstantsFor.ALL_DEVICES.size())
            .append("/5610 (")
            .append((float) ConstantsFor.ALL_DEVICES.size() / (float) (ConstantsFor.IPS_IN_VELKOM_VLAN / 100))
            .append(" %)");
        sb.append("</a>}");
        sb.append(" ROOT_PATH_STR= ").append(ROOT_PATH_STR);
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
        LOGGER.info("DiapazonedScan.pingSwitch");
        StringBuilder stringBuilder = new StringBuilder();
        Field[] swFields = Switches.class.getFields();
        List<String> swList = new ArrayList<>();
        for (Field swF : swFields) {
            swList.add("\n" + swF.get(swF).toString());
        }
        swList.forEach(stringBuilder::append);
        String retMe = stringBuilder.toString();
        LOGGER.warn(retMe);
        return swList;
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
            if (ConstantsFor.ALL_DEVICES.remainingCapacity() == 0) {
                MessageToUser messageToUser = new DBMessenger();
                messageToUser.infoNoTitles(new TForms().fromArray(ConstantsFor.ALL_DEVICES));
                ConstantsFor.ALL_DEVICES.clear();
                scanLan(printWriter, 200, 218, stArt, "10.200.");
            } else scanLan(printWriter, 200, 218, stArt, "10.200.");
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
     192.168.11-14.254

     @param stArt таймер начала общего скана
     @see #scanNew()
     */
    private void scanOldLan(long stArt) {
        File oldLANFile = new File(ConstantsFor.OLD_LAN_TXT);
        Path p = Paths.get(ROOT_PATH_STR + "\\lan\\192_" + System.currentTimeMillis() / 1000 + ".scan");
        String msg1 = "scanOldLan " + p.toAbsolutePath().toString();
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
        for (int i = fromVlan; i < toVlan; i++) {
            StringBuilder msgBuild = new StringBuilder();
            for (int j = 0; j < 255; j++) {
                msgBuild = new StringBuilder();
                byte[] aBytes = InetAddress.getByName(whatVlan + i + "." + j).getAddress();
                InetAddress byAddress = InetAddress.getByAddress(aBytes);
                int t = 100;
                if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
                    t = 400;
                }
                if (byAddress.isReachable(t)) {
                    printWriter.println(byAddress.getHostName() + " " + byAddress.getHostAddress());
                    ConstantsFor.ALL_DEVICES.add("<font color=\"green\">" + byAddress.toString() + FONT_BR_STR);
                } else {
                    ConstantsFor.ALL_DEVICES.add("<font color=\"red\">" + byAddress.toString() + FONT_BR_STR);
                }
                msgBuild.append("IP was ").append(whatVlan).append(i).append("<-i.j->").append(j).append("\n")
                    .append(j).append(" was j\n");
                String msg = msgBuild.toString();
                LOGGER.info(msg);
            }
            msgBuild
                .append(i).append(" was i. Total time: ")
                .append(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - stArt))
                .append("min\n").append(ConstantsFor.ALL_DEVICES.size()).append(" ALL_DEVICES.size()");
            String msg = msgBuild.toString();
            LOGGER.warn(msg);
            printWriter.println(msg);
        }
    }

}
