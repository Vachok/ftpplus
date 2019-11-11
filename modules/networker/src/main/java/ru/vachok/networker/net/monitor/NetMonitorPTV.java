// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.file.*;
import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 Периодический мониторинг телевизоров и WiFi.
 <p>
 Точки : <br>
 {@link SwitchesWiFi#C_204_2_UPAK} ; {@link SwitchesWiFi#C_204_3_UPAK} ;
 {@link SwitchesWiFi#C_204_10_GP} ; {@link OtherKnownDevices}
 
 @see NetMonitorPTVTest
 @since 05.02.2019 (9:00) */
public class NetMonitorPTV implements NetScanService {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, NetMonitorPTV.class.getSimpleName());
    
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    private OutputStream outputStream;
    
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    private PrintStream printStream;
    
    private Preferences preferences = InitProperties.getUserPref();
    
    private String pingResultLast = "No pings yet.";
    
    private File pingTv = new File(FileNames.PING_TV);
    
    @Override
    public String getExecution() {
        return FileSystemWorker.readFile(pingTv.getAbsolutePath());
    }
    
    @Override
    public String getPingResultStr() {
        return pingResultLast;
    }
    
    @Override
    public String writeLog() {
        try {
            writeStatAndCheckSize();
            return pingTv.getAbsolutePath();
        }
        catch (IOException | BackingStoreException e) {
            String errStr = MessageFormat.format("NetMonitorPTV.writeLogToFile: {0}, ({1})", e.getMessage(), e.getClass().getName());
            messageToUser.error(errStr);
            return errStr;
        }
    }
    
    @Override
    public Runnable getMonitoringRunnable() {
        return this;
    }
    
    @Override
    public String getStatistics() {
        throw new TODOException("01.11.2019 (9:04)");
    }
    
    protected String getPingResultLast() {
        return pingResultLast;
    }
    
    @Override
    public void run() {
        try {
            if (outputStream == null || printStream == null) {
                createFile();
            }
            pingIPTV();
        }
        catch (IOException | IllegalAccessException e) {
            messageToUser.warn("NetMonitorPTV", "run", e.getMessage() + " see line: 154");
        }
        catch (BackingStoreException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".run", e));
        }
    }
    
    private void createFile() throws IOException, BackingStoreException {
        Path filePath = pingTv.toPath();
    
        if (!pingTv.exists()) {
            Files.createFile(pingTv.toPath());
            preferences.put(FileNames.PING_TV, new Date() + "_create");
        }
        else if (filePath.toAbsolutePath().normalize().toFile().isFile()) {
            preferences.sync();
        }
        else {
            preferences.put(FileNames.PING_TV, "7-JAN-1984 )");
        }
        this.outputStream = new FileOutputStream(pingTv);
        this.printStream = new PrintStream(Objects.requireNonNull(outputStream), true);
    }
    
    private void pingIPTV() throws IOException, BackingStoreException, IllegalAccessException {
        Thread.currentThread().checkAccess();
        Thread.currentThread().setPriority(1);
        StringBuilder stringBuilder = new StringBuilder();
        int timeOut = ConstantsFor.TIMEOUT_650 / 2;
        
        byte[] upakCisco2042b = InetAddress.getByName(SwitchesWiFi.C_204_2_UPAK).getAddress();
        byte[] upakCisco2043b = InetAddress.getByName(SwitchesWiFi.C_204_3_UPAK).getAddress();
        byte[] gpCisco20410b = InetAddress.getByName(SwitchesWiFi.C_204_3_UPAK).getAddress();
        
        InetAddress iKornetovaDO0055 = InetAddress.getByName(OtherKnownDevices.DO0055_IKORN);
        InetAddress ptv1 = InetAddress.getByName(OtherKnownDevices.PTV1_EATMEAT_RU);
        InetAddress ptv2 = InetAddress.getByName(OtherKnownDevices.PTV2_EATMEAT_RU);
        
        InetAddress upakCisco2042 = InetAddress.getByAddress(upakCisco2042b);
        InetAddress upakCisco2043 = InetAddress.getByAddress(upakCisco2043b);
        InetAddress gpCisco20410 = InetAddress.getByAddress(gpCisco20410b);
        
        boolean ptv1Reachable = ptv1.isReachable(timeOut);
        boolean ptv2Reachable = ptv2.isReachable(timeOut);
        boolean iKornIsReachable = ptv2.isReachable(timeOut);
        boolean upakCisco2042Reachable = upakCisco2042.isReachable(timeOut);
        boolean upakCisco2043Reachable = upakCisco2043.isReachable(timeOut);
        boolean gpCisco2042Reachable = gpCisco20410.isReachable(timeOut);
        
        stringBuilder.append(ptv1);
        stringBuilder.append(" is ");
        stringBuilder.append(ptv1Reachable);
        stringBuilder.append(", ");
        stringBuilder.append(ptv2);
        stringBuilder.append(" is ");
        stringBuilder.append(ptv2Reachable);
        
        stringBuilder.append("<br>");
        stringBuilder.append("\n***Wi-Fi points:");
        
        stringBuilder.append(upakCisco2042).append(" is ").append(upakCisco2042Reachable).append(", ");
        stringBuilder.append(upakCisco2043).append(" is ").append(upakCisco2043Reachable).append(", ");
        stringBuilder.append(iKornetovaDO0055).append(" is ").append(iKornIsReachable).append(", ");
        stringBuilder.append(gpCisco20410).append(" is ").append(gpCisco2042Reachable).append("<br>***");
        this.pingResultLast = stringBuilder.toString();
        pingSwitches();
        writeStatAndCheckSize();
    }
    
    private void pingSwitches() throws IllegalAccessException {
        Field[] swFields = SwitchesWiFi.class.getFields();
        for (Field swF : swFields) {
            String ipAddrStr = swF.get(swF).toString();
            writePingToDB(ipAddrStr, swF.getName());
        }
    }
    
    private void writeStatAndCheckSize() throws IOException, BackingStoreException {
        printStream.print(pingResultLast + " " + LocalDateTime.now());
        printStream.println();
        
        if (pingTv.length() > ConstantsFor.MBYTE) {
            printStream.close();
            ifPingTVIsBig();
        }
        else {
            this.pingResultLast = pingResultLast + " (" + pingTv.length() / ConstantsFor.KBYTE + " KBytes)";
        }
    }
    
    private void writePingToDB(String ipAddr, String name) {
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_LANMONITOR)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SwitchesWiFi (`ip`, `pcName`, `online`) VALUES (?,?,?);")) {
                preparedStatement.setString(1, ipAddr);
                preparedStatement.setString(2, name);
                preparedStatement.setString(3, String.valueOf(NetScanService.isReach(ipAddr)));
                preparedStatement.executeUpdate();
            }
        }
        catch (SQLException e) {
            messageToUser.info(MessageFormat.format("NetMonitorPTV.writePingToDB", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace())));
        }
    }
    
    private void ifPingTVIsBig() throws IOException, BackingStoreException {
        String fileCopyPathString = "." + ConstantsFor.FILESYSTEM_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + "tv_" + System.currentTimeMillis() / 1000 + ".ping";
        boolean isPingTvCopied = FileSystemWorker
                .copyOrDelFile(pingTv, Paths.get(fileCopyPathString).toAbsolutePath().normalize(), true);
        if (isPingTvCopied) {
            this.outputStream = new FileOutputStream(pingTv);
            this.printStream = new PrintStream(outputStream, true);
            preferences.put(FileNames.PING_TV, new Date() + "_renewed");
            preferences.sync();
        }
        else {
            messageToUser.info(pingTv.getAbsolutePath() + " size in kb = " + pingTv.length() / ConstantsFor.KBYTE);
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetMonitorPTV{");
        sb.append("pingResultLast='").append(pingResultLast).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
