// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;



import org.springframework.ui.Model;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
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
import java.util.concurrent.*;


/**
 Скан диапазона адресов

 @since 19.12.2018 (11:35) */
@SuppressWarnings({"ClassWithMultipleLoggers", "MagicNumber", "resource", "IOResourceOpenedButNotSafelyClosed"})
public class DiapazonedScan implements Runnable {


    /**
     {@link NetScanFileWorker#getI()}
     */
    private static final NetScanFileWorker NET_SCAN_FILE_WORKER_INST = NetScanFileWorker.getI(); // TODO: 23.03.2019 fin? static?

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

    /**
     Singleton inst
     */
    private static DiapazonedScan OUR_INSTANCE = new DiapazonedScan();

    private static final MessageToUser messageToUser = new MessageLocal(DiapazonedScan.class.getSimpleName());

    private final long stArt = ConstantsFor.getAtomicTime();

    private long stopClassStampLong = NetScanFileWorker.getI().getLastStamp();

    private Map<String, File> srvFiles = new ConcurrentHashMap<>();

    /**
     Приватный конструктор
     */
    private DiapazonedScan() {
        AppComponents.threadConfig().thrNameSet("DScanF:" + srvFiles.size());
    }


    public Map<String, File> getSrvFiles() throws NullPointerException {
        return NetScanFileWorker.srvFiles;
    }

    public long getStopClassStampLong() {
        return stopClassStampLong;
    }

    @Override
    public void run() {
        NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
        String classMeth = "DiapazonedScan.run";
        int stringFileMap = addFiles();
        AppComponents.threadConfig().thrNameSet(String.valueOf(stringFileMap));
        if (NetScanFileWorker.srvFiles.size() <= 0) {
            throw new RejectedExecutionException("No Files in " + DiapazonedScan.class.getSimpleName() + " srvFiles");
        } else {
            AppComponents.threadConfig().execByThreadConfig(this::theNewLan);
            AppComponents.threadConfig().execByThreadConfig(this::scanServers);
            AppComponents.threadConfig().execByThreadConfig(this::scanOldLan);
        }
    }

    /**
     * SINGLETON
     *
     * @return single.
     */
    public static DiapazonedScan getInstance() {
        messageToUser.info(DiapazonedScan.OUR_INSTANCE.getClass().getSimpleName(), " is initializing, and trying to " +
                "addFiles in ",
            String.valueOf(OUR_INSTANCE.addFiles()));
        return OUR_INSTANCE;
    }

    /**
     * @return /showalldev = {@link NetScanCtr#allDevices(Model , HttpServletRequest , HttpServletResponse)}
     */
    @SuppressWarnings("StringConcatenation")
    @Override
    public String toString() {
        return theInfoToString();
    }

    /**
     * Пингует в 200х VLANах девайсы с 10.200.x.250 по 10.200.x.254
     * <p>
     * Свичи начала сегментов. Вкл. в оптическое ядро.
     *
     * @return лист важного оборудования
     *
     * @throws IllegalAccessException swF.get(swF).toString()
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

    private void oneIpScan(String whatVlan, int i, int j, ConcurrentMap<String, String> stStMap, PrintWriter printStream) throws IOException {
        AppComponents.threadConfig().thrNameSet(String.valueOf(ALL_DEVICES_LOCAL_DEQUE.remainingCapacity())); String classMeth = "DiapazonedScan.oneIpScan"; String strOnlineIs = "Online is ";

        int timeOutMSec = (int) ConstantsFor.DELAY;
        byte[] aBytes = InetAddress.getByName(whatVlan + i + "." + j).getAddress();
        InetAddress byAddress = InetAddress.getByAddress(aBytes);
        String byIPStr = byAddress.toString();

        if (ConstantsFor.thisPC().equalsIgnoreCase("HOME")) {
            timeOutMSec = (int) (ConstantsFor.DELAY * 3);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
        }
        if (byAddress.isReachable(timeOutMSec)) {
            String hostName = byAddress.getHostName();
            String hostAddress = byAddress.getHostAddress();
            String ifAbsStr = stStMap.putIfAbsent(hostAddress, " " + hostName);
            String putIPAddrName = NetListKeeper.getI().getOnLinesResolve().put(hostAddress, hostName);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());

            ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"green\">" + byIPStr + FONT_BR_STR);

            messageToUser.info(strOnlineIs + true, "ON putIPAddrName", " = " + putIPAddrName);
            messageToUser.info("stStMap", ".putIfAbsent", " = " + ifAbsStr);
        }
        else {
            String putIPAddrName = NetListKeeper.getI().getOffLines().put(byAddress.getHostAddress(), byAddress.getHostName());
            ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"red\">" + byIPStr + FONT_BR_STR);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());

            messageToUser.info(strOnlineIs + false, "OFF byIPStr" , " = " + byIPStr);
        }
    }


    private int addFiles() {

        srvFiles.putIfAbsent("200-210" , new File(ConstantsNet.FILENAME_AVAILABLELAST_200210_TXT));
        srvFiles.putIfAbsent("210-220" , new File(ConstantsNet.FILENAME_AVAILABLELAST210220TXT));

        srvFiles.putIfAbsent("11" , new File(ConstantsNet.FILENAME_SERVTXT_11SRVTXT));
        srvFiles.putIfAbsent("21" , new File(ConstantsNet.FILENAME_SERVTXT_21SRVTXT));
        srvFiles.putIfAbsent("31" , new File(ConstantsNet.FILENAME_SERVTXT_31SRVTXT));
        srvFiles.putIfAbsent("31" , new File(ConstantsNet.FILENAME_SERVTXT_41SRVTXT));

        srvFiles.putIfAbsent("old0" , new File(ConstantsNet.FILENAME_OLDLANTXT0));
        srvFiles.putIfAbsent("old1" , new File(ConstantsNet.FILENAME_OLDLANTXT1));
        return srvFiles.size();
    }

    /**
     Сканер локальной сети@param stStMap Запись в лог@param fromVlan начало с 3 октета IP

     @param toVlan конец с 3 октета IP
     @param whatVlan первый 2 октета, с точкоё в конце.
     @param printWriter {@link PrintWriter}
     */
    @SuppressWarnings({"MethodWithMultipleLoops", "ObjectAllocationInLoop"})
    private void scanLan(int fromVlan, int toVlan, String whatVlan, PrintWriter printWriter) {
        for (int i = fromVlan; i < toVlan; i++) {
            StringBuilder msgBuild = new StringBuilder();
            ConcurrentMap<String, String> stStMap = new ConcurrentHashMap<>(MAX_IN_VLAN_INT);
            for (int j = 0; j < MAX_IN_VLAN_INT; j++) {
                try {
                    oneIpScan(whatVlan, i, j, stStMap, printWriter);
                    if (stStMap.size() > 0) {
                        printWriter.println(new TForms().fromArray(stStMap, false));
                        msgBuild.append("\n").append(printWriter.hashCode()).append(" printStream.hashCode");
                        msgBuild.append("\n").append(new TForms().fromArray(NET_SCAN_FILE_WORKER_INST.getFilesScan(), false));
                        String msg = msgBuild.toString();
                        messageToUser.warn(msg);
                    }
                    NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
                } catch (IOException e) {
                    FileSystemWorker.error("DiapazonedScan.scanLan", e);
                }
            }
        }
    }


    /**
     192.168.11-14.254
     */
    @SuppressWarnings("MagicNumber")
    private void scanOldLan() {
        String classMeth = this.getClass().getSimpleName() + ".scanOldLan";
        File oldLANFile0 = NetScanFileWorker.srvFiles.get("old0");
        File oldLANFile1 = NetScanFileWorker.srvFiles.get("old1");
        Path p0 = Paths.get(ROOT_PATH_STR + "\\lan\\0_192_" + System.currentTimeMillis() / 1000 + ".scan");
        Path p1 = Paths.get(ROOT_PATH_STR + "\\lan\\1_192_" + System.currentTimeMillis() / 1000 + ".scan");

        try {
            OutputStream outputStream = new FileOutputStream(oldLANFile0);
            OutputStream outputStream1 = new FileOutputStream(oldLANFile1);
            AppComponents.threadConfig().execByThreadConfig(()->{
                try {
                    execScan(outputStream, oldLANFile0, 11, 15, "192.168.");
                }
                catch (IOException e) {
                    FileSystemWorker.error(classMeth, e);
                }
            });
            AppComponents.threadConfig().execByThreadConfig(()->{
                try {
                    execScan(outputStream1, oldLANFile1, 15, 21, "192.168.");
                }
                catch (IOException e) {
                    FileSystemWorker.error(classMeth, e);
                }
            });
        } catch (IOException e) {
            messageToUser.errorAlert(getClass().getSimpleName(), "scanOldLan", e.getMessage());
            FileSystemWorker.error(classMeth, e);
        }
        boolean isFileCopied0 = FileSystemWorker.copyOrDelFile(oldLANFile0, p0.toAbsolutePath().toString(), false);
        boolean isFileCopied1 = FileSystemWorker.copyOrDelFile(oldLANFile1, p1.toAbsolutePath().toString(), false);
        NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
    }


    private void execRunServersScanRunnables( Runnable runnableScan ) {
        AppComponents.threadConfig().execByThreadConfig(runnableScan);
        messageToUser.info("DiapazonedScan.execRunServersScanRunnables" , "runnableScan" , " = " + runnableScan);
        NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
    }


    /**
     Скан подсетей 10.10.xx.xxx
     */
    private void scanServers() {
        @SuppressWarnings("DuplicateStringLiteralInspection") String classMeth = "DiapazonedScan.scanServers";
        @SuppressWarnings("DuplicateStringLiteralInspection") final String methName = "scanServers";


        String vlanIs = "10.10.";
        Collection<Runnable> runList = new ArrayBlockingQueue<>(4);

        Runnable srv11 = ()->{
            try {
                File file = NetScanFileWorker.srvFiles.get("11");
                OutputStream outputStream = new FileOutputStream(file);
                execScan(outputStream , NetScanFileWorker.srvFiles.get("11") , 11 , 21 , vlanIs);
            }
            catch (IOException e) {
                messageToUser.errorAlert(getClass().getSimpleName(), methName, e.getMessage());
                FileSystemWorker.error(getClass().getSimpleName() + ".scanServers", e);
            }
        };
        Runnable srv19 = ()->{
            try {
                File file = NetScanFileWorker.srvFiles.get("21");
                OutputStream outputStream = new FileOutputStream(file);
                execScan(outputStream , NetScanFileWorker.srvFiles.get("21") , 21 , 31 , vlanIs);
            }
            catch (IOException e) {
                messageToUser.errorAlert(getClass().getSimpleName(), methName, e.getMessage());
                FileSystemWorker.error(getClass().getSimpleName() + ".scanServers" , e);
            }
        };
        Runnable srv27 = ()->{
            try {
                File file = NetScanFileWorker.srvFiles.get("31");
                OutputStream outputStream = new FileOutputStream(file);
                execScan(outputStream , NetScanFileWorker.srvFiles.get("31") , 31 , 41 , vlanIs);
            }
            catch (IOException e) {
                messageToUser.errorAlert(getClass().getSimpleName(), methName, e.getMessage());
                FileSystemWorker.error(getClass().getSimpleName() + ".scanServers" , e);
            }
        };
        Runnable srv41 = ()->{
            try {
                File file = NetScanFileWorker.srvFiles.get("31");
                OutputStream outputStream = new FileOutputStream(file);
                execScan(outputStream , NetScanFileWorker.srvFiles.get("31") , 41 , 51 , vlanIs);
            }
            catch (IOException e) {
                messageToUser.errorAlert(getClass().getSimpleName(), methName, e.getMessage());
                FileSystemWorker.error(getClass().getSimpleName() + ".scanServers" , e);
            }
        };
        runList.add(srv11);
        runList.add(srv19);
        runList.add(srv27);
        runList.add(srv41);

        runList.iterator().forEachRemaining(this::execRunServersScanRunnables);


        messageToUser.warn(classMeth , "srvFiles" , " = " + new TForms().fromArray(NetScanFileWorker.srvFiles , false));
    }


    @SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed"})
    private void theNewLan() {
        String vlanIs = "10.200.";
        String classMeth = "DiapazonedScan.theNewLan";

        if (ALL_DEVICES_LOCAL_DEQUE.remainingCapacity() == 0) {
            ALL_DEVICES_LOCAL_DEQUE.clear();
            messageToUser.info(classMeth, "toString is", " = " + this);
        }
        try (OutputStream outputStream200 = new FileOutputStream(NetScanFileWorker.srvFiles.get("200")); OutputStream outputStream210 = new FileOutputStream(NetScanFileWorker.srvFiles.get("210"))) {
            String methName = "theNewLan";
            AppComponents.threadConfig().execByThreadConfig(()->{
                try {
                    execScan(outputStream200 , NetScanFileWorker.srvFiles.get("200") , 200 , 210 , vlanIs);
                }
                catch (IOException e) {
                    messageToUser.errorAlert(getClass().getSimpleName(), methName, e.getMessage());
                    FileSystemWorker.error(classMeth, e);
                }
            });
            AppComponents.threadConfig().execByThreadConfig(()->{
                try {
                    execScan(outputStream210 , NetScanFileWorker.srvFiles.get("210") , 210 , 218 , vlanIs);
                }
                catch (IOException e) {
                    messageToUser.errorAlert(getClass().getSimpleName(), methName, e.getMessage());
                    FileSystemWorker.error(classMeth, e);
                }
            });
        }
        catch (IOException e) {
            FileSystemWorker.error(classMeth, e);
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }


    private void execScan(OutputStream outputStream, File lanFileToWrite, int from, int to, String whatVlan) throws IOException {
        PrintWriter printWriter = new PrintWriter(outputStream, true);
        scanLan(from, to, whatVlan, printWriter);
        Path p210 = Paths.get(".\\lan\\" + lanFileToWrite.getName().replace(".txt", "_") + System.currentTimeMillis() / 1000 + ".scan");
        NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
        outputStream.close();
        FileSystemWorker.copyOrDelFile(lanFileToWrite, p210.toAbsolutePath().toString(), false);

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

            fileTimes.append(ConstantsNet.FILENAME_AVAILABLELAST_200210_TXT).append(ConstantsNet.FILENAME_AVAILABLELAST210220TXT).append(atStr);
            fileTimes.append(Paths.get(ConstantsNet.FILENAME_AVAILABLELAST_200210_TXT).toFile().length());
            fileTimes.append(Paths.get(ConstantsNet.FILENAME_AVAILABLELAST210220TXT).toFile().length()).append("<br>\n");
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
}
