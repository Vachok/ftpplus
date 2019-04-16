// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.springframework.ui.Model;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.controller.NetScanCtr;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.services.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.time.LocalTime;
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

    /**
     Корень директории.
     */
    private static final String ROOT_PATH_STR = Paths.get(".").toAbsolutePath().toString();

    /**
     Повторения.
     */
    private static final String FONT_BR_STR = "</font><br>\n";

    private static final int MAX_IN_VLAN_INT = 255;

    private final DiapazonedScan.ExecScan[] runnablesScans = {
        new DiapazonedScan.ExecScan(10 , 20 , "10.10." , new File(FILENAME_SERVTXT_11SRVTXT)) ,
        new DiapazonedScan.ExecScan(21 , 31 , "10.10." , new File(FILENAME_SERVTXT_21SRVTXT)) ,
        new DiapazonedScan.ExecScan(31 , 41 , "10.10." , new File(FILENAME_SERVTXT_31SRVTXT)) ,
        new DiapazonedScan.ExecScan(41 , 51 , "10.10." , new File(FILENAME_SERVTXT_41SRVTXT)) ,
    };

    /**
     {@link ConstantsNet#getAllDevices()}
     */
    private static final BlockingDeque<String> ALL_DEVICES_LOCAL_DEQUE = getAllDevices();

    private static final MessageToUser messageToUser = new MessageLocal(DiapazonedScan.class.getSimpleName());
    /**
     {@link NetScanFileWorker#getI()}
     */
    private static final NetScanFileWorker NET_SCAN_FILE_WORKER_INST = NetScanFileWorker.getI();
    /**
     Singleton inst
     */
    private static final DiapazonedScan OUR_INSTANCE = new DiapazonedScan();

    private long stopClassStampLong = NetScanFileWorker.getI().getLastStamp();

    private Map<String, File> srvFiles = NET_SCAN_FILE_WORKER_INST.getSrvFiles();

    private final long timeStart = System.currentTimeMillis();

    /**
     Приватный конструктор
     */
    private DiapazonedScan() {
        AppComponents.threadConfig().thrNameSet("DScanF:" + srvFiles.size());
    }

    public Map<String, File> getSrvFiles() throws NullPointerException {
        return NET_SCAN_FILE_WORKER_INST.getSrvFiles();
    }

    public long getStopClassStampLong() {
        return stopClassStampLong;
    }

    @Override
    public void run() {
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
        srvFiles.clear();
    }

    @SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed"})
    private void theNewLan() {
        Runnable execScan200210 = new DiapazonedScan.ExecScan(200, 210, "10.200.", new File(FILENAME_NEWLAN200210));
        AppComponents.threadConfig().execByThreadConfig(execScan200210);

        Runnable execScan210220 = new DiapazonedScan.ExecScan(210 , 219 , "10.200." , new File(FILENAME_NEWLAN210));
        AppComponents.threadConfig().execByThreadConfig(execScan210220);
    }

    private void startDo() {
        if (ALL_DEVICES_LOCAL_DEQUE.remainingCapacity() == 0) {
            boolean isWritten = new ExitApp("alldev_" + (System.currentTimeMillis() / 1000) + ".map", ALL_DEVICES_LOCAL_DEQUE).writeOwnObject();
            if (isWritten) {
                ALL_DEVICES_LOCAL_DEQUE.clear();
            } else {
                messageToUser.info(getClass().getSimpleName() + ".startDo" , "ALL_DEVICES_LOCAL_DEQUE:" , " = " + ALL_DEVICES_LOCAL_DEQUE.size());
            }
        }
        AppComponents.threadConfig().execByThreadConfig(this::theNewLan);
        AppComponents.threadConfig().execByThreadConfig(this::scanServers);
        AppComponents.threadConfig().execByThreadConfig(this::scanOldLan);
    }

    /**
     * Да запуска скана из {@link DiapazonedScan}
     *
     * @since 24.03.2019 (16:01)
     */
    class ExecScan implements Runnable {

        private static final String PAT_IS_ONLINE = " is online";

        private final long stArt;

        private long getSpend() {
            return System.currentTimeMillis() - stArt;
        }

        private int from;

        private int to;

        private String whatVlan;

        private PrintStream printStream;

        private File vlanFile;

        private final MessageToUser messageToUser = new MessageLocal(DiapazonedScan.ExecScan.class.getSimpleName());

        public ExecScan(int from, int to, String whatVlan, File vlanFile) {
            this.from = from;
            this.to = to;
            this.whatVlan = whatVlan; this.vlanFile = vlanFile;
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(vlanFile);
            } catch (IOException e) {
                messageToUser.errorAlert(getClass().getSimpleName(), ".ExecScan", e.getMessage());
            }
            this.printStream = new PrintStream(Objects.requireNonNull(outputStream), true); stArt = System.currentTimeMillis();
        }

        @Override
        public void run() {
            boolean isExecute = execScan();
            if (isExecute) {
                messageToUser.info(getClass().getSimpleName() + ".run", "ALL_DEVICES_LOCAL_DEQUE.size()", " = " + ALL_DEVICES_LOCAL_DEQUE.size());
            } else {
                messageToUser.error(getClass().getSimpleName(), String.valueOf(ALL_DEVICES_LOCAL_DEQUE.remainingCapacity()), " ALL_DEVICES_LOCAL_DEQUE remainingCapacity!");
            }
        }

        private boolean execScan() {
            @SuppressWarnings("DuplicateStringLiteralInspection") String methName = ".execScan";
            try {
                ConcurrentMap<String, String> stringStringConcurrentMap = scanLanSegment(from, to, whatVlan, printStream);
                NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
                if (vlanFile.length() > 10) {
                    FileSystemWorker.copyOrDelFile(vlanFile, vlanFile
                        .getName()
                        .replace("lan_" , ".\\lan\\" + vlanFile.getName() + "." + LocalTime
                            .now()
                            .toSecondOfDay()), true);
                } else {
                    new ExitApp(NET_SCAN_FILE_WORKER_INST
                        .getClass()
                        .getSimpleName() + ".cla", NET_SCAN_FILE_WORKER_INST).writeOwnObject();
                }
                return true;
            } catch (Exception e) {
                messageToUser.errorAlert(getClass().getSimpleName(), methName, e.getMessage());
                FileSystemWorker.error(getClass().getSimpleName() + methName, e);
                return false;
            }
        }

        private String oneIpScanAndPrintToFile(int iThree, int jFour) throws IOException {
            AppComponents.threadConfig().thrNameSet(String.valueOf(ALL_DEVICES_LOCAL_DEQUE.remainingCapacity()));

            String classMeth = "DiapazonedScan.oneIpScanAndPrintToFile";
            int timeOutMSec = (int) ConstantsFor.DELAY;
            if (ConstantsFor.thisPC().equalsIgnoreCase("HOME")) {
                timeOutMSec = (int) (ConstantsFor.DELAY * 2);
                NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
            }
            byte[] aBytes = InetAddress.getByName(whatVlan + iThree + "." + jFour).getAddress();

            StringBuilder stringBuffer = new StringBuilder();
            InetAddress byAddress = InetAddress.getByAddress(aBytes);
            String hostName = byAddress.getHostName();
            String hostAddress = byAddress.getHostAddress();
            if (byAddress.isReachable(timeOutMSec)) {
                NetListKeeper.getI().getOnLinesResolve().put(hostAddress, hostName);
                NetScanFileWorker
                    .getI()
                    .setLastStamp(System.currentTimeMillis()); ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"green\">" + hostName + FONT_BR_STR);
                stringBuffer.append(hostAddress).append(" ").append(hostName).append(PAT_IS_ONLINE);
            } else {
                NetListKeeper
                    .getI()
                    .getOffLines()
                    .put(byAddress.getHostAddress(), hostName); ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"red\">" + hostName + FONT_BR_STR);
                stringBuffer.append(hostAddress).append(" ").append(hostName);
            }
            String retStr = stringBuffer.toString(); if (retStr.contains(PAT_IS_ONLINE)) {
                printStream.println(hostAddress + " " + hostName);
                messageToUser.info("print: " , vlanFile.getName() , " = " + vlanFile.length() + ConstantsFor.STR_BYTES);
            }
            long msSpend = System.currentTimeMillis() - stArt;
            return retStr;
        }

        /**
         * Сканер локальной сети@param stStMap Запись в лог@param fromVlan начало с 3 октета IP@param toVlan   конец с 3 октета IP
         *
         * @param whatVlan первый 2 октета, с точкоё в конце.
         */
        private ConcurrentMap<String, String> scanLanSegment(int fromVlan, int toVlan, String whatVlan, PrintStream printStream) {
            @SuppressWarnings("DuplicateStringLiteralInspection") String methName = ".scanLanSegment";
            ConcurrentMap<String, String> stStMap = new ConcurrentHashMap<>(MAX_IN_VLAN_INT);
            String theScannedIPHost="No scan yet";
            for (int i = fromVlan; i < toVlan; i++) {
                StringBuilder msgBuild = new StringBuilder();
                for (int j = 0; j < MAX_IN_VLAN_INT; j++) {
                    try {
                        theScannedIPHost = oneIpScanAndPrintToFile(i, j);
                        stStMap.put(theScannedIPHost.split(" ")[0], theScannedIPHost.split(" ")[1]);
                    } catch (IOException e) {
                        String errorWrite = FileSystemWorker.error("DiapazonedScan.scanLanSegment", e);
                        stStMap.put(e.getMessage(), errorWrite);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        stStMap.put(theScannedIPHost, e.getMessage());
                    }
                }
            }
            return stStMap;
        }

        @Override public String toString() {
            final StringBuilder sb = new StringBuilder("ExecScan{"); sb
                .append("from=")
                .append(from); sb
                .append(", stArt=")
                .append(new Date(stArt)); sb
                .append(", to=")
                .append(to); sb.append('}'); return sb.toString();
        }
    }


    /**
     Скан подсетей 10.10.xx.xxx
     */
    private void scanServers() {
        for(DiapazonedScan.ExecScan r : runnablesScans){
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
        try{
            String atStr = " size in bytes: ";
            fileTimes.append(FILENAME_NEWLAN210).append(atStr).append(Paths.get(FILENAME_NEWLAN210).toFile().length()).append("<br>\n");
            fileTimes.append(FILENAME_NEWLAN200210).append(atStr).append(Paths.get(FILENAME_NEWLAN200210).toFile().length()).append("<br>\n");
            fileTimes.append(FILENAME_OLDLANTXT0).append(atStr).append(Paths.get(FILENAME_OLDLANTXT0).toFile().length()).append("<br>\n");
            fileTimes.append(FILENAME_OLDLANTXT1).append(atStr).append(Paths.get(FILENAME_OLDLANTXT1).toFile().length()).append("<br>\n");
            fileTimes.append(FILENAME_SERVTXT_11SRVTXT).append(atStr).append(Paths.get(FILENAME_SERVTXT_11SRVTXT).toFile().length()).append("<br>\n");
            fileTimes.append(FILENAME_SERVTXT_21SRVTXT).append(atStr).append(Paths.get(FILENAME_SERVTXT_21SRVTXT).toFile().length()).append("<br>\n");
            fileTimes.append(FILENAME_SERVTXT_31SRVTXT).append(atStr).append(Paths.get(FILENAME_SERVTXT_31SRVTXT).toFile().length()).append("<br>\n");
            fileTimes.append(FILENAME_SERVTXT_41SRVTXT).append(atStr).append(Paths.get(FILENAME_SERVTXT_41SRVTXT).toFile().length()).append("<br>\n");
        }catch(NullPointerException e){
            messageToUser.info("NO FILES!");
        }
        StringBuilder sb = new StringBuilder("DiapazonedScan. Start at ");
        sb.append(new Date(timeStart));
        sb.append("( ");
        sb.append(TimeUnit.MILLISECONDS.toMinutes(getRunMin()));
        sb.append(" min) ");
        sb.append("{ ");
        sb.append("<a href=\"/showalldev\">ALL_DEVICES ")
            .append(ALL_DEVICES_LOCAL_DEQUE.size())
            .append("/")
            .append(IPS_IN_VELKOM_VLAN)
            .append("(")
            .append((float) ALL_DEVICES_LOCAL_DEQUE.size() / (float) (IPS_IN_VELKOM_VLAN / 100))
            .append(" %)");
        sb.append("</a>}");
        sb.append(" ROOT_PATH_STR= ").append(ROOT_PATH_STR);
        sb.append("<br><b>\nfileTimes= </b><br>").append(fileTimes);
        return sb.toString();
    }


    private long getRunMin() {
        List<Long> timeSpend = new ArrayList<>();
        for(DiapazonedScan.ExecScan e : runnablesScans){
            timeSpend.add(e.getSpend());
        }
        return Collections.max(timeSpend);
    }


    /**
     192.168.11-14.254
     */
    @SuppressWarnings("MagicNumber")
    private void scanOldLan() {
        Runnable execScanOld0 = new DiapazonedScan.ExecScan(11 , 16 , "192.168." , new File(FILENAME_OLDLANTXT0));
        Runnable execScanOld1 = new DiapazonedScan.ExecScan(16 , 21 , "192.168." , new File(FILENAME_OLDLANTXT1));

        AppComponents.threadConfig().execByThreadConfig(execScanOld0);
        AppComponents.threadConfig().execByThreadConfig(execScanOld1);
    }
}
