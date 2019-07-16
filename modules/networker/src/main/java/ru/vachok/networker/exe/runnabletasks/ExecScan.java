// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.TForms;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.net.scanner.NetListKeeper;
import ru.vachok.networker.restapi.message.DBMessenger;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import static ru.vachok.networker.net.enums.ConstantsNet.MAX_IN_ONE_VLAN;


/**
 Да запуска скана из {@link DiapazonScan}
 <p>
 
 @see ru.vachok.networker.exe.runnabletasks.ExecScanTest
 @since 24.03.2019 (16:01) */
public class ExecScan extends DiapazonScan {
    
    
    @SuppressWarnings("StaticVariableOfConcreteClass")
    private static final ThreadConfig THR_CONFIG = AppComponents.threadConfig();
    
    private static final String FONT_BR_CLOSE = "</font><br>";
    
    private static final Pattern COMPILE = Pattern.compile("\\Q.txt\\E", Pattern.LITERAL);
    
    private static final int HOME_VLAN = 111;
    
    protected static final String PAT_IS_ONLINE = " is online";
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private final Preferences preferences = Preferences.userRoot();
    
    private final Properties props = AppComponents.getProps();
    
    private File vlanFile;
    
    private boolean isTest;
    
    private long stArt;
    
    private int fromVlan;
    
    private int toVlan;
    
    private String whatVlan;
    
    private Map<String, String> offLines;
    
    private NetListKeeper netListKeeper = NetListKeeper.getI();
    
    public ExecScan(int fromVlan, int toVlan, String whatVlan, File vlanFile) {
        
        this.fromVlan = fromVlan;
        
        this.toVlan = toVlan;
        
        this.whatVlan = whatVlan;
        
        this.vlanFile = vlanFile;
    
        this.stArt = LocalDateTime.of(ConstantsFor.YEAR_OF_MY_B, 1, 7, 2, 0).toEpochSecond(ZoneOffset.ofHours(3)) * 1000;
    
        this.offLines = netListKeeper.editOffLines();
        
    }
    
    public ExecScan(int fromVlan, int toVlan, String whatVlan, File vlanFile, boolean isTest) {
        
        this.fromVlan = fromVlan;
        
        this.toVlan = toVlan;
        
        this.whatVlan = whatVlan;
        
        this.vlanFile = vlanFile;
        
        this.isTest = isTest;
        
        this.stArt = LocalDateTime.of(ConstantsFor.YEAR_OF_MY_B, 1, 7, 2, 0).toEpochSecond(ZoneOffset.ofHours(3)) * 1000;
    
        this.offLines = AppComponents.netKeeper().editOffLines();
    }
    
    /**
     Only Testing
     <p>
     
     @see ru.vachok.networker.exe.runnabletasks.ExecScanTest#toStringTest()
     @since 10.07.2019 (21:09)
     */
    protected ExecScan() {
        
        this.fromVlan = HOME_VLAN;
        
        this.toVlan = HOME_VLAN + 1;
        
        this.whatVlan = "10.10.";
        
        this.vlanFile = new File("home.test");
        
        this.stArt = LocalDateTime.of(ConstantsFor.YEAR_OF_MY_B, 1, 7, 2, 0).toEpochSecond(ZoneOffset.ofHours(3)) * 1000;
    }
    
    @Override
    public void run() {
    
        if (vlanFile != null) {
            String copyOldResult = MessageFormat.format("Copy {0} is: {1}", vlanFile.getAbsolutePath(), cpOldFile());
            System.out.println(copyOldResult);
            messageToUser.info(copyOldResult);
        }
        if (getAllDevLocalDeq().remainingCapacity() > 0) {
            boolean execScanB = execScan();
            messageToUser.info(this.getClass().getSimpleName(), MessageFormat
                .format("Scan fromVlan {0} toVlan {1} is {2}", fromVlan, toVlan, execScanB), "allDevLocalDeq = " + getAllDevLocalDeq().size());
        }
        else {
            messageToUser.error(getClass().getSimpleName(), String.valueOf(getAllDevLocalDeq().remainingCapacity()), " allDevLocalDeq remainingCapacity!");
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ExecScan.class.getSimpleName() + "[\n", "\n]")
            .add("vlanFile = " + vlanFile.toPath().toAbsolutePath().normalize())
            .add("isTest = " + isTest)
            .add("stArt = " + stArt)
            .add("fromVlan = " + fromVlan)
            .add("toVlan = " + toVlan)
            .add("whatVlan = '" + whatVlan + "'")
            .toString();
    }
    
    private boolean execScan() {
    
        this.stArt = System.currentTimeMillis();
        try {
            ConcurrentMap<String, String> ipNameMap = scanVlans(fromVlan, toVlan);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
            new ExitApp(fromVlan + "-" + toVlan + ".map", ipNameMap).writeOwnObject();
            return true;
        }
        catch (Exception e) {
            messageToUser.error(MessageFormat.format("ExecScan.execScan says: {0}. Parameters: \n[]: {1}", e.getMessage(), false));
            return false;
        }
    }
    
    /**
     @param thirdOctet третий октет vlan
     @param fourthOctet четвертый октет vlan
     @return Example: {@code 192.168.11.0 192.168.11.0} or {@code 10.200.200.1 10.200.200.1 is online}
     
     @throws IOException при записи файла
     */
    private String oneIpScan(int thirdOctet, int fourthOctet) throws IOException {
        
        int timeOutMSec = (int) ConstantsFor.DELAY;
        byte[] aBytes = InetAddress.getByName(whatVlan + thirdOctet + "." + fourthOctet).getAddress();
        
        StringBuilder stringBuilder = new StringBuilder();
        InetAddress byAddress = InetAddress.getByAddress(aBytes);
        String hostName = byAddress.getHostName();
        String hostAddress = byAddress.getHostAddress();
        NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis(), hostAddress);
    
        if (byAddress.isReachable(calcTimeOutMSec())) {
            netListKeeper.getOnLinesResolve().put(hostAddress, hostName);
            getAllDevLocalDeq().add("<font color=\"green\">" + hostName + FONT_BR_CLOSE);
            stringBuilder.append(hostAddress).append(" ").append(hostName).append(PAT_IS_ONLINE);
        }
        else {
            offLines.put(byAddress.getHostAddress(), hostName);
            getAllDevLocalDeq().add("<font color=\"red\">" + hostName + FONT_BR_CLOSE);
            stringBuilder.append(hostAddress).append(" ").append(hostName);
        }
    
        if (stringBuilder.toString().contains(PAT_IS_ONLINE)) {
            printToFile(hostAddress, hostName, thirdOctet, fourthOctet);
        }
        netListKeeper.setOffLines(offLines);
    
        return stringBuilder.toString();
    }
    
    private int calcTimeOutMSec() {
        int timeOutMSec = (int) ConstantsFor.DELAY;
        if (ConstantsFor.thisPC().equalsIgnoreCase("HOME")) {
            timeOutMSec = (int) (ConstantsFor.DELAY * 2);
        }
        return timeOutMSec;
    }
    
    private void printToFile(String hostAddress, String hostName, int thirdOctet, int fourthOctet) throws IOException {
        messageToUser = new DBMessenger(getClass().getSimpleName());
        
        try (OutputStream outputStream = new FileOutputStream(vlanFile, true);
             PrintStream printStream = new PrintStream(Objects.requireNonNull(outputStream), true)
        ) {
            printStream.println(hostAddress + " " + hostName);
            messageToUser.info(getClass().getSimpleName() + ".oneIpScan ip online " + whatVlan + thirdOctet + "." + fourthOctet, vlanFile.getName(), " = " + vlanFile
                .length() + ConstantsFor.STR_BYTES);
            
        }
    }
    
    private void setSpend() {
        long spendMS = System.currentTimeMillis() - stArt;
        try {
            preferences.sync();
            preferences.putLong(getClass().getSimpleName(), spendMS);
            preferences.sync();
        }
        catch (BackingStoreException e) {
            props.setProperty(getClass().getSimpleName(), String.valueOf(spendMS));
        }
    }
    
    private boolean cpOldFile() {
        String fileSepar = System.getProperty(ConstantsFor.PRSYS_SEPARATOR);
        long epochSec = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(3));
        String replaceInName = "_" + epochSec + ".scan";
        Path copyPath = Paths.get(ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + vlanFile).toAbsolutePath().normalize();
        return FileSystemWorker.copyOrDelFile(vlanFile, copyPath, true);
    }
    
    /**
     Сканер локальной сети@param stStMap Запись в лог@param fromVlan начало с 3 октета IP@param toVlan   конец с 3 октета IP@param whatVlan первый 2 октета, с точкоё в конце.
     */
    private ConcurrentMap<String, String> scanVlans(int fromVlan, int toVlan) throws IOException {
        ConcurrentMap<String, String> ipNameMap = new ConcurrentHashMap<>(MAX_IN_ONE_VLAN * (toVlan - fromVlan));
        String theScannedIPHost = "No scan yet. MAP Capacity: ";
    
        for (int i = fromVlan; i < toVlan; i++) {
            setSpend();
            int maxIPs = MAX_IN_ONE_VLAN;
            if (isTest) {
                maxIPs = (int) ConstantsFor.DELAY;
            }
            for (int j = 0; j < maxIPs; j++) {
                THR_CONFIG.thrNameSet(i + "." + j);
                try {
                    theScannedIPHost = oneIpScan(i, j);
                    ipNameMap.put(theScannedIPHost.split(" ")[0], theScannedIPHost.split(" ")[1]);
                }
                catch (IOException e) {
                    ipNameMap.put(e.getMessage(), new TForms().fromArray(e, false));
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    ipNameMap.put(theScannedIPHost, e.getMessage());
                }
            }
        }
        return ipNameMap;
    }
}
