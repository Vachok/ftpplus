// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.springframework.ui.Model;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.MessageToTray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static ru.vachok.networker.net.enums.ConstantsNet.*;


/**
 Скан диапазона адресов

 @since 19.12.2018 (11:35) */
@SuppressWarnings({"ClassWithMultipleLoggers", "MagicNumber", "resource", "IOResourceOpenedButNotSafelyClosed"})
public class DiapazonedScan implements Runnable {

    private final long stArt;

    /**
     Корень директории.
     */
    private static final String ROOT_PATH_STR = Paths.get(".").toAbsolutePath().toString();

    /**
     Повторения.
     */
    private static final String FONT_BR_STR = "</font><br>\n";

    private static final int MAX_IN_VLAN_INT = 255;

    /**
     {@link ConstantsNet#getAllDevices()}
     */
    private static final BlockingDeque<String> ALL_DEVICES_LOCAL_DEQUE = ConstantsNet.getAllDevices();

    private static final MessageToUser messageToUser = new MessageLocal(DiapazonedScan.class.getSimpleName());
    /**
     {@link NetScanFileWorker#getI()}
     */
    private static final NetScanFileWorker NET_SCAN_FILE_WORKER_INST = NetScanFileWorker.getI();
    /**
     Singleton inst
     */
    private static DiapazonedScan OUR_INSTANCE = new DiapazonedScan();

    private long stopClassStampLong = NetScanFileWorker.getI().getLastStamp();

    private Map<String, File> srvFiles = new ConcurrentHashMap<>();

    /**
     Приватный конструктор
     */
    private DiapazonedScan() {
        AppComponents.threadConfig().thrNameSet("DScanF:" + srvFiles.size());
        stArt = System.currentTimeMillis();
    }

    public Map<String, File> getSrvFiles() throws NullPointerException {
        return NetScanFileWorker.srvFiles;
    }

    public long getStopClassStampLong() {
        return stopClassStampLong;
    }

    @Override
    public void run() {
        String classMeth = "DiapazonedScan.run";
        startDo();
    }

    /**
     @return /showalldev = {@link NetScanCtr#allDevices(Model , HttpServletRequest , HttpServletResponse)}
     */
    @SuppressWarnings("StringConcatenation")
    @Override
    public String toString() {
        return theInfoToString();
    }

    /**
     SINGLETON

     @return single.
     */
    public static DiapazonedScan getInstance() {
        return OUR_INSTANCE;
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

    private void copyToArch() {
        int archID = new Random().nextInt(1000);
        Collection<File> fileList = srvFiles.values();
        for (File f : fileList) {
            FileSystemWorker.copyOrDelFile(f , ".\\lan\\" + f.getName().replace(".txt" , "") + archID + ".scan" , true);
        }
    }

    @SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed"})
    private void theNewLan() {
        String vlanIs = "10.200.";
        String classMeth = "DiapazonedScan.theNewLan";
        String methName = ".theNewLan";

        Runnable execScan200210 = new ExecScan(200, 210, "10.200.", new File(FILENAME_NEWLAN200210));
        AppComponents.threadConfig().execByThreadConfig(execScan200210);

        Runnable execScan210220 = new ExecScan(200, 218, "10.200.", new File(FILENAME_NEWLAN200210));
        AppComponents.threadConfig().execByThreadConfig(execScan210220);
    }

    private void startDo() {
        if (ALL_DEVICES_LOCAL_DEQUE.remainingCapacity() == 0) {
            boolean isWritten = new ExitApp("alldev." + (System.currentTimeMillis() / 1000) + ".map", ALL_DEVICES_LOCAL_DEQUE).writeOwnObject();
            if (isWritten) {
                ALL_DEVICES_LOCAL_DEQUE.clear();
            } else {
                new MessageSwing(getInstance().getClass().getSimpleName()).warn("ALL_DEVICES_LOCAL_DEQUE is not written by " + getInstance().hashCode() + ".startDo");
            }
        }

        AppComponents.threadConfig().getTaskExecutor().submit(this::theNewLan);
        AppComponents.threadConfig().getTaskExecutor().submit(this::scanServers);
        AppComponents.threadConfig().getTaskExecutor().submit(this::scanOldLan);
    }

    /**
     192.168.11-14.254
     */
    @SuppressWarnings("MagicNumber")
    private void scanOldLan() {
        String classMeth = this.getClass().getSimpleName() + ".scanOldLan";

        Runnable execScanOld0 = new ExecScan(11, 16, "192.168.", new File(FILENAME_OLDLANTXT0));
        Runnable execScanOld1 = new ExecScan(16, 21, "192.168.", new File(FILENAME_OLDLANTXT1));

        AppComponents.threadConfig().execByThreadConfig(execScanOld0);
        AppComponents.threadConfig().execByThreadConfig(execScanOld1);

        NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
    }

    /**
     Скан подсетей 10.10.xx.xxx
     */
    private void scanServers() {
        @SuppressWarnings("DuplicateStringLiteralInspection") String classMeth = "DiapazonedScan.scanServers";
        @SuppressWarnings("DuplicateStringLiteralInspection") String methName = "scanServers";
        String vlanIs = "10.10.";
        Runnable[] runnablesScan = {
            new ExecScan(10, 20, vlanIs, new File(FILENAME_SERVTXT_11SRVTXT)),
            new ExecScan(21, 31, vlanIs, new File(FILENAME_SERVTXT_21SRVTXT)),
            new ExecScan(31, 41, vlanIs, new File(FILENAME_SERVTXT_31SRVTXT)),
            new ExecScan(41, 51, vlanIs, new File(FILENAME_SERVTXT_41SRVTXT))
        };
        for (Runnable r : runnablesScan) {
            AppComponents.threadConfig().execByThreadConfig(r);
        }
    }

    /**
     Чтобы случайно не уничтожить Overridden {@link #toString()}
     <p>
     @return информация о состоянии файлов
     */
    private String theInfoToString() {
        StringBuilder fileTimes = new StringBuilder();
        messageToUser.warn("DiapazonedScan.theInfoToString", "ROOT_PATH_STR", " = " + ROOT_PATH_STR);
        try {
            String atStr = " size in bytes: ";

            fileTimes.append(ConstantsNet.FILENAME_NEWLAN210).append(FILENAME_NEWLAN200210).append(atStr);
            fileTimes.append(Paths.get(ConstantsNet.FILENAME_NEWLAN210).toFile().length());
            fileTimes.append(Paths.get(FILENAME_NEWLAN200210).toFile().length()).append("<br>\n");
            fileTimes.append(ConstantsNet.FILENAME_OLDLANTXT0).append(", ");
            fileTimes.append(ConstantsNet.FILENAME_OLDLANTXT1).append(atStr);
            fileTimes.append(Paths.get(ConstantsNet.FILENAME_OLDLANTXT0).toFile().length()).append("<br>\n");
            fileTimes.append(Paths.get(ConstantsNet.FILENAME_OLDLANTXT1).toFile().length()).append("<br>\n");

            NetScanFileWorker.srvFiles.forEach(( k , v ) -> fileTimes.append(k).append(" is ").append(v.getName()).append(atStr).append(v.length()).append("<br>\n"));
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
        sb.append("<br><b>\nfileTimes= </b><br>").append(fileTimes);
        return sb.toString();
    }


    /**
     * Да запуска скана из {@link DiapazonedScan}
     *
     * @since 24.03.2019 (16:01)
     */
    class ExecScan implements Runnable {

        private int from;

        private int to;

        private String whatVlan;

        private File vlanFile;

        private MessageToUser messageToUser = new MessageToTray();

        public ExecScan(int from, int to, String whatVlan, File vlanFile) {
            this.from = from;
            this.to = to;
            this.whatVlan = whatVlan;
            this.vlanFile = vlanFile;
        }

        @Override
        public void run() {
            try {
                OutputStream outputStream = new FileOutputStream(vlanFile);
                PrintStream printStream = new PrintStream(outputStream, true);
                messageToUser.info(getClass().getSimpleName() + ".run", "bodyMsg", " = " + execScan(printStream));
            } catch (FileNotFoundException e) {
                messageToUser.errorAlert(getClass().getSimpleName(), ".run", e.getMessage());
                FileSystemWorker.error(getClass().getSimpleName() + ".run", e);
            }
            srvFiles.put(vlanFile.getName(), vlanFile);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ExecScan{");
            sb.append("from=").append(from);
            sb.append(", to=").append(to);
            sb.append(", vlanFile=").append(vlanFile);
            sb.append(", whatVlan='").append(whatVlan).append('\'');
            sb.append('}');
            return sb.toString();
        }

        private boolean execScan(PrintStream printStream) {
            @SuppressWarnings("DuplicateStringLiteralInspection") String methName = ".execScan";
            try {
                ConcurrentMap<String, String> stringStringConcurrentMap = scanLanSegment(to, from, whatVlan, printStream);
                NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
                return true;
            } catch (Exception e) {
                messageToUser.errorAlert(getClass().getSimpleName(), methName, e.getMessage());
                FileSystemWorker.error(getClass().getSimpleName() + methName, e);
                return false;
            }
        }

        private String oneIpScanAndPrintToFile(PrintStream printStream) throws IOException {
            AppComponents.threadConfig().thrNameSet(String
                .valueOf(ALL_DEVICES_LOCAL_DEQUE.remainingCapacity()));
            String classMeth = "DiapazonedScan.oneIpScanAndPrintToFile";
            int timeOutMSec = (int) ConstantsFor.DELAY;
            if (ConstantsFor.thisPC().equalsIgnoreCase("HOME")) {
                timeOutMSec = (int) (ConstantsFor.DELAY * 3);
                NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
            }
            byte[] aBytes = InetAddress.getByName(whatVlan + from + "." + to).getAddress();
            StringBuilder stringBuffer = new StringBuilder();
            InetAddress byAddress = InetAddress.getByAddress(aBytes);
            String hostName = byAddress.getHostName();
            String hostAddress = byAddress.getHostAddress();
            if (byAddress.isReachable(timeOutMSec)) {
                NetListKeeper.getI().getOnLinesResolve().put(hostAddress, hostName);
                NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
                ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"green\">" + hostName + FONT_BR_STR);

                stringBuffer.append(hostAddress).append(" ").append(hostName);
                printStream.println(hostAddress + " " + hostName);
            } else {
                NetListKeeper.getI().getOffLines().put(byAddress.getHostAddress(), hostName);
                ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"red\">" + hostName + FONT_BR_STR);
                stringBuffer.append(hostAddress).append(" ").append(hostName);
            }
            return stringBuffer.toString();
        }

        /**
         * Сканер локальной сети@param stStMap Запись в лог@param fromVlan начало с 3 октета IP@param toVlan   конец с 3 октета IP
         *
         * @param whatVlan первый 2 октета, с точкоё в конце.
         */
        private ConcurrentMap<String, String> scanLanSegment(int fromVlan, int toVlan, String whatVlan, PrintStream printStream) {
            @SuppressWarnings("DuplicateStringLiteralInspection") String methName = ".scanLanSegment";
            ConcurrentMap<String, String> stStMap = new ConcurrentHashMap<>(MAX_IN_VLAN_INT);
            for (int i = fromVlan; i < toVlan; i++) {
                StringBuilder msgBuild = new StringBuilder();
                for (int j = 0; j < MAX_IN_VLAN_INT; j++) {
                    try {
                        String theScannedIPHost = oneIpScanAndPrintToFile(printStream);
                        stStMap.putIfAbsent(theScannedIPHost.split(" ")[0], theScannedIPHost.split(" ")[1]);
                    } catch (IOException e) {
                        FileSystemWorker.error("DiapazonedScan.scanLanSegment", e);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        messageToUser.errorAlert(getClass().getSimpleName(), ".scanLanSegment", e.getMessage());
                        FileSystemWorker.error(getClass().getSimpleName() + ".scanLanSegment", e);
                    }
                }
            }
            return stStMap;
        }
    }
}
