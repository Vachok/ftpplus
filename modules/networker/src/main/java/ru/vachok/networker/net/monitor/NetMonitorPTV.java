// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import ru.vachok.networker.AbstractForms;
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
        if (pingTv.length() > ConstantsFor.MBYTE) {
            ifPingTVIsBig();
        }
        else {
            this.pingResultLast = pingResultLast + " (" + pingTv.length() / ConstantsFor.KBYTE + " KBytes)";
        }
        return pingTv.getAbsolutePath();
    }
    
    @Override
    public Runnable getMonitoringRunnable() {
        return this;
    }
    
    @Override
    public String getStatistics() {
        return FileSystemWorker.readFile(pingTv);
    }
    
    private void ifPingTVIsBig() {
        String fileCopyPathString = "." + ConstantsFor.FILESYSTEM_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + "tv_" + System.currentTimeMillis() / 1000 + ".ping";
        boolean isPingTvCopied = FileSystemWorker.copyOrDelFile(pingTv, Paths.get(fileCopyPathString).toAbsolutePath().normalize(), true);
        if (isPingTvCopied) {
            InitProperties.setPreference(FileNames.PING_TV, new Date() + "_renewed");
        }
        else {
            messageToUser.info(pingTv.getAbsolutePath() + " size in kb = " + pingTv.length() / ConstantsFor.KBYTE);
        }
    }
    
    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        try {
            pingIPTV();
        }
        catch (IOException | IllegalAccessException e) {
            messageToUser.warn(NetMonitorPTV.class.getSimpleName(), "run", e.getMessage() + Thread.currentThread().getState().name());
        }
    }
    
    protected String getPingResultLast() {
        return pingResultLast;
    }
    
    private void pingIPTV() throws IllegalAccessException, IOException {
        Thread.currentThread().checkAccess();
        Thread.currentThread().setPriority(1);
        createFile();
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
        writeStat();
    }
    
    private void createFile() throws IOException {
        Path filePath = pingTv.toPath();
        if (!pingTv.exists()) {
            Files.createFile(pingTv.toPath());
            InitProperties.setPreference(FileNames.PING_TV, new Date() + "_create");
        }
    }
    
    private void pingSwitches() throws IllegalAccessException {
        Field[] swFields = SwitchesWiFi.class.getFields();
        for (Field swF : swFields) {
            String ipAddrStr = swF.get(swF).toString();
            writePingToDB(ipAddrStr, swF.getName());
        }
    }
    
    private void writePingToDB(String ipAddr, String name) {
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_LANMONITOR)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO SwitchesWiFi (`ip`, `pcName`, `online`) VALUES (?,?,?);")) {
                preparedStatement.setQueryTimeout(2);
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
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetMonitorPTV{");
        sb.append("pingResultLast='").append(pingResultLast).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    private void writeStat() {
        try (OutputStream outputStream = new FileOutputStream(pingTv, true);
             PrintStream printStream = new PrintStream(outputStream, true)) {
            printStream.print(pingResultLast + " " + LocalDateTime.now());
            printStream.println();
        }
        catch (IOException e) {
            messageToUser.error("NetMonitorPTV.writeStatAndCheckSize", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
    }
}
