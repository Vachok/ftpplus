// Copyright (c) all rights. http://networker.vachok.ru 2019.

/*
 * Copyright (c) 2019.
 */

/*
 * Copyright (c) 2019.
 */

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
import java.time.LocalTime;
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
    private static final NetScanFileWorker NET_SCAN_FILE_WORKER_INST = NetScanFileWorker.getI();
    
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
    private static final DiapazonedScan OUR_INSTANCE = new DiapazonedScan();
    
    private static final MessageToUser messageToUser = new MessageLocal(DiapazonedScan.class.getSimpleName());

    private final long stArt = ConstantsFor.getAtomicTime();
    
    private long stopClassStampLong = NetScanFileWorker.getI().getLastStamp();
    
    private List<File> srvFiles = new ArrayList<>(4);
    
    /**
     Приватный конструктор
     */
    private DiapazonedScan() {
        AppComponents.threadConfig().thrNameSet("DiaI");
    }
    
    public List<File> getSrvFiles() {
        return srvFiles;
    }
    
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiapazonedScan scan = (DiapazonedScan) o;
        return stArt == scan.stArt && stopClassStampLong == scan.stopClassStampLong && srvFiles.equals(scan.srvFiles);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(stArt, stopClassStampLong, srvFiles);
    }
    
    /**
     @return /showalldev = {@link NetScanCtr#allDevices(Model, HttpServletRequest, HttpServletResponse)}
     */
    @SuppressWarnings("StringConcatenation")
    @Override
    public String toString() {
        return theInfoToString();
    }
    
    /**
     Старт
 
     1 {@link NetScanFileWorker#setLastStamp(long)} установка текущего временного штампа. <p>
     1.1 Если {@link #srvFiles} == 4:<br>
     */
    @Override
    public void run() {
        //TODO : 21.03.2019 анализ выполнения
        NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
        if (addSrvFiles()) {
            Future<?> future = AppComponents.threadConfig().getTaskExecutor().submit(this::scanNew);
            Future<?> future1 = AppComponents.threadConfig().getTaskExecutor().submit(this::scanServers);
            long timeOut = ((9 * ConstantsNet.MAX_IN_ONE_VLAN) / 116) + ConstantsFor.DELAY;
            String classMeth = "DiapazonedScan.run";
            try {
                future.get(timeOut, TimeUnit.MINUTES);
                future1.get(timeOut, TimeUnit.MINUTES);
                NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
            }
            catch (InterruptedException | ExecutionException | TimeoutException e) {
                messageToUser.errorAlert(this.getClass().getSimpleName(), "run", e.getMessage());
                FileSystemWorker.error(classMeth, e);
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
            }
        }
        else {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
            throw new RejectedExecutionException("NO SRV-FILES!\n" + new TForms().fromArray(srvFiles, false));
        }
    }
    
    private void oneIpScan(String whatVlan, int i, int j, ConcurrentMap<String, String> stStMap, PrintWriter printStream) throws IOException {
        AppComponents.threadConfig().thrNameSet(String.valueOf(ALL_DEVICES_LOCAL_DEQUE.remainingCapacity()));
        
        int timeOutMSec = (int) ConstantsFor.DELAY;
        byte[] aBytes = InetAddress.getByName(whatVlan + i + "." + j).getAddress();
        InetAddress byAddress = InetAddress.getByAddress(aBytes);
        String byIPStr = byAddress.toString();
        String classMeth = "DiapazonedScan.oneIpScan";
        if (ConstantsFor.thisPC().equalsIgnoreCase("HOME")) {
            timeOutMSec = (int) (ConstantsFor.DELAY * 3);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
        }
        final String strOnlineIs = "Online is ";
        if (byAddress.isReachable(timeOutMSec)) {
            String hostName = byAddress.getHostName();
            String hostAddress = byAddress.getHostAddress();
            String ifAbsStr = stStMap.putIfAbsent(hostAddress, " " + hostName);
            String putIPAddrName = NetListKeeper.getI().getOnLinesResolve().put(hostAddress, hostName);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
            
            ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"green\">" + byIPStr + FONT_BR_STR);
    
            messageToUser.info(strOnlineIs + true, "ON putIPAddrName", " = " + putIPAddrName);
            messageToUser.info("stStMap", ".putIfAbsent", " = " + ifAbsStr);
        } else {
            String putIPAddrName = NetListKeeper.getI().getOffLines().put(byAddress.getHostAddress(), byAddress.getHostName());
            ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"red\">" + byIPStr + FONT_BR_STR);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
    
            messageToUser.info(strOnlineIs + false, "OFF byIPStr", " = " + byIPStr);
        }
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
    
    private boolean addSrvFiles() {
        File srvFile = new File(ConstantsNet.FILENAME_SERVTXT_11SRVTXT);
        srvFiles.add(srvFile);
        srvFile = new File(ConstantsNet.FILENAME_SERVTXT_21SRVTXT);
        srvFiles.add(srvFile);
        srvFile = new File(ConstantsNet.FILENAME_SERVTXT_31SRVTXT);
        srvFiles.add(srvFile);
        srvFile = new File(ConstantsNet.FILENAME_SERVTXT_41SRVTXT);
        srvFiles.add(srvFile);
        return srvFiles.size() == 4;
    }
    
    /**
     Добавляет в {@link ConstantsNet#getAllDevices()} адреса <i>10.200.200-217.254</i>
     */
    private void scanNew() {
        AppComponents.threadConfig().execByThreadConfig(this::scanOldLan);
        AppComponents.threadConfig().thrNameSet("ScNew");
        
        writeToFileByConditions();
        
        NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
    }
    
    /**
     192.168.11-14.254
     
     @see #scanNew()
     */
    @SuppressWarnings("MagicNumber")
    private void scanOldLan() {
        String classMeth = this.getClass().getSimpleName() + ".scanOldLan";
        File oldLANFile0 = new File(ConstantsNet.FILENAME_OLDLANTXT0);
        File oldLANFile1 = new File(ConstantsNet.FILENAME_OLDLANTXT1);
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
        NET_SCAN_FILE_WORKER_INST.setOldLanLastScan(p0.toFile(), p1.toFile());
        NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
    }
    
    /**
     Скан подсетей 10.10.xx.xxx
     */
    private void scanServers() {
        @SuppressWarnings("DuplicateStringLiteralInspection") String classMeth = "DiapazonedScan.scanServers";
        @SuppressWarnings("DuplicateStringLiteralInspection") final String methName = "scanServers";
        
        Collections.sort(srvFiles);
        String vlanIs = "10.10.";
        Collection<Runnable> runList = new ArrayBlockingQueue<>(4);
    
        Runnable srv11 = ()->{
            try {
                OutputStream outputStream = new FileOutputStream(srvFiles.get(0));
                execScan(outputStream, srvFiles.get(0), 11, 21, vlanIs);
            }
            catch (IOException e) {
                messageToUser.errorAlert(getClass().getSimpleName(), methName, e.getMessage());
                FileSystemWorker.error(getClass().getSimpleName() + ".scanServers", e);
            }
        };
        Runnable srv19 = ()->{
            try {
                OutputStream outputStream = new FileOutputStream(srvFiles.get(1));
                execScan(outputStream, srvFiles.get(1), 21, 31, vlanIs);
            }
            catch (IOException e) {
                messageToUser.errorAlert(getClass().getSimpleName(), methName, e.getMessage());
                FileSystemWorker.error(classMeth, e);
            }
        };
        Runnable srv27 = ()->{
            try {
                OutputStream outputStream = new FileOutputStream(srvFiles.get(2));
                execScan(outputStream, srvFiles.get(2), 31, 41, vlanIs);
            }
            catch (IOException e) {
                messageToUser.errorAlert(getClass().getSimpleName(), methName, e.getMessage());
                FileSystemWorker.error(classMeth, e);
            }
        };
        Runnable srv41 = ()->{
            try {
                OutputStream outputStream = new FileOutputStream(srvFiles.get(3));
                execScan(outputStream, srvFiles.get(3), 41, 51, vlanIs);
            }
            catch (IOException e) {
                messageToUser.errorAlert(getClass().getSimpleName(), methName, e.getMessage());
                FileSystemWorker.error(classMeth, e);
            }
        };
        runList.add(srv11);
        runList.add(srv19);
        runList.add(srv27);
        runList.add(srv41);
        
        runList.iterator().forEachRemaining(this::execRunServersScanRunnables);
        
        NET_SCAN_FILE_WORKER_INST.setSrvFiles(srvFiles);
        messageToUser.warn(classMeth, "srvFiles", " = " + new TForms().fromArray(srvFiles, false));
    }
    
    private void execRunServersScanRunnables(Runnable runnableScan) {
        AppComponents.threadConfig().execByThreadConfig(runnableScan);
        messageToUser.info("DiapazonedScan.execRunServersScanRunnables", "runnableScan", " = " + runnableScan);
        NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
    }
    
    
    @SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed"})
    private void writeToFileByConditions() {
        File newLanFile200 = new File(ConstantsNet.FILENAME_AVAILABLELAST200210TXT);
        File newLanFile210 = new File(ConstantsNet.FILENAME_AVAILABLELAST210220TXT);
        final String vlanIs = "10.200.";
        
        NET_SCAN_FILE_WORKER_INST.setNewLanLastScan(newLanFile200, newLanFile210);
    
        final String classMeth = "DiapazonedScan.writeToFileByConditions";
        if (ALL_DEVICES_LOCAL_DEQUE.remainingCapacity() == 0) {
            ALL_DEVICES_LOCAL_DEQUE.clear();
            messageToUser.info(classMeth, "toString is", " = " + this);
        }
        try {
            OutputStream outputStream210 = new FileOutputStream(ConstantsNet.FILENAME_AVAILABLELAST200210TXT);
            OutputStream outputStream220 = new FileOutputStream(ConstantsNet.FILENAME_AVAILABLELAST210220TXT);
    
            final String methName = "writeToFileByConditions";
            AppComponents.threadConfig().execByThreadConfig(()->{
                try {
                    execScan(outputStream210, newLanFile200, 200, 210, vlanIs);
                }
                catch (IOException e) {
                    messageToUser.errorAlert(getClass().getSimpleName(), methName, e.getMessage());
                    FileSystemWorker.error(classMeth, e);
                }
            });
            AppComponents.threadConfig().execByThreadConfig(()->{
                try {
                    execScan(outputStream220, newLanFile210, 210, 218, vlanIs);
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
            
            fileTimes.append(ConstantsNet.FILENAME_AVAILABLELAST200210TXT).append(ConstantsNet.FILENAME_AVAILABLELAST210220TXT).append(atStr);
            fileTimes.append(Paths.get(ConstantsNet.FILENAME_AVAILABLELAST200210TXT).toFile().length());
            fileTimes.append(Paths.get(ConstantsNet.FILENAME_AVAILABLELAST210220TXT).toFile().length()).append("<br>\n");
            fileTimes.append(ConstantsNet.FILENAME_OLDLANTXT0).append(", ");
            fileTimes.append(ConstantsNet.FILENAME_OLDLANTXT1).append(atStr);
            fileTimes.append(Paths.get(ConstantsNet.FILENAME_OLDLANTXT0).toFile().length()).append("<br>\n");
            fileTimes.append(Paths.get(ConstantsNet.FILENAME_OLDLANTXT1).toFile().length()).append("<br>\n");
            
            srvFiles.forEach(x->fileTimes.append(x.getName()).append(atStr).append(x.length()).append("<br>\n"));
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
     Сканер сегментов в подсетях 10.10.x.x
     
     @since 06.03.2019 (2:46)
     @deprecated since 20.03.2019 (19:44)
     */
    @SuppressWarnings("CyclicClassDependency")
    @Deprecated
    private class Vlans1010ToScan implements Runnable {
        
        
        private static final String VLAN = "10.10";
        
        private File srvFile;
        
        private int toVlan;
        
        private int fromVlan;
        
        Vlans1010ToScan(File srvFile, int fromVlan, int toVlan) {
            this.srvFile = srvFile;
            this.fromVlan = fromVlan;
            this.toVlan = toVlan;
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
        }
        
        @Override
        public void run() {
            AppComponents.threadConfig().thrNameSet("vlans:" + fromVlan + "-" + toVlan);
            srvFiles.add(srvFile);
            if (srvFiles.size() == 4) {
                cpFile();
            }
        }
        
        private void cpFile() {
            String classMeth = "Vlans1010ToScan.cpFile";
            srvFiles.forEach(x->{
                boolean isFileCopied = FileSystemWorker.copyOrDelFile(x, x.getAbsolutePath().replace(".txt", "_" + LocalTime.now().toSecondOfDay() + ".scan"), false);
                messageToUser.warn(classMeth, x.getName(), " Copy = " + isFileCopied);
            });
            srvFiles.clear();
            stopClassStampLong = System.currentTimeMillis();
            NET_SCAN_FILE_WORKER_INST.setSrvFiles(srvFiles);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
        }
    }
}
