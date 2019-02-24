package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.TimeChecker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;


/**
 Скан диапазона адресов

 @since 19.12.2018 (11:35) */
public class DiapazonedScan implements Runnable {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DiapazonedScan.class.getSimpleName());

    /**
     {@link NetScanFileWorker#getI()}
     */
    private static final NetScanFileWorker NET_SCAN_FILE_WORKER_INST = NetScanFileWorker.getI();

    /**
     Повторения.
     */
    private static final String FONT_BR_STR = "</font><br>";

    /**
     Корень директории.
     */
    private static final String ROOT_PATH_STR = Paths.get(".").toAbsolutePath().toString();

    private static final int MAX_IN_VLAN_INT = 255;

    /**
     {@link ConstantsFor#ALL_DEVICES}
     */
    private static final BlockingDeque<String> ALL_DEVICES_LOCAL_DEQUE = ConstantsFor.ALL_DEVICES;

    /**
     Singleton inst
     */
    private static final DiapazonedScan OUR_INSTANCE = new DiapazonedScan();

    private final MessageToUser messageToUser = new MessageLocal();

    private long stopClassStampLong = System.currentTimeMillis();

    public long getStopClassStampLong() {
        return stopClassStampLong;
    }

    /**
     SINGLETON

     @return single.
     */
    public static DiapazonedScan getInstance() {
        new MessageCons().errorAlert("DiapazonedScan.getInstance");
        new MessageCons().info(ConstantsFor.STR_INPUT_OUTPUT, "", "ru.vachok.networker.net.DiapazonedScan");
        return OUR_INSTANCE;
    }

    /**
     @return {@link #NET_SCAN_FILE_WORKER_INST}
     */
    static NetScanFileWorker getNetScanFileWorkerInst() {
        return NET_SCAN_FILE_WORKER_INST;
    }

    /**
     Приватный конструктор
     */
    private DiapazonedScan() {
        LOGGER.warn("DiapazonedScan.DiapazonedScan");
    }

    /**
     Пингует в 200х VLANах девайсы с 10.200.x.250 по 10.200.x.254
     <p>
     Свичи начала сегментов. Вкл. в оптическое ядро.

     @return лист важного оборудования
     @throws IllegalAccessException swF.get(swF).toString()
     */
    public static List<String> pingSwitch() throws IllegalAccessException {
        Field[] swFields = SwitchesWiFi.class.getFields();
        List<String> swList = new ArrayList<>();
        for (Field swF : swFields) {
            String ipAddrStr = swF.get(swF).toString();
            swList.add(ipAddrStr);
            new MessageCons().infoNoTitles(ipAddrStr);
        }
        return swList;
    }

    /**
     Добавляет в {@link ConstantsFor#ALL_DEVICES} адреса <i>10.200.200-217.254</i>
     */
    private void scanNew() {
        final long stArt = System.currentTimeMillis();
        final String classMeth = "DiapazonedScan.scanNew";
        Path p = Paths.get(ROOT_PATH_STR + "\\lan\\200_" + System.currentTimeMillis() / 1000 + ".scan");

        File newLanFile = new File(ConstantsFor.AVAILABLE_LAST_TXT);

        try (OutputStream outputStream = new FileOutputStream(newLanFile);
             PrintWriter printWriter = new PrintWriter(outputStream, true)) {
            writeToFileByConditions(printWriter, stArt);
        } catch (IOException e) {
            FileSystemWorker.error(classMeth, e);
        }
        boolean isFileCopied = FileSystemWorker.copyOrDelFile(newLanFile, p.toAbsolutePath().toString(), false);
        NET_SCAN_FILE_WORKER_INST.setNewLanLastScan(p.toFile());

        messageToUser.info(classMeth, "p.toAbsolutePath().toString()", p.toAbsolutePath().toString());
        messageToUser.info(classMeth, "isFileCopied", String.valueOf(isFileCopied));

        scanOldLan(stArt);
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
        for (int i = fromVlan; i < toVlan; i++) {
            StringBuilder msgBuild = new StringBuilder();
            for (int j = 0; j < MAX_IN_VLAN_INT; j++) {
                ipScan(whatVlan, i, j, printWriter);
            }
            msgBuild
                .append(i).append(" was i. Total time: ")
                .append(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - stArt))
                .append("min\n").append(ALL_DEVICES_LOCAL_DEQUE.size()).append(" ALL_DEVICES.size()");
            String msg = msgBuild.toString();
            LOGGER.warn(msg);
        }
    }

    private void ipScan(String whatVlan, int i, int j, PrintWriter printWriter) throws IOException {
        int t = 100;
        byte[] aBytes = InetAddress.getByName(whatVlan + i + "." + j).getAddress();
        InetAddress byAddress = InetAddress.getByAddress(aBytes);
        String toString = byAddress.toString();
        if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
            t = 500;
        }
        if (byAddress.isReachable(t)) {
            ConditionChecker.thrNameSet("Dia");
            String hostName = byAddress.getHostName();
            String hostAddress = byAddress.getHostAddress();
            printWriter.println(hostName + " " + hostAddress);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
            ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"green\">" + toString + FONT_BR_STR);

            String valStr = "host = " + hostName + "/" + hostAddress + " is online: " + true;
            LOGGER.info(valStr);
        } else {
            Thread.currentThread().setName(ALL_DEVICES_LOCAL_DEQUE.size() + " of " + ConstantsFor.IPS_IN_VELKOM_VLAN);
            ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"red\">" + toString + FONT_BR_STR);
        }
    }

    /**
     192.168.11-14.254

     @param stArt таймер начала общего скана
     @see #scanNew()
     */
    @SuppressWarnings("MagicNumber")
    private void scanOldLan(long stArt) {
        File oldLANFile = new File(ConstantsFor.OLD_LAN_TXT);
        Path p = Paths.get(new StringBuilder().append(ROOT_PATH_STR).append("\\lan\\192_").append(System.currentTimeMillis() / 1000).append(".scan").toString());

        try (OutputStream outputStream = new FileOutputStream(oldLANFile);
             PrintWriter printWriter = new PrintWriter(outputStream, true)) {
            scanLan(printWriter, 11, 15, stArt, "192.168.");
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        boolean isFileCopied = FileSystemWorker.copyOrDelFile(oldLANFile, p.toAbsolutePath().toString(), false);
        NET_SCAN_FILE_WORKER_INST.setOldLanLastScan(p.toFile());
        this.stopClassStampLong = new TimeChecker().call().getReturnTime();

        messageToUser.info("DiapazonedScan.scanOldLan", "p.toAbsolutePath()", p.toAbsolutePath().toString());
        messageToUser.info("DiapazonedScan.scanOldLan", "isFileCopied", String.valueOf(isFileCopied));
    }

    private void writeToFileByConditions(PrintWriter printWriter, long stArt) throws IOException {
        LOGGER.warn("DiapazonedScan.writeToFileByConditions");
        new MessageCons().info("printWriter = [" + printWriter.checkError() + "], stArt = [" + stArt + "]", ConstantsFor.STR_RETURNS, "void");

        if (ALL_DEVICES_LOCAL_DEQUE.remainingCapacity() == 0) {
            MessageToUser messageToUser = new MessageCons();
            messageToUser.infoNoTitles(new TForms().fromArray(ALL_DEVICES_LOCAL_DEQUE, false));
            ALL_DEVICES_LOCAL_DEQUE.clear();
            scanLan(printWriter, 200, 218, stArt, "10.200.");
        } else {
            scanLan(printWriter, 200, 218, stArt, "10.200.");
        }
        NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
    }

    @Override
    public int hashCode() {
        int result = messageToUser.hashCode();
        result = 31 * result + (int) (stopClassStampLong ^ (stopClassStampLong >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DiapazonedScan that = (DiapazonedScan) o;

        if (stopClassStampLong != that.stopClassStampLong) return false;
        return messageToUser.equals(that.messageToUser);
    }

    /**
     @return /showalldev = {@link NetScanCtr#allDevices(Model, HttpServletRequest, HttpServletResponse)}
     */
    @SuppressWarnings("StringConcatenation")
    @Override
    public String toString() {
        String fileTimes = "No filetimes";
        try {
            fileTimes = ConstantsFor.AVAILABLE_LAST_TXT + " " +
                Paths.get(ConstantsFor.AVAILABLE_LAST_TXT).toFile().lastModified() + "\n" +
                ConstantsFor.OLD_LAN_TXT +
                " " +
                Paths.get(ConstantsFor.OLD_LAN_TXT).toFile().lastModified();
        } catch (NullPointerException e) {
            LOGGER.info("NO FILES!");
        }
        LOGGER.info("DiapazonedScan.toString");
        final StringBuilder sb = new StringBuilder("DiapazonedScan{ ");
        sb
            .append("<a href=\"/showalldev\">ALL_DEVICES ")
            .append(ALL_DEVICES_LOCAL_DEQUE.size())
            .append("/5610 (")
            .append((float) ALL_DEVICES_LOCAL_DEQUE.size() / (float) (ConstantsFor.IPS_IN_VELKOM_VLAN / 100))
            .append(" %)");
        sb.append("</a>}");
        sb.append(" ROOT_PATH_STR= ").append(ROOT_PATH_STR);
        sb.append(" fileTimes= ").append(fileTimes);
        sb.append("<br>NetScanFileWorker hash= ").append(NET_SCAN_FILE_WORKER_INST.hashCode());

        return sb.toString();
    }

    /**
     Старт
     <p>
     1. {@link #readObject(ObjectInputStream)}. Пробуем десериализоваться. 2. {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)} запишем исключение в лог. {@link IOException} или
     {@link Class} <br>
     */
    @Override
    public void run() {
        Thread.currentThread().setName("DiapazonedScan.run");
        scanNew();
    }

}
