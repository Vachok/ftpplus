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
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;


/**
 Скан диапазона адресов
 
 @since 19.12.2018 (11:35) */
@SuppressWarnings({"ClassWithMultipleLoggers", "MagicNumber"})
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
    
    private static final String STR_IS_FILE_COPIED = "isFileCopied";
    
    private static final String METH_SCAN_SERVERS = "scanServers";
    
    private final long stArt = ConstantsFor.getAtomicTime();
    
    private long stopClassStampLong = stArt;
    
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
     */
    @Override
    public void run() {
        if (addSrvFiles()) {
            Future<?> future = AppComponents.threadConfig().getTaskExecutor().submit(this::scanNew);
            Future<?> future1 = AppComponents.threadConfig().getTaskExecutor().submit(this::scanServers);
            long timeOut = ((9 * ConstantsNet.MAX_IN_ONE_VLAN) / 116) + ConstantsFor.DELAY;
            String classMeth = "DiapazonedScan.run";
            try {
                future.get(timeOut, TimeUnit.MINUTES);
                future1.get(timeOut, TimeUnit.MINUTES);
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
            throw new RejectedExecutionException("NO SRV-FILES!\n" + new TForms().fromArray(srvFiles, false));
        }
    }
    
    private void ipScan(String whatVlan, int i, int j, ConcurrentMap<String, String> stStMap, PrintStream printStream) throws IOException {
        int timeOutMSec = (int) ConstantsFor.DELAY;
        byte[] aBytes = InetAddress.getByName(whatVlan + i + "." + j).getAddress();
        InetAddress byAddress = InetAddress.getByAddress(aBytes);
        String byIPStr = byAddress.toString();
        String classMeth = "DiapazonedScan.ipScan";
        if (ConstantsFor.thisPC().equalsIgnoreCase("HOME")) {
            timeOutMSec = (int) (ConstantsFor.DELAY * 3);
        }
        
        if (byAddress.isReachable(timeOutMSec)) {
            String hostName = byAddress.getHostName();
            String hostAddress = byAddress.getHostAddress();
            String ifAbsStr = stStMap.putIfAbsent(hostName, " " + hostAddress);
            printStream.println(ifAbsStr);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
            
            ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"green\">" + byIPStr + FONT_BR_STR);
            
            messageToUser.warn("stStMap", "putIfAbsent", " = " + ifAbsStr);
        } else {
            AppComponents.threadConfig().thrNameSet(ALL_DEVICES_LOCAL_DEQUE.size() + "/" + ConstantsNet.IPS_IN_VELKOM_VLAN);
            ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"red\">" + byIPStr + FONT_BR_STR);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
            messageToUser.info(classMeth, "byIPStr", " = " + byIPStr);
        }
        messageToUser.warn("DiapazonedScan.ipScan. EXIT METHOD.", "stStMap", " = " + stStMap.size());
    }
    
    /**
     Сканер локальной сети@param stStMap Запись в лог
     @param fromVlan начало с 3 октета IP
     @param toVlan конец с 3 октета IP
     @param whatVlan первый 2 октета, с точкоё в конце.
     @param printStream {@link PrintStream}
 
     */
    @SuppressWarnings({"MethodWithMultipleLoops", "ObjectAllocationInLoop"})
    private void scanLan(int fromVlan, int toVlan, String whatVlan, PrintStream printStream) {
        for (int i = fromVlan; i < toVlan; i++) {
            StringBuilder msgBuild = new StringBuilder();
            ConcurrentMap<String, String> stStMap = new ConcurrentHashMap<>(MAX_IN_VLAN_INT);
            for (int j = 0; j < MAX_IN_VLAN_INT; j++) {
                try {
                    ipScan(whatVlan, i, j, stStMap, printStream);
                } catch (IOException e) {
                    FileSystemWorker.error("DiapazonedScan.scanLan", e);
                }
            }
            printStream.println(new TForms().fromArray(stStMap, false));
            msgBuild.append(i).append(" was i. Total time: ").append(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - stArt)).append("min\n").append(ALL_DEVICES_LOCAL_DEQUE.size()).append(" ALL_DEVICES.size()");
            String msg = msgBuild.toString();
            messageToUser.warn(msg);
        }
    }
    
    private boolean addSrvFiles() {
        File srvFile = new File(ConstantsNet.FILENAME_SERVTXT_11SRVTXT);
        srvFiles.add(srvFile);
        try {
            Thread.sleep(ConstantsFor.DELAY / 2);
        }
        catch (InterruptedException e) {
            messageToUser.error(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        
        srvFile = new File(ConstantsNet.FILENAME_SERVTXT_21SRVTXT);
        srvFiles.add(srvFile);
        try {
            Thread.sleep(ConstantsFor.DELAY / 2);
        }
        catch (InterruptedException e) {
            messageToUser.error(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        
        srvFile = new File(ConstantsNet.FILENAME_SERVTXT_31SRVTXT);
        srvFiles.add(srvFile);
        try {
            Thread.sleep(ConstantsFor.DELAY / 2);
        }
        catch (InterruptedException e) {
            messageToUser.error(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        
        srvFile = new File(ConstantsNet.FILENAME_SERVTXT_41SRVTXT);
        srvFiles.add(srvFile);
        try {
            Thread.sleep(ConstantsFor.DELAY / 2);
        }
        catch (InterruptedException e) {
            messageToUser.error(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        return srvFiles.size() == 4;
    }
    
    /**
     Добавляет в {@link ConstantsNet#getAllDevices()} адреса <i>10.200.200-217.254</i>
     */
    private void scanNew() {
        AppComponents.threadConfig().execByThreadConfig(this::scanOldLan);
        AppComponents.threadConfig().thrNameSet("ScNew");
        writeToFileByConditions();
        stopClassStampLong = System.currentTimeMillis();
    }
    
    /**
     192.168.11-14.254
     
     @see #scanNew()
     */
    @SuppressWarnings("MagicNumber")
    private void scanOldLan() {
        String classMeth = this.getClass().getSimpleName() + ".scanOldLan";
        File oldLANFile = new File(ConstantsNet.FILENAME_OLDLANTXT);
        Path p = Paths.get(ROOT_PATH_STR + "\\lan\\192_" + System.currentTimeMillis() / 1000 + ".scan");
        try (OutputStream outputStream = new FileOutputStream(oldLANFile); PrintStream printStream = new PrintStream(outputStream, true)) {
            scanLan(11, 15, "192.168.", printStream);
        
            scanLan(15, 21, "192.168.", printStream);
        } catch (IOException e) {
            messageToUser.errorAlert(getClass().getSimpleName(), "scanOldLan", e.getMessage());
            FileSystemWorker.error(classMeth, e);
        }
        boolean isFileCopied = FileSystemWorker.copyOrDelFile(oldLANFile, p.toAbsolutePath().toString(), false);
        NET_SCAN_FILE_WORKER_INST.setOldLanLastScan(p.toFile());
        messageToUser.info(classMeth, "p.toAbsolutePath()", p.toAbsolutePath().toString());
        messageToUser.info(classMeth, STR_IS_FILE_COPIED, String.valueOf(isFileCopied));
        stopClassStampLong = System.currentTimeMillis();
    }
    
    /**
     Скан подсетей 10.10.xx.xxx
     */
    private void scanServers() {
        String classMeth = "DiapazonedScan.scanServers";
        Collections.sort(srvFiles);
        Collection<Runnable> runList = new ArrayBlockingQueue<>(4);
        Runnable srv11 = new Vlans1010ToScan(srvFiles.get(0), 11, 21);
        Runnable srv19 = new Vlans1010ToScan(srvFiles.get(1), 21, 31);
        Runnable srv27 = new Vlans1010ToScan(srvFiles.get(2), 31, 41);
        Runnable srv41 = new Vlans1010ToScan(srvFiles.get(3), 41, 51);
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
    }
    
    
    private void writeToFileByConditions() {
        File newLanFile200 = new File(ConstantsNet.FILENAME_AVAILABLELAST200210TXT);
        File newLanFile210 = new File(ConstantsNet.FILENAME_AVAILABLELAST210220TXT);
        
        NET_SCAN_FILE_WORKER_INST.setNewLanLastScan(newLanFile200, newLanFile210);
        
        if (ALL_DEVICES_LOCAL_DEQUE.remainingCapacity() == 0) {
            ALL_DEVICES_LOCAL_DEQUE.clear();
            messageToUser.info("DiapazonedScan.writeToFileByConditions", "toString is", " = " + this);
        }
        try (OutputStream outputStream210 = new FileOutputStream(ConstantsNet.FILENAME_AVAILABLELAST200210TXT); OutputStream outputStream220 = new FileOutputStream(ConstantsNet.FILENAME_AVAILABLELAST210220TXT)) {
            
            Runnable execScan200210 = ()->{
                scanLan(200, 210, "10.200.", new PrintStream(outputStream210, true, Charset.defaultCharset()));
                Path p200 = Paths.get(".\\lan\\200_" + System.currentTimeMillis() / 1000 + ".scan");
                FileSystemWorker.copyOrDelFile(newLanFile200, p200.toAbsolutePath().toString(), false);
            };
            Runnable execScan210220 = ()->{
                scanLan(210, 220, "10.200.", new PrintStream(outputStream220, true, Charset.defaultCharset()));
                Path p210 = Paths.get(".\\lan\\210_" + System.currentTimeMillis() / 1000 + ".scan");
                FileSystemWorker.copyOrDelFile(newLanFile210, p210.toAbsolutePath().toString(), false);
            };
            
            boolean isExecByThreadConfig = AppComponents.threadConfig().execByThreadConfig(execScan200210);
            messageToUser.warn("200-210", "isExecByThreadConfig", " = " + isExecByThreadConfig);
            
            Thread.sleep(ConstantsFor.DELAY * 10);
            
            isExecByThreadConfig = AppComponents.threadConfig().execByThreadConfig(execScan210220);
            messageToUser.warn("200-210", "isExecByThreadConfig", " = " + isExecByThreadConfig);
        }
        catch (IOException | InterruptedException e) {
            FileSystemWorker.error("DiapazonedScan.writeToFileByConditions", e);
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
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
    
            fileTimes.append(ConstantsNet.FILENAME_AVAILABLELAST200210TXT).append(ConstantsNet.FILENAME_AVAILABLELAST210220TXT).append(atStr).append(Paths.get(ConstantsNet.FILENAME_AVAILABLELAST200210TXT).toFile().length()).append(Paths.get(ConstantsNet.FILENAME_AVAILABLELAST210220TXT).toFile().length()).append("<br>\n").append(ConstantsNet.FILENAME_OLDLANTXT).append(atStr).append(Paths.get(ConstantsNet.FILENAME_OLDLANTXT).toFile().length()).append("<br>\n");
    
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
     */
    @SuppressWarnings("CyclicClassDependency")
    private class Vlans1010ToScan implements Runnable {
    
    
        private File srvFile;
        
        private int toVlan;
        
        private int fromVlan;
    
        Vlans1010ToScan(File srvFile, int fromVlan, int toVlan) {
            this.srvFile = srvFile;
            this.fromVlan = fromVlan;
            this.toVlan = toVlan;
        }
    
        @Override
        public void run() {
            AppComponents.threadConfig().thrNameSet("vlans:" + fromVlan + "-" + toVlan);
            srvFiles.add(srvFile);
    
            try (OutputStream outputStream = new FileOutputStream(this.srvFile); PrintStream printStream = new PrintStream(outputStream, true)) {
                Map<String, String> stringMap = new ConcurrentHashMap<>();
                scanLan(fromVlan, toVlan, "10.10.", printStream);
            } catch (IOException e) {
                messageToUser.errorAlert(getClass().getSimpleName(), METH_SCAN_SERVERS, e.getMessage());
                FileSystemWorker.error("run", e);
            }
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
        }
    }
}
