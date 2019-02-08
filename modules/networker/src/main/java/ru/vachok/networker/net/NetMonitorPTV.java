package ru.vachok.networker.net;


import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 Периодический мониторинг сети
 <p>

 @since 05.02.2019 (9:00) */
public class NetMonitorPTV implements Runnable {

    private PrintWriter printWriter = null;

    private static final String CLASS_NAME = "NetMonitorPTV";

    {
        try {
            OutputStream outputStream = new FileOutputStream("ping.tv");
            printWriter = new PrintWriter(Objects.requireNonNull(outputStream), true);
        } catch (FileNotFoundException e) {
            new MessageLocal().errorAlert(CLASS_NAME, "instance initializer", e.getMessage());
            FileSystemWorker.error("NetMonitorPTV.instance initializer", e);
        }
    }

    private void pingIPTV() throws IOException {
        InetAddress ptv1 = InetAddress.getByName(ConstantsNet.PTV1_EATMEAT_RU);
        InetAddress ptv2 = InetAddress.getByName(ConstantsNet.PTV2_EATMEAT_RU);
        boolean inetAddressReachable = ptv1.isReachable(ConstantsFor.TIMEOUT_650);
        boolean inetAddressReachable1 = ptv2.isReachable(ConstantsFor.TIMEOUT_650);
        String s = ptv1 + " is " + inetAddressReachable + ", " + ptv2 + " is " + inetAddressReachable1;
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
        }
    }

    @Override
    public void run() {
        try {
            pingIPTV();
        } catch (IOException e) {
            new MessageLocal().errorAlert(CLASS_NAME, "run", e.getMessage());
            FileSystemWorker.error("NetMonitorPTV.run", e);
        }
    }
}
