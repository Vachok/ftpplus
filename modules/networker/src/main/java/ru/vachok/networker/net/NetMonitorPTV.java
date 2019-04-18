package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.VersionInfo;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.OtherKnownDevices;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

/**
 Периодический мониторинг телевизоров и WiFi.
 <p>
 Точки : <br>
 {@link ru.vachok.networker.net.enums.SwitchesWiFi#C_204_2_UPAK} ; {@link ru.vachok.networker.net.enums.SwitchesWiFi#C_204_3_UPAK} ;
 {@link ru.vachok.networker.net.enums.SwitchesWiFi#C_204_10_GP} ; {@link ru.vachok.networker.net.enums.OtherKnownDevices}

 @since 05.02.2019 (9:00) */
public class NetMonitorPTV implements Runnable {

    private static final String CLASS_NAME = NetMonitorPTV.class.getSimpleName();

    private VersionInfo versionInfo = AppComponents.versionInfo();

    private OutputStream outputStream;
    
    private PrintStream printStream;
    
    private long ptvStartStamp;
    
    public NetMonitorPTV() {
        File ptvFile = new File(FILENAME_PINGTV);
        NetListKeeper.setPtvTime(new Date().toString());
        try {
            outputStream = new FileOutputStream(ptvFile);
            printStream = new PrintStream(Objects.requireNonNull(outputStream), true);
        }
        catch (IOException e) {
            messageToUser.errorAlert("NetMonitorPTV", "NetMonitorPTV", e.getMessage());
            FileSystemWorker.error("NetMonitorPTV.NetMonitorPTV", e);
        }
    }
    
    private final String simpleName = NetMonitorPTV.class.getSimpleName();
    
    private MessageToUser messageToUser = new MessageLocal(simpleName);
    
    private String pingResultLast = "No pings yet.";
    
    private static final String FILENAME_PINGTV = "ping.tv";
    
    public long getPtvStartStamp() {
        return ptvStartStamp;
    }

    private void pingIPTV() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        byte[] upakCisco2042b = InetAddress.getByName(SwitchesWiFi.C_204_2_UPAK).getAddress();
        byte[] upakCisco2043b = InetAddress.getByName(SwitchesWiFi.C_204_3_UPAK).getAddress();
        byte[] gpCisco20410b = InetAddress.getByName(SwitchesWiFi.C_204_3_UPAK).getAddress();

        InetAddress ptv1 = InetAddress.getByName(OtherKnownDevices.PTV1_EATMEAT_RU);
        InetAddress ptv2 = InetAddress.getByName(OtherKnownDevices.PTV2_EATMEAT_RU);
        InetAddress upakCisco2042 = InetAddress.getByAddress(upakCisco2042b);
        InetAddress upakCisco2043 = InetAddress.getByAddress(upakCisco2043b);
        InetAddress gpCisco20410 = InetAddress.getByAddress(gpCisco20410b);

        boolean ptv1Reachable = ptv1.isReachable(ConstantsFor.TIMEOUT_650);
        boolean ptv2Reachable = ptv2.isReachable(ConstantsFor.TIMEOUT_650);
        boolean upakCisco2042Reachable = upakCisco2042.isReachable(ConstantsFor.TIMEOUT_650);
        boolean upakCisco2043Reachable = upakCisco2043.isReachable(ConstantsFor.TIMEOUT_650);
        boolean gpCisco2042Reachable = gpCisco20410.isReachable(ConstantsFor.TIMEOUT_650);

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
        checkSize();
    }
    
    private void checkSize() throws IOException {
        File pingTv = new File(ConstantsFor.FILENAME_PTV);
        printStream.print(pingResultLast + " " + LocalDateTime.now());
        printStream.println();
        
        if (pingTv.length() > ConstantsFor.MBYTE) {
            printStream.close();
            ifPingTVIsBig(pingTv);
        } else {
            this.pingResultLast = pingResultLast + " (" + pingTv.length() / ConstantsFor.KBYTE + " KBytes)";
        }
    }
    
    private void ifPingTVIsBig(File pingTv) throws IOException {
        boolean isPingTvCopied = FileSystemWorker.copyOrDelFile(pingTv, ".\\lan\\tv_" + System.currentTimeMillis() / 1000 + ".ping", true);
        String classMeth = "NetMonitorPTV.ifPingTVIsBig";
        if (isPingTvCopied) {
            NetListKeeper.setPtvTime(new Date().toString());
            AppComponents.threadConfig().thrNameSet(getClass().getSimpleName());
            this.outputStream = new FileOutputStream(pingTv);
            this.printStream = new PrintStream(outputStream, true);
            this.ptvStartStamp = System.currentTimeMillis();
        } else {
            messageToUser.info(FILENAME_PINGTV, "creating", new Date(ptvStartStamp).toString());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetMonitorPTV{");
        sb.append("pingResultLast='").append(pingResultLast).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void run() {
        try {
            pingIPTV();
        } catch (IOException e) {
            messageToUser.errorAlert(CLASS_NAME, "run", e.getMessage());
        }
    }
}
