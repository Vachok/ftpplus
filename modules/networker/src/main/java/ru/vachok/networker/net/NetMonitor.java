package ru.vachok.networker.net;


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
public class NetMonitor implements Runnable {

    private PrintWriter printWriter = null;

    private static final String CLASS_NAME = "NetMonitor";

    {
        try {
            OutputStream outputStream = new FileOutputStream("ping.tv");
            printWriter = new PrintWriter(Objects.requireNonNull(outputStream), true);
        } catch (FileNotFoundException e) {
            new MessageLocal().errorAlert(CLASS_NAME, "instance initializer", e.getMessage());
            FileSystemWorker.error("NetMonitor.instance initializer", e);
        }
    }

    @Override
    public void run() {
        try{
            pingIPTV();
        }
        catch(Exception e){
            new MessageLocal().errorAlert(CLASS_NAME, "run", e.getMessage());
            FileSystemWorker.error("NetMonitor.run", e);
        }
    }

    private void pingIPTV() throws Exception {
        InetAddress ptv1 = InetAddress.getByName(ConstantsNet.PTV1_EATMEAT_RU);
        InetAddress ptv2 = InetAddress.getByName(ConstantsNet.PTV2_EATMEAT_RU);
        boolean inetAddressReachable = ptv1.isReachable(ConstantsFor.TIMEOUT_650);
        boolean inetAddressReachable1 = ptv2.isReachable(ConstantsFor.TIMEOUT_650);
        String s = ptv1 + " is " + inetAddressReachable + ", " + ptv2 + " is " + inetAddressReachable1;
        printWriter.print(s + " " + LocalDateTime.now());
        printWriter.println();
        checkSize();
    }

    private void checkSize() throws Exception {
        File pingTv = new File("ping.tv");
        if (pingTv.length() > ConstantsFor.MBYTE) {
            boolean b = FileSystemWorker.copyOrDelFile(pingTv, ".\\lan\\tv_" + System.currentTimeMillis() / 1000 + ".ping", true);
            printWriter.println("ping.tv is " + b);
        }
    }
}
