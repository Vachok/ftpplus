package ru.vachok.networker.net;


import ru.vachok.messenger.email.ESender;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.VersionInfo;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

/**
 Периодический мониторинг сети
 <p>

 @since 05.02.2019 (9:00) */
public class NetMonitor implements Runnable {

    private PrintWriter printWriter = null;
    {
        try {
            OutputStream outputStream = new FileOutputStream("ping.tv");
            printWriter = new PrintWriter(Objects.requireNonNull(outputStream), true);
        } catch (FileNotFoundException e) {
            new MessageLocal().errorAlert("NetMonitor", "instance initializer", e.getMessage());
            FileSystemWorker.error("NetMonitor.instance initializer", e);
        }
    }
    private void pingIPTV() throws Exception {
        InetAddress ptv1 = InetAddress.getByName("ptv1.eatmeat.ru");
        InetAddress ptv2 = InetAddress.getByName("ptv2.eatmeat.ru");
        boolean inetAddressReachable = ptv1.isReachable(500);
        boolean inetAddressReachable1 = ptv2.isReachable(500);
        String s = ptv1 + " is " + inetAddressReachable + ", " + ptv2 + " is " + inetAddressReachable1;
        printWriter.print(s + " " + LocalDateTime.now());
        printWriter.println();
        checkSize();
    }

    private void checkSize() throws Exception {
        File pingTv = new File("ping.tv");
        if (pingTv.length() > ConstantsFor.MBYTE) {
            FileSystemWorker.copyOrDelFile(pingTv, ".\\lan\\tv_" + System.currentTimeMillis() / 1000 + ".ping", true);
            ESender.sendM(Collections.singletonList("143500@gmail.com"), pingTv.length() / ConstantsFor.MBYTE + " is " + pingTv.getName(), ConstantsFor.getUpTime() + "\n" + new VersionInfo().toString());
        }
    }

    @Override
    public void run() {
        try {
            pingIPTV();
        } catch (Exception e) {
            new MessageLocal().errorAlert("NetMonitor", "run", e.getMessage());
            FileSystemWorker.error("NetMonitor.run", e);
        }
    }
}
