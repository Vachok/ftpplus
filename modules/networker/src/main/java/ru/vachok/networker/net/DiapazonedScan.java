package ru.vachok.networker.net;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.services.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;


/**
 Скан диапазона адресов

 @since 19.12.2018 (11:35) */
public class DiapazonedScan implements Runnable {

    /**
     {@link AppComponents#getLogger(String)}
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
     {@link ConstantsNet#getAllDevices()}
     */
    private static final BlockingDeque<String> ALL_DEVICES_LOCAL_DEQUE = ConstantsNet.getAllDevices();

    /**
     Singleton inst
     */
    private static final DiapazonedScan OUR_INSTANCE = new DiapazonedScan();

    private static final MessageToUser messageToUser = new MessageLocal();

    private static final String STR_ISFILECOPIED = "isFileCopied";

    private long stopClassStampLong = System.currentTimeMillis();

    private long stArt;

    public long getStopClassStampLong() {
        return stopClassStampLong;
    }

    /**
     SINGLETON

     @return single.
     */
    public static DiapazonedScan getInstance() {
        messageToUser.warn("DiapazonedScan.getInstance");
        messageToUser.info(ConstantsFor.STR_INPUT_OUTPUT, "", "ru.vachok.networker.net.DiapazonedScan");
        return OUR_INSTANCE;
    }

// --Commented out by Inspection START (05.03.2019 9:05):
//    /**
//     @return {@link #NET_SCAN_FILE_WORKER_INST}
//     */
//    static NetScanFileWorker getNetScanFileWorkerInst() {
//        return NET_SCAN_FILE_WORKER_INST;
//    }
// --Commented out by Inspection STOP (05.03.2019 9:05)

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
        AppComponents.threadConfig().thrNameSet("DiaPSW");
        Field[] swFields = SwitchesWiFi.class.getFields();
        List<String> swList = new ArrayList<>();
        for (Field swF : swFields) {
            String ipAddrStr = swF.get(swF).toString();
            swList.add(ipAddrStr);
            messageToUser.info(ipAddrStr);
        }
        return swList;
    }

    private void ipScan(String whatVlan, int i, int j, PrintWriter printWriter) throws IOException {
        AppComponents.threadConfig().thrNameSet("DIAip");
        int t = 75;
        byte[] aBytes = InetAddress.getByName(whatVlan + i + "." + j).getAddress();
        InetAddress byAddress = InetAddress.getByAddress(aBytes);
        String toString = byAddress.toString();
        if (ConstantsFor.thisPC().equalsIgnoreCase("HOME")) {
            t = 250;
        }
        if (byAddress.isReachable(t)) {
            AppComponents.threadConfig().thrNameSet("Dia");
            String hostName = byAddress.getHostName();
            String hostAddress = byAddress.getHostAddress();
            printWriter.println(hostName + " " + hostAddress);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
            ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"green\">" + toString + FONT_BR_STR);

            String valStr = "host = " + hostName + "/" + hostAddress + " is online: " + true;
            LOGGER.info(valStr);
        } else {
            AppComponents.threadConfig().thrNameSet(ALL_DEVICES_LOCAL_DEQUE.size() + " of " + ConstantsNet.IPS_IN_VELKOM_VLAN);
            ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"red\">" + toString + FONT_BR_STR);
        }
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
    @SuppressWarnings({"MethodWithMultipleLoops", "ObjectAllocationInLoop"})
    private void scanLan(PrintWriter printWriter, int fromVlan, int toVlan, long stArt, String whatVlan) throws IOException {
        AppComponents.threadConfig().thrNameSet("DIAlan");
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
            messageToUser.warn(msg);
        }
    }

    /**
     Добавляет в {@link ConstantsNet#getAllDevices()} адреса <i>10.200.200-217.254</i>
     */
    private void scanNew() {
        AppComponents.threadConfig().thrNameSet("DIAnew");
        this.stArt = ConstantsFor.getAtomicTime();

        String classMeth = "DiapazonedScan.scanNew";
        File newLanFile = new File(ConstantsNet.FILENAME_AVAILABLELASTTXT);
        Path p = Paths.get(ROOT_PATH_STR + "\\lan\\200_" + System.currentTimeMillis() / 1000 + ".scan");

        AppComponents.threadConfig().executeAsThread(this::scanOldLan);

        try (OutputStream outputStream = new FileOutputStream(newLanFile);
             PrintWriter printWriter = new PrintWriter(outputStream, true)) {
            writeToFileByConditions(printWriter, stArt);
        } catch (IOException e) {
            messageToUser.errorAlert(getClass().getSimpleName(), "scanNew", e.getMessage());
            FileSystemWorker.error("DiapazonedScan.scanNew", e);
        }
        boolean isFileCopied = FileSystemWorker.copyOrDelFile(newLanFile, p.toAbsolutePath().toString(), false);
        NET_SCAN_FILE_WORKER_INST.setNewLanLastScan(p.toFile());
        messageToUser.info(classMeth, "p.toAbsolutePath().toString()", p.toAbsolutePath().toString());
        messageToUser.info(classMeth, STR_ISFILECOPIED, String.valueOf(isFileCopied));
        this.stopClassStampLong = System.currentTimeMillis();
    }

    /**
     192.168.11-14.254

     @param stArt таймер начала общего скана
     @see #scanNew()
     */
    @SuppressWarnings("MagicNumber")
    private void scanOldLan() {
        AppComponents.threadConfig().thrNameSet("DIAold");

        String classMeth = getClass().getSimpleName() + ".scanOldLan";
        File oldLANFile = new File(ConstantsNet.FILENAME_OLDLANTXT);
        Path p = Paths.get(ROOT_PATH_STR + "\\lan\\192_" + System.currentTimeMillis() / 1000 + ".scan");

        AppComponents.threadConfig().executeAsThread(this::scanServers);

        try (OutputStream outputStream = new FileOutputStream(oldLANFile);
             PrintWriter printWriter = new PrintWriter(outputStream, true)) {
            scanLan(printWriter, 11, 15, stArt, "192.168.");
        } catch (IOException e) {
            messageToUser.errorAlert("DiapazonedScan", "scanOldLan", e.getMessage());
            FileSystemWorker.error(classMeth, e);
        }
        boolean isFileCopied = FileSystemWorker.copyOrDelFile(oldLANFile, p.toAbsolutePath().toString(), false);
        NET_SCAN_FILE_WORKER_INST.setOldLanLastScan(p.toFile());
        messageToUser.info(classMeth, "p.toAbsolutePath()", p.toAbsolutePath().toString());
        messageToUser.info(classMeth, STR_ISFILECOPIED, String.valueOf(isFileCopied));
        this.stopClassStampLong = System.currentTimeMillis();
    }

    private void writeToFileByConditions(PrintWriter printWriter, long stArt) throws IOException {
        AppComponents.threadConfig().thrNameSet("DIAfile");

        if (ALL_DEVICES_LOCAL_DEQUE.remainingCapacity() == 0) {
            messageToUser.infoNoTitles(new TForms().fromArray(ALL_DEVICES_LOCAL_DEQUE, false));
            ALL_DEVICES_LOCAL_DEQUE.clear();
            scanLan(printWriter, 200, 218, stArt, "10.200.");
        } else {
            scanLan(printWriter, 200, 218, stArt, "10.200.");
        }
        NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
    }

    /**
     Скан подсетей 10.10.xx.xxx
     */
    private void scanServers() {
        AppComponents.threadConfig().thrNameSet("DIAsrv");

        String classMeth = "DiapazonedScan.scanServers";
        File srvFile = new File(ConstantsNet.FILENAME_SERVTXT);
        Path path = Paths.get(ROOT_PATH_STR + "\\lan\\srv_" + System.currentTimeMillis() / 1000 + ".scan");

        try (OutputStream outputStream = new FileOutputStream(srvFile);
             PrintWriter printWriter = new PrintWriter(outputStream, true)) {
            scanLan(printWriter, 11, 40, stArt, "10.10.");
        } catch (IOException e) {
            messageToUser.errorAlert(getClass().getSimpleName(), "scanServers", e.getMessage());
            FileSystemWorker.error(classMeth, e);
        }
        boolean isFileCopied = FileSystemWorker.copyOrDelFile(srvFile, path.toAbsolutePath().toString(), false);
        NET_SCAN_FILE_WORKER_INST.setSrvScan(path.toFile());
        messageToUser.info(classMeth, "p.toAbsolutePath()", path.toAbsolutePath().toString());
        messageToUser.info(classMeth, STR_ISFILECOPIED, String.valueOf(isFileCopied));
        this.stopClassStampLong = System.currentTimeMillis();
    }

    /**
     Старт
     */
    @Override
    public void run() {
        AppComponents.threadConfig().executeAsThread(this::scanNew);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stopClassStampLong, stArt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiapazonedScan that = (DiapazonedScan) o;
        return stopClassStampLong == that.stopClassStampLong &&
            stArt == that.stArt;
    }

    /**
     @return /showalldev = {@link NetScanCtr#allDevices(Model, HttpServletRequest, HttpServletResponse)}
     */
    @SuppressWarnings("StringConcatenation")
    @Override
    public String toString() {
        String fileTimes = "No filetimes";
        try {
            fileTimes = ConstantsNet.FILENAME_AVAILABLELASTTXT + " " +
                new Date(Paths.get(ConstantsNet.FILENAME_AVAILABLELASTTXT).toFile().lastModified()) + "\n" +
                ConstantsNet.FILENAME_OLDLANTXT +
                " " +
                new Date(Paths.get(ConstantsNet.FILENAME_OLDLANTXT).toFile().lastModified()) + "\n" +
                ConstantsNet.FILENAME_SERVTXT +
                " " +
                new Date(Paths.get(ConstantsNet.FILENAME_SERVTXT).toFile().lastModified()) + "";
        } catch (NullPointerException e) {
            messageToUser.info("NO FILES!");
        }
        final StringBuilder sb = new StringBuilder("DiapazonedScan. Start at ")
            .append(new Date(stArt)).append("( ").append(TimeUnit.MILLISECONDS.toMinutes(ConstantsFor.getAtomicTime() - stArt)).append(" min) ")
            .append("{ ");

        sb
            .append("<a href=\"/showalldev\">ALL_DEVICES ")
            .append(ALL_DEVICES_LOCAL_DEQUE.size())
            .append("/").append(ConstantsNet.IPS_IN_VELKOM_VLAN).append("(")
            .append((float) ALL_DEVICES_LOCAL_DEQUE.size() / (float) (ConstantsNet.IPS_IN_VELKOM_VLAN / 100))
            .append(" %)");
        sb.append("</a>}");
        sb.append(" ROOT_PATH_STR= ").append(ROOT_PATH_STR);
        sb.append(" fileTimes= ").append(fileTimes);
        sb.append("<br>NetScanFileWorker hash= ").append(NET_SCAN_FILE_WORKER_INST.hashCode());

        return sb.toString();
    }

}
