package ru.vachok.networker.net;


import ru.vachok.messenger.MessageFile;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.OtherKnownDevices;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.time.LocalDateTime;
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

    private OutputStream outputStream = null;

    private PrintWriter printWriter = null;

    private MessageToUser messageToUser = new MessageLocal();

    private String pingResultLast = "No pings yet.";

    public String getPingResultLast() {
        return pingResultLast;
    }

    {
        try {
            outputStream = new FileOutputStream("ping.tv");
            printWriter = new PrintWriter(Objects.requireNonNull(outputStream), true);
        } catch (FileNotFoundException e) {
            new MessageLocal().errorAlert(CLASS_NAME, "instance initializer", e.getMessage());
            FileSystemWorker.error("NetMonitorPTV.instance initializer", e);
        }
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

    private void checkSize() {
        File pingTv = new File("ping.tv");
        printWriter.print(pingResultLast + " " + LocalDateTime.now());
        printWriter.println();
        if (pingTv.length() > ConstantsFor.MBYTE) {
            printWriter.close();
            ifPingTVIsBig(pingTv);
        } else {
            this.pingResultLast = pingResultLast + " (" + pingTv.length() / ConstantsFor.KBYTE + " KBytes)";
        }
    }

    private void ifPingTVIsBig(File pingTv) {
        boolean isPingTvCopied = FileSystemWorker.copyOrDelFile(pingTv, ".\\lan\\tv_" + System.currentTimeMillis() / 1000 + ".ping", true);
        Thread.currentThread().setName("PingTVIsBig-" + isPingTvCopied);
        String classMeth = "NetMonitorPTV.ifPingTVIsBig";

        messageToUser.info(classMeth, "isPingTvCopied", String.valueOf(isPingTvCopied));

        if (isPingTvCopied) {
            Thread.currentThread().setName(classMeth);
            this.printWriter = new PrintWriter(outputStream, true);
        } else {
            messageToUser = new MessageFile();
            messageToUser.info(classMeth, "printWriter.checkError()", String.valueOf(printWriter.checkError()));
            messageToUser = new MessageLocal();
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
            FileSystemWorker.error("NetMonitorPTV.run", e);
        }
    }
}
