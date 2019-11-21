// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.*;
import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static ru.vachok.networker.data.enums.ConstantsNet.MAX_IN_ONE_VLAN;


/**
 Да запуска скана из {@link DiapazonScan}
 <p>
 
 @see ExecScanTest
 @since 24.03.2019 (16:01) */
public class ExecScan extends DiapazonScan {
    
    
    private static final int HOME_VLAN = 111;
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, ExecScan.class.getSimpleName());
    
    protected static final String PAT_IS_ONLINE = " is online";
    
    private File vlanFile;
    
    private boolean isTest;
    
    private long stArt;
    
    private int fromVlan;
    
    private int toVlan;
    
    private Queue<String> ipsQueue = new LinkedList<>();
    
    private String whatVlan;
    
    private Preferences preferences = InitProperties.getUserPref();
    
    private int thirdOctet;
    
    private int fourthOctet;
    
    public ExecScan(int fromVlan, int toVlan, String whatVlan, File vlanFile) {
        
        this.fromVlan = fromVlan;
        
        this.toVlan = toVlan;
        
        this.whatVlan = whatVlan;
        
        this.vlanFile = vlanFile;
    
        this.stArt = LocalDateTime.of(ConstantsFor.YEAR_OF_MY_B, 1, 7, 2, 0).toEpochSecond(ZoneOffset.ofHours(3)) * 1000;
        
    }
    
    public ExecScan(int fromVlan, int toVlan, String whatVlan, File vlanFile, boolean isTest) {
        
        this.fromVlan = fromVlan;
        
        this.toVlan = toVlan;
        
        this.whatVlan = whatVlan;
        
        this.vlanFile = vlanFile;
        
        this.isTest = isTest;
    
        this.stArt = LocalDateTime.of(ConstantsFor.YEAR_OF_MY_B, 1, 7, 2, 0).toEpochSecond(ZoneOffset.ofHours(3)) * 1000;
    }
    
    protected ExecScan() {
        
        this.fromVlan = HOME_VLAN;
        
        this.toVlan = HOME_VLAN + 1;
        
        this.whatVlan = "10.10.";
        
        this.vlanFile = new File("home.test");
    
        this.stArt = LocalDateTime.of(ConstantsFor.YEAR_OF_MY_B, 1, 7, 2, 0).toEpochSecond(ZoneOffset.ofHours(3)) * 1000;
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
    
    @Override
    public void run() {
        Thread jobThread = new Thread(this::makeJob);
        try {
            jobThread.run();
        }
        finally {
            messageToUser.warn(this.getClass().getSimpleName(), "Running finalization...", jobThread.toString());
            jobThread.interrupt();
            Runtime.getRuntime().runFinalization();
        }
    
    }
    
    private void makeJob() {
        File ipsWithInet = new File(FileNames.INETSTATSIP_CSV);
        ipsQueue.clear();
        ipsQueue.addAll(FileSystemWorker.readFileToQueue(ipsWithInet.toPath().toAbsolutePath().normalize()));
        boolean isFileNotNullAndExists = vlanFile != null && vlanFile.exists();
        boolean isLocalDequeCapacityBiggerZero = getAllDevLocalDeq().remainingCapacity() > 0;
        if (isFileNotNullAndExists) {
            String copyOldResult = MessageFormat.format("Copy {0} is: {1} ({2})", vlanFile.getAbsolutePath(), cpOldFile(), this.getClass().getSimpleName());
            messageToUser.info(copyOldResult);
        }
        if (isLocalDequeCapacityBiggerZero) {
            
            boolean execScanB = execScan();
            messageToUser.info(this.getClass().getSimpleName(), MessageFormat
                .format("Scan fromVlan {0} toVlan {1} is {2}", fromVlan, toVlan, execScanB), "allDevLocalDeq = " + getAllDevLocalDeq().size());
        }
        else {
            messageToUser.error(getExecution(), String.valueOf(getAllDevLocalDeq().remainingCapacity()), " allDevLocalDeq remainingCapacity!");
        }
    }
    
    private boolean cpOldFile() {
        String vlanFileName = vlanFile.getName();
        vlanFileName = vlanFileName.replace(".txt", "_" + LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(3)) + ".scan");
        String toPath = ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + vlanFileName;
        Path copyPath = Paths.get(toPath).toAbsolutePath().normalize();
        return FileSystemWorker.copyOrDelFile(vlanFile, copyPath, true);
    }
    
    private boolean execScan() {
        this.stArt = System.currentTimeMillis();
        try {
            Files.createFile(vlanFile.toPath());
            scanVlans(fromVlan, toVlan);
            preferences.putLong(DiapazonScan.class.getSimpleName(), System.currentTimeMillis());
            preferences.sync();
            return true;
        }
        catch (RuntimeException | BackingStoreException | IOException e) {
            messageToUser.error(MessageFormat.format("ExecScan.execScan says: {0}. Parameters: \n[]: {1}", e.getMessage(), false));
            return false;
        }
    }
    
    /**
     Сканер локальной сети@param stStMap Запись в лог@param fromVlan начало с 3 октета IP@param toVlan   конец с 3 октета IP@param whatVlan первый 2 октета, с точкоё в конце.
     */
    private void scanVlans(int fromVlan, int toVlan) {
        for (int i = fromVlan; i < toVlan; i++) {
            setSpend();
            int maxIPs = MAX_IN_ONE_VLAN;
            if (isTest) {
                maxIPs = (int) ConstantsFor.DELAY;
            }
            for (int j = 0; j < maxIPs; j++) {
                ThreadConfig.thrNameSet(i + "." + j);
                try {
                    oneIpScan(i, j);
                }
                catch (IOException e) {
                    messageToUser.error("ExecScan.scanVlans", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    messageToUser.warning(ExecScan.class.getSimpleName(), e.getMessage(), " see line: 204 ***");
                }
            }
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
            InitProperties.getTheProps().setProperty(getClass().getSimpleName(), String.valueOf(spendMS));
            messageToUser.error("ExecScan.setSpend", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
    }
    
    /**
     @param thirdOctet третий октет vlan
     @param fourthOctet четвертый октет vlan
     @throws IOException при записи файла
     */
    private void oneIpScan(int thirdOctet, int fourthOctet) throws IOException {
        this.thirdOctet = thirdOctet;
        this.fourthOctet = fourthOctet;
        byte[] aBytes = InetAddress.getByName(whatVlan + thirdOctet + "." + fourthOctet).getAddress();
        InetAddress byAddress = InetAddress.getByAddress(aBytes);
        InitProperties.setPreference(DiapazonScan.class.getSimpleName(), String.valueOf(System.currentTimeMillis()));
        pingDev(byAddress);
    }
    
    private void pingDev(@NotNull InetAddress byAddress) throws IOException {
        String hostName = byAddress.getHostName();
        String hostAddress = byAddress.getHostAddress();
        if (byAddress.isReachable(calcTimeOutMSec())) {
            NetKeeper.getOnLinesResolve().put(hostAddress, hostName);
            getAllDevLocalDeq().add(HTMLGeneration.getInstance("").getHTMLCenterColor(ConstantsFor.GREEN, hostName));
            printToFile(hostAddress, hostName, thirdOctet, fourthOctet);
            if (UsefulUtilities.thisPC().toLowerCase().contains("rups") || UsefulUtilities.thisPC().toLowerCase().contains("do0")) {
                AppConfigurationLocal.getInstance().execute(()->writeToDB(hostAddress, hostName), 19);
            }
            else {
                messageToUser.info(this.getClass().getSimpleName(), "writeToDB", hostAddress);
            }
        }
        else {
            getAllDevLocalDeq().add(HTMLGeneration.getInstance("").setColor(ConstantsFor.RED, hostName));
        }
    }
    
    private int calcTimeOutMSec() {
        int timeOutMSec = (int) ConstantsFor.DELAY / 2;
        if (UsefulUtilities.thisPC().equalsIgnoreCase("home")) {
            timeOutMSec = (int) (ConstantsFor.DELAY * 2);
        }
        return timeOutMSec;
    }
    
    private void writeToDB(String hostAddress, String hostName) {
        final String sql = "INSERT INTO lan.online (ip, pcName, inet) VALUES (?, ?, ?)";
        if (hostName == null || hostName.isEmpty()) {
            hostName = "no name";
        }
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_LANONLINE)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setQueryTimeout(18);
                preparedStatement.setString(1, hostAddress);
                preparedStatement.setString(2, hostName);
                preparedStatement.setString(3, checkIP(hostAddress));
                preparedStatement.executeUpdate();
            }
        }
        catch (SQLException e) {
            messageToUser.error(getClass().getSimpleName(), "writeToDB", FileSystemWorker.error(getClass().getSimpleName() + ".writeToDB", e));
        }
    }
    
    private void printToFile(String hostAddress, String hostName, int thirdOctet, int fourthOctet) throws IOException {
        
        try (OutputStream outputStream = new FileOutputStream(vlanFile, true);
             PrintStream printStream = new PrintStream(Objects.requireNonNull(outputStream), true)
        ) {
            printStream.println(hostAddress + " " + hostName);
            messageToUser.info(getClass().getSimpleName() + ".oneIpScan ip online " + whatVlan + thirdOctet + "." + fourthOctet, vlanFile.getName(), " = " + vlanFile
                .length() + ConstantsFor.STR_BYTES);
            
        }
    }
    
    @Contract(pure = true)
    private String checkIP(String ipAddress) {
        String retStr = "N";
        for (String s : ipsQueue) {
            if (s.equalsIgnoreCase(ipAddress)) {
                retStr = "Y";
                break;
            }
        }
        return retStr;
    }
}
