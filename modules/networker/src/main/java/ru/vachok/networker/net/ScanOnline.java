// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.Pinger;
import ru.vachok.networker.ad.user.MoreInfoWorker;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.*;


/**
 Сканирование только тех, что он-лайн
 <p>
 
 @see DiapazonedScan
 @since 26.01.2019 (11:18) */
@Service
public class ScanOnline implements Runnable, Pinger {
    
    
    /**
     {@link NetListKeeper#getI()}
     */
    private static final NetListKeeper NET_LIST_KEEPER = NetListKeeper.getI();
    
    private static final String FILENAME_ON = ScanOnline.class.getSimpleName() + ".onList";
    
    /**
     {@link NetListKeeper#getOnLinesResolve()}
     */
    private ConcurrentMap<String, String> onLinesResolve = NET_LIST_KEEPER.getOnLinesResolve();
    
    /**
     {@link MessageLocal}
     */
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private InfoWorker tvInfo = new MoreInfoWorker("tv");
    
    private PrintStream printStream = null;
    
    @Override public String getTimeToEndStr() {
        throw new IllegalComponentStateException("18.04.2019 (11:31)");
    }
    
    @Override public String getPingResultStr() {
        return FileSystemWorker.readFile(FILENAME_ON);
    }
    
    @Override public boolean isReach(String inetAddrStr) {
        boolean xReachable = true;
        try {
            byte[] addressBytes = InetAddress.getByName(inetAddrStr.split(" ")[0]).getAddress();
            InetAddress inetAddress = InetAddress.getByAddress(addressBytes);
            xReachable = inetAddress.isReachable(100);
            if (!xReachable) {
                NET_LIST_KEEPER.getOffLines().put(inetAddress.toString(), LocalTime.now().toString());
                if (onLinesResolve.containsKey(inetAddress.toString())) {
                    NET_LIST_KEEPER.getOffLines().remove(inetAddress.toString());
                }
                printStream.println(inetAddrStr + " is offline. Checked: " + new Date());
            }
            else {
                printStream.println(inetAddrStr);
                onLinesResolve.putIfAbsent(inetAddress.toString(), LocalTime.now().toString());
            }
        }
        catch (IOException | ArrayIndexOutOfBoundsException e) {
            messageToUser.error(e.getMessage());
        }
        return xReachable;
    }
    
    
    @Override
    public void run() {
        AppComponents.threadConfig().execByThreadConfig(this::offlineNotEmptyActions);
        try {
            OutputStream outputStream = new FileOutputStream(FILENAME_ON);
            this.printStream = new PrintStream(outputStream);
            Deque<String> onList = NetScanFileWorker.getI().getListOfOnlineDev();
            while (!onList.isEmpty()) {
                isReach(onList.poll());
            }
        }
        catch (IOException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".run", e));
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<b>Since ").append("<i>").append(new Date(NetScanFileWorker.getI().getLastStamp())).append("</i>").append(tvInfo.getInfoAbout()).append("</b><br><br>");
        sb.append("Offline pc is <font color=\"red\"><b>").append(NET_LIST_KEEPER.getOffLines().size()).append(":</b></font><br>");
        sb.append("Online  pc is<font color=\"#00ff69\"> <b>").append(onLinesResolve.size()).append(":</b><br>").append(new TForms().fromArray(onLinesResolve, true)).append("</font><br>");
        return sb.toString();
    }
    
    
    private void offlineNotEmptyActions() {
        SwitchesAvailability switchesAvailability = new SwitchesAvailability();
        Future<?> submit = AppComponents.threadConfig().getTaskExecutor().submit(switchesAvailability);
        try {
            submit.get(ConstantsFor.DELAY * 2, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            messageToUser.error(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        
        Set<String> availabilityOkIP = switchesAvailability.getOkIP();
        availabilityOkIP.forEach(x->onLinesResolve.put(x, LocalDateTime.now().toString()));
    }
}