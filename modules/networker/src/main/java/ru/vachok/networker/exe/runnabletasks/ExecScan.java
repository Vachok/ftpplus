package ru.vachok.networker.exe.runnabletasks;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.TForms;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetListKeeper;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.vachok.networker.net.enums.ConstantsNet.MAX_IN_ONE_VLAN;


/**
 Да запуска скана из {@link DiapazonScan}
 
 @since 24.03.2019 (16:01) */
public class ExecScan extends DiapazonScan implements Runnable {
    
    
    private static final String PAT_IS_ONLINE = " is online";
    
    private static final String FONT_BR_CLOSE = "</font><br>";
    
    private static final Pattern COMPILE = Pattern.compile("\\Q.txt\\E", Pattern.LITERAL);
    
    private final MessageToUser messageToUser = new MessageLocal(ExecScan.class.getSimpleName());
    
    private final Preferences preferences = Preferences.userRoot();
    
    private final File vlanFile;
    
    private final ThreadConfig threadConfig = AppComponents.threadConfig();
    
    private long stArt;
    
    private int from;
    
    private int to;
    
    private String whatVlan;
    
    private PrintStream printStream;
    
    public ExecScan(int from, int to, String whatVlan, File vlanFile) {
        
        this.from = from;
        
        this.to = to;
        
        this.whatVlan = whatVlan;
        
        this.vlanFile = vlanFile;
    
        this.stArt = LocalDateTime.of(1984, 1, 7, 2, 0).toEpochSecond(ZoneOffset.ofHours(3)) * 1000;
    }
    
    @Override
    public void run() {
        if (vlanFile.exists()) {
            String newFileName = vlanFile.getAbsolutePath()
                .replace(vlanFile.getName(), "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + COMPILE.matcher(vlanFile.getName())
                    .replaceAll(Matcher.quoteReplacement("_" + (System.currentTimeMillis() / 1000))) + ".scan");
            boolean copyFile = FileSystemWorker.copyOrDelFile(vlanFile, newFileName, true);
            messageToUser.info(vlanFile.getName() + " copied to ", newFileName, " = " + copyFile);
        }
        OutputStream outputStream = null;
        try {
            //noinspection resource,IOResourceOpenedButNotSafelyClosed
            outputStream = new FileOutputStream(vlanFile);
        }
        catch (FileNotFoundException e) {
            messageToUser.errorAlert(getClass().getSimpleName(), ".ExecScan", e.getMessage());
        }
        this.printStream = new PrintStream(Objects.requireNonNull(outputStream), true);
        if (ALL_DEVICES_LOCAL_DEQUE.remainingCapacity() > 0) {
            boolean execScanB = execScan();
            messageToUser.info("ALL_DEV", "Scan from " + from + " to " + to + " is " + execScanB, "ALL_DEVICES_LOCAL_DEQUE = " + ALL_DEVICES_LOCAL_DEQUE.size());
        }
        else {
            messageToUser.error(getClass().getSimpleName(), String.valueOf(ALL_DEVICES_LOCAL_DEQUE.remainingCapacity()), " ALL_DEVICES_LOCAL_DEQUE remainingCapacity!");
        }
    }
    
    private long setSpend() throws IOException {
        long spendMS = System.currentTimeMillis() - stArt;
        try {
            preferences.sync();
            preferences.putLong(getClass().getSimpleName(), spendMS);
            preferences.sync();
        }
        catch (BackingStoreException e) {
            InitProperties initProperties = new FileProps(ConstantsFor.PROPS_FILE_JAVA_ID);
            Properties fileProps = initProperties.getProps();
            fileProps.setProperty(getClass().getSimpleName(), String.valueOf(spendMS));
            fileProps.store(new FileOutputStream(ConstantsFor.PROPS_FILE_JAVA_ID), getClass().getSimpleName() + ".setSpend");
        }
        return spendMS;
    }
    
    private String getBeansInfo() {
        final StringBuilder sb = new StringBuilder();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        
        sb.append("<br>");
        sb.append(runtimeMXBean.getName()).append(" Name. ");
        sb.append(runtimeMXBean.getUptime()).append(" Time. ");
        sb.append("<br>");
        sb.append("<br>");
        
        ThreadInfo infoThisThr = threadMXBean.getThreadInfo(Thread.currentThread().getId());
        sb.append(infoThisThr).append(" String. ");
        sb.append(infoThisThr.getThreadName()).append(" ThreadName. ");
        sb.append(infoThisThr.getWaitedTime()).append(" WaitedTime (millis). ");
        sb.append(infoThisThr.getThreadState()).append(" current state. ");
        sb.append(TimeUnit.NANOSECONDS.toMillis(threadMXBean.getCurrentThreadCpuTime())).append(" current thread CPU time in millis. ");
        
        return sb.toString();
    }
    
    private boolean execScan() {
        this.stArt = System.currentTimeMillis();
        try {
            ConcurrentMap<String, String> stringStringConcurrentMap = scanLanSegment(from, to);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
            new ExitApp(from + "-" + to + ".map", stringStringConcurrentMap).writeOwnObject();
            return true;
        }
        catch (Exception e) {
            messageToUser.error(e.getMessage());
            return false;
        }
    }
    
    /**
     @param iThree третий октет vlan
     @param jFour четвертый октет vlan
     @return Example: {@code 192.168.11.0 192.168.11.0} or {@code 10.200.200.1 10.200.200.1 is online}
     
     @throws IOException при записи файла
     */
    private String oneIpScanAndPrintToFile(int iThree, int jFour) throws IOException {
        threadConfig.thrNameSet(String.valueOf(iThree));
        
        int timeOutMSec = (int) ConstantsFor.DELAY;
        byte[] aBytes = InetAddress.getByName(whatVlan + iThree + "." + jFour).getAddress();
        StringBuilder stringBuilder = new StringBuilder();
        InetAddress byAddress = InetAddress.getByAddress(aBytes);
        String hostName = byAddress.getHostName();
        String hostAddress = byAddress.getHostAddress();
        
        if (ConstantsFor.thisPC().equalsIgnoreCase("HOME")) {
            timeOutMSec = (int) (ConstantsFor.DELAY * 2);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
        }
        
        if (byAddress.isReachable(timeOutMSec)) {
            NetListKeeper.getI().getOnLinesResolve().put(hostAddress, hostName);
            
            ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"green\">" + hostName + FONT_BR_CLOSE);
            stringBuilder.append(hostAddress).append(" ").append(hostName).append(PAT_IS_ONLINE);
        }
        else {
            NetListKeeper.getI().getOffLines().put(byAddress.getHostAddress(), hostName);
            
            ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"red\">" + hostName + FONT_BR_CLOSE);
            stringBuilder.append(hostAddress).append(" ").append(hostName);
        }
        if (stringBuilder.toString().contains(PAT_IS_ONLINE)) {
            printStream.println(hostAddress + " " + hostName);
            messageToUser.info(getClass().getSimpleName() + ".oneIpScanAndPrintToFile ip online " + whatVlan + iThree + "." + jFour, vlanFile.getName(), " = " + vlanFile
                .length() + ConstantsFor.STR_BYTES);
        }
        return stringBuilder.toString();
    }
    
    /**
     Сканер локальной сети@param stStMap Запись в лог@param fromVlan начало с 3 октета IP@param toVlan   конец с 3 октета IP@param whatVlan первый 2 октета, с точкоё в конце.
     */
    private ConcurrentMap<String, String> scanLanSegment(int fromVlan, int toVlan) throws IOException {
        ConcurrentMap<String, String> stStMap = new ConcurrentHashMap<>(MAX_IN_ONE_VLAN * (toVlan - fromVlan));
        String theScannedIPHost = "No scan yet. MAP Capacity: ";
        
        for (int i = fromVlan; i < toVlan; i++) {
            for (int j = 0; j < 255; j++) {
                AppComponents.threadConfig().thrNameSet(i + "." + j);
                try {
                    theScannedIPHost = oneIpScanAndPrintToFile(i, j);
                    stStMap.put(theScannedIPHost.split(" ")[0], theScannedIPHost.split(" ")[1]);
                }
                catch (IOException e) {
                    stStMap.put(e.getMessage(), new TForms().fromArray(e, false));
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    stStMap.put(theScannedIPHost, e.getMessage());
                }
            }
            executionProcessLog.add(getBeansInfo());
            Collections.sort(executionProcessLog);
            setSpend();
            if (executionProcessLog.size() >= 8) {
                String fileName = this.from + "_vlan-to_" + this.to + ".log";
                boolean fileOk = FileSystemWorker.writeFile(fileName, executionProcessLog.stream());
                executionProcessLog.clear();
                messageToUser.info(fileName, "fileOk", " = " + fileOk);
            }
        }
        return stStMap;
    }
}
