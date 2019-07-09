// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.OtherKnownDevices;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 Периодический мониторинг телевизоров и WiFi.
 <p>
 Точки : <br>
 {@link ru.vachok.networker.net.enums.SwitchesWiFi#C_204_2_UPAK} ; {@link ru.vachok.networker.net.enums.SwitchesWiFi#C_204_3_UPAK} ;
 {@link ru.vachok.networker.net.enums.SwitchesWiFi#C_204_10_GP} ; {@link ru.vachok.networker.net.enums.OtherKnownDevices}
 
 @since 05.02.2019 (9:00) */
public class NetMonitorPTV implements Runnable {
    
    
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    private OutputStream outputStream;
    
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    private PrintStream printStream;
    
    private MessageToUser messageToUser = new MessageLocal(NetMonitorPTV.class.getSimpleName());
    
    private Preferences preferences = AppComponents.getUserPref();
    
    private String pingResultLast = "No pings yet.";
    
    private File pingTv = new File(ConstantsFor.FILENAME_PTV);
    
    @Override
    public void run() {
        try {
            if (outputStream == null || printStream == null) {
                createFile();
            }
            pingIPTV();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        catch (BackingStoreException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".run", e));
        }
    }
    
    protected String getPingResultLast() {
        return pingResultLast;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetMonitorPTV{");
        sb.append("pingResultLast='").append(pingResultLast).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    private void createFile() throws IOException, BackingStoreException {
        Path filePath = pingTv.toPath();
    
        if (!pingTv.exists()) {
            Files.createFile(pingTv.toPath());
            preferences.put(ConstantsFor.FILENAME_PTV, new Date() + "_create");
        }
        else if (filePath.toAbsolutePath().normalize().toFile().isFile()) {
            preferences.sync();
        }
        else {
            System.err.println(filePath);
            preferences.put(ConstantsFor.FILENAME_PTV, "7-JAN-1984 )");
        }
        this.outputStream = new FileOutputStream(pingTv);
        this.printStream = new PrintStream(Objects.requireNonNull(outputStream), true);
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
    
    private void ifPingTVIsBig() throws IOException, BackingStoreException {
        String fileCopyPathString = "." + ConstantsFor.FILESYSTEM_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + "tv_" + System.currentTimeMillis() / 1000 + ".ping";
        boolean isPingTvCopied = FileSystemWorker
            .copyOrDelFile(pingTv, Paths.get(fileCopyPathString).toAbsolutePath().normalize(), true);
        if (isPingTvCopied) {
            this.outputStream = new FileOutputStream(pingTv);
            this.printStream = new PrintStream(outputStream, true);
            preferences.put(ConstantsFor.FILENAME_PTV, new Date() + "_renewed");
            preferences.sync();
        }
        else {
            System.out.println(pingTv.getAbsolutePath() + " size in kb = " + pingTv.length() / ConstantsFor.KBYTE);
        }
    }
    
    private void pingIPTV() throws IOException, BackingStoreException {
        StringBuilder stringBuilder = new StringBuilder();
        int timeOut = ConstantsFor.TIMEOUT_650 / 2;
    
        byte[] upakCisco2042b = InetAddress.getByName(SwitchesWiFi.C_204_2_UPAK).getAddress();
        byte[] upakCisco2043b = InetAddress.getByName(SwitchesWiFi.C_204_3_UPAK).getAddress();
        byte[] gpCisco20410b = InetAddress.getByName(SwitchesWiFi.C_204_3_UPAK).getAddress();
    
        InetAddress ptv1 = InetAddress.getByName(OtherKnownDevices.PTV1_EATMEAT_RU);
        InetAddress ptv2 = InetAddress.getByName(OtherKnownDevices.PTV2_EATMEAT_RU);
        
        InetAddress upakCisco2042 = InetAddress.getByAddress(upakCisco2042b);
        InetAddress upakCisco2043 = InetAddress.getByAddress(upakCisco2043b);
        InetAddress gpCisco20410 = InetAddress.getByAddress(gpCisco20410b);
    
        boolean ptv1Reachable = ptv1.isReachable(timeOut);
        boolean ptv2Reachable = ptv2.isReachable(timeOut);
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
        stringBuilder.append(gpCisco20410).append(" is ").append(gpCisco2042Reachable).append("<br>***");
        this.pingResultLast = stringBuilder.toString();
        writeStatAndCheckSize();
    }
}
