package ru.vachok.networker.net;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.OtherKnownDevices;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

/**
 Периодический мониторинг телевизоров и WiFi.
 <p>
 Точки : <br>
 {@link ru.vachok.networker.net.enums.SwitchesWiFi#C_204_2_UPAK} ; {@link ru.vachok.networker.net.enums.SwitchesWiFi#C_204_3_UPAK} ;
 {@link ru.vachok.networker.net.enums.SwitchesWiFi#C_204_10_GP} ; {@link ru.vachok.networker.net.enums.OtherKnownDevices}
 @since 05.02.2019 (9:00) */
public class NetMonitorPTV implements Runnable {

    private OutputStream outputStream = null;

    private PrintWriter printWriter = null;

    private static final String CLASS_NAME = NetMonitorPTV.class.getSimpleName();

    private MessageToUser messageToUser = new MessageLocal();

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

        boolean inetAddressReachable = ptv1.isReachable(ConstantsFor.TIMEOUT_650);
        boolean inetAddressReachable1 = ptv2.isReachable(ConstantsFor.TIMEOUT_650);
        boolean upakCisco2042Reachable = upakCisco2042.isReachable(ConstantsFor.TIMEOUT_650);
        boolean upakCisco2043Reachable = upakCisco2043.isReachable(ConstantsFor.TIMEOUT_650);
        boolean gpCisco2042Reachable = gpCisco20410.isReachable(ConstantsFor.TIMEOUT_650);

        stringBuilder.append(ptv1);
        stringBuilder.append(" is ");
        stringBuilder.append(inetAddressReachable);
        stringBuilder.append(", ");
        stringBuilder.append(ptv2);
        stringBuilder.append(" is ");
        stringBuilder.append(inetAddressReachable1);

        stringBuilder.append("<br>");

        stringBuilder.append(upakCisco2042).append(" is ").append(upakCisco2042Reachable).append(", ");
        stringBuilder.append(upakCisco2043).append(" is ").append(upakCisco2043Reachable).append(", ");
        stringBuilder.append(gpCisco20410).append(" is ").append(gpCisco2042Reachable).append("<br>");

        String s = stringBuilder.toString();
        printWriter.print(s + " " + LocalDateTime.now());
        printWriter.println();
        checkSize();
    }

    private void checkSize() {
        File pingTv = new File("ping.tv");
        if (pingTv.length() > ConstantsFor.MBYTE) {
            printWriter.close();
            boolean b = FileSystemWorker.copyOrDelFile(pingTv, ".\\lan\\tv_" + System.currentTimeMillis() / 1000 + ".ping", true);
            new MessageCons().infoNoTitles(b + " " + pingTv.toPath() + " " + pingTv.length() / ConstantsFor.KBYTE);
            if(b){
                this.printWriter = new PrintWriter(outputStream, true);
            }
            else{
                try{
                    ESender.sendM(Collections.singletonList(ConstantsFor.GMAIL_COM), getClass().getSimpleName(), pingTv.getAbsolutePath()
                        + " is " + pingTv.exists());
                }
                catch(Exception e){
                    FileSystemWorker.error("NetMonitorPTV.checkSize", e);
                }
            }
        }
    }

    @Override
    public void run() {
        messageToUser.info(OtherKnownDevices.class.getSimpleName(), ".values()", OtherKnownDevices.values().length + "");
        try {
            pingIPTV();
        } catch (IOException e) {
            new MessageLocal().errorAlert(CLASS_NAME, "run", e.getMessage());
            FileSystemWorker.error("NetMonitorPTV.run", e);
        }
    }
}
