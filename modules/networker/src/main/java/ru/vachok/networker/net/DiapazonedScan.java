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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;


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

    private List<File> srvFiles = new ArrayList<>();

    public long getStopClassStampLong() {
        return stopClassStampLong;
    }

    /**
     SINGLETON

     @return single.
     */
    public static DiapazonedScan getInstance() {
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
            messageToUser.info(ipAddrStr);
        }
        return swList;
    }

    private void ipScan(String whatVlan, int i, int j, PrintWriter printWriter) throws IOException {
        int t = (int) ConstantsFor.DELAY;
        byte[] aBytes = InetAddress.getByName(whatVlan + i + "." + j).getAddress();
        InetAddress byAddress = InetAddress.getByAddress(aBytes);
        String toString = byAddress.toString();
        if (ConstantsFor.thisPC().equalsIgnoreCase("HOME")) {
            t = (int) (ConstantsFor.DELAY * 3);
        }
        if (byAddress.isReachable(t)) {
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
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
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

     @see #scanNew()
     */
    @SuppressWarnings("MagicNumber")
    private void scanOldLan() {
        String classMeth = getClass().getSimpleName() + ".scanOldLan";
        File oldLANFile = new File(ConstantsNet.FILENAME_OLDLANTXT);
        Path p = Paths.get(ROOT_PATH_STR + "\\lan\\192_" + System.currentTimeMillis() / 1000 + ".scan");

        AppComponents.threadConfig().executeAsThread(this::scanServers);

        try (OutputStream outputStream = new FileOutputStream(oldLANFile);
             PrintWriter printWriter = new PrintWriter(outputStream, true)) {
            scanLan(printWriter, 11, 20, stArt, "192.168.");
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

    /**
     Скан подсетей 10.10.xx.xxx
     */
    private void scanServers() {
        String classMeth = "DiapazonedScan.scanServers";
        File srvFile;
        Path path = Paths.get(ROOT_PATH_STR + "\\lan\\srv_" + System.currentTimeMillis() / 1000 + ".scan");


        srvFile = new File("11v_" + ConstantsNet.FILENAME_SERVTXT);
        srvFiles.add(srvFile);
        Thread srv11 = new Vlans1010ToScan(srvFile, 11, 20);

        srvFile = new File("21v_" + ConstantsNet.FILENAME_SERVTXT);
        srvFiles.add(srvFile);
        Thread srv19 = new Vlans1010ToScan(srvFile, 21, 30);

        srvFile = new File("31v_" + ConstantsNet.FILENAME_SERVTXT);
        srvFiles.add(srvFile);
        Thread srv27 = new Vlans1010ToScan(srvFile, 31, 40);

        srvFile = new File("41v_" + ConstantsNet.FILENAME_SERVTXT);
        srvFiles.add(srvFile);
        Thread srv41 = new Vlans1010ToScan(srvFile, 41, 42);

        AppComponents.threadConfig().executeAsThread(srv11);
        AppComponents.threadConfig().executeAsThread(srv19);
        AppComponents.threadConfig().executeAsThread(srv27);
        AppComponents.threadConfig().executeAsThread(srv41);

        srvFiles.forEach(x -> {
            boolean isFileCopied = FileSystemWorker.copyOrDelFile(x, path.toAbsolutePath().toString(), false);
            messageToUser.info(classMeth, x.getAbsolutePath(), "Copy = " + isFileCopied);
            this.stopClassStampLong = System.currentTimeMillis();
        });
        NET_SCAN_FILE_WORKER_INST.setSrvFiles(srvFiles);
        messageToUser.info(classMeth, "p.toAbsolutePath()", path.toAbsolutePath().toString());
    }

    private void writeToFileByConditions(PrintWriter printWriter, long stArt) throws IOException {
        if (ALL_DEVICES_LOCAL_DEQUE.remainingCapacity() == 0) {
            messageToUser.infoNoTitles(new TForms().fromArray(ALL_DEVICES_LOCAL_DEQUE, false));
            ALL_DEVICES_LOCAL_DEQUE.clear();
            scanLan(printWriter, 200, 218, stArt, "10.200.");
        } else {
            scanLan(printWriter, 200, 220, stArt, "10.200.");
        }
        NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
    }

    @Override
    public int hashCode() {
        return Objects.hash(stopClassStampLong, stArt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
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
        StringBuilder fileTimes = new StringBuilder();
        try {
            fileTimes.append(
                ConstantsNet.FILENAME_AVAILABLELASTTXT + " " +
                new Date(Paths.get(ConstantsNet.FILENAME_AVAILABLELASTTXT).toFile().lastModified()) + "\n" +
                    ConstantsNet.FILENAME_OLDLANTXT + " " +
                    new Date(Paths.get(ConstantsNet.FILENAME_OLDLANTXT).toFile().lastModified()) + "\n");
            srvFiles.forEach(x -> fileTimes.append(
                x.getName() + " " +
                    new Date(x.lastModified()) + ""));
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
        sb.append(" fileTimes= ").append(fileTimes.toString());
        sb.append("<br>NetScanFileWorker hash= ").append(NET_SCAN_FILE_WORKER_INST.hashCode());

        return sb.toString();
    }

    /**
     * Старт
     */
    @Override
    public void run() {
        Future<?> future = AppComponents.threadConfig().getTaskExecutor().submit(this::scanNew);
        long timeOut = ((9 * ConstantsNet.MAX_IN_ONE_VLAN) / 116) + ConstantsFor.DELAY;
        messageToUser.warn("DiapazonedScan.run", "timeOut", " = " + timeOut);
        try {
            future.get(timeOut, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            messageToUser.errorAlert(getClass().getSimpleName(), "run", e.getMessage());
            FileSystemWorker.error("DiapazonedScan.run", e);
        }
    }

    /**
     * Сканер сегментов в подсетях 10.10.x.x
     *
     * @since 06.03.2019 (2:46)
     */
    private class Vlans1010ToScan extends Thread {

        private final File srvFile;
        private int toVlan;
        private int fromVlan;

        public Vlans1010ToScan(File srvFile, int fromVlan, int toVlan) {
            this.srvFile = srvFile;
            this.fromVlan = fromVlan;
            this.toVlan = toVlan;
        }

        @Override
        public void run() {
            try (OutputStream outputStream = new FileOutputStream(srvFile);
                 PrintWriter printWriter = new PrintWriter(outputStream, true)) {
                scanLan(printWriter, fromVlan, toVlan, stArt, "10.10.");
            } catch (IOException e) {
                messageToUser.errorAlert("DiapazonedScan", "scanServers", e.getMessage());
                FileSystemWorker.error("DiapazonedScan.scanServers", e);
            }
        }
    }
}
