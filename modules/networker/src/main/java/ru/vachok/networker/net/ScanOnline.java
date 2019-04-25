// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.Pinger;
import ru.vachok.networker.ad.user.MoreInfoWorker;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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
    
    private static final String FILEEXT_ONLIST = ".onList";
    
    private static final String FILENAME_ON = ScanOnline.class.getSimpleName() + FILEEXT_ONLIST;
    
    /**
     {@link NetListKeeper#getOnLinesResolve()}
     */
    private ConcurrentMap<String, String> onLinesResolve = NET_LIST_KEEPER.getOnLinesResolve();
    
    /**
     {@link MessageLocal}
     */
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private InfoWorker tvInfo = new MoreInfoWorker("tv");
    
    private PrintStream printStream;
    
    private List<String> maxOnList;
    
    public ScanOnline() {
        try {
            this.maxOnList = FileSystemWorker.readFileToList(new File(FILENAME_ON).getAbsolutePath().replace(FILENAME_ON, "\\lan\\max.online"));
        }
        catch (NullPointerException e) {
            this.maxOnList = new ArrayList<>();
        }
    }
    
    @Override public String getTimeToEndStr() {
        return new AppInfoOnLoad().toString();
    }
    
    @Override public String getPingResultStr() {
        return FileSystemWorker.readFile(FILENAME_ON);
    }
    
    @Override public boolean isReach(String inetAddrStr) {
        ConcurrentMap<String, String> offLines = NET_LIST_KEEPER.getOffLines();
        boolean xReachable = true;
        try {
            byte[] addressBytes = InetAddress.getByName(inetAddrStr.split(" ")[0]).getAddress();
            InetAddress inetAddress = InetAddress.getByAddress(addressBytes);
            xReachable = inetAddress.isReachable(100);
            if (!xReachable) {
                String removeOnline = onLinesResolve.remove(inetAddress.toString());
                printStream.println(inetAddrStr + " <font color=\"red\">offline</font>.");
                if (!(removeOnline == null)) {
                    offLines.putIfAbsent(inetAddress.toString(), new Date().toString());
                    messageToUser.warn(inetAddrStr, " offline", " = " + removeOnline);
                }
            }
            else {
                printStream.println(inetAddrStr + " <font color=\"green\">online</font>.");
                String ifAbsent = onLinesResolve.putIfAbsent(inetAddress.toString(), LocalTime.now().toString());
                String removeOffline = offLines.remove(inetAddress.toString());
                if (!(removeOffline == null)) {
                    messageToUser.info(inetAddrStr, "online", " = " + removeOffline);
                }
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
        File onlinesFile = new File(FILENAME_ON);
        File fileMAX = new File(onlinesFile.toPath().toAbsolutePath().toString().replace(FILENAME_ON, "\\lan\\max.online"));
        
        if (onlinesFile.exists()) {
            String replaceStr = onlinesFile.getAbsolutePath().replace(FILEEXT_ONLIST, ".last");
            File repFile = new File(replaceStr);
            List<String> stringsLastScan = FileSystemWorker.readFileToList(repFile.getAbsolutePath());
            try {
                if (stringsLastScan.size() < NetScanFileWorker.getI().getListOfOnlineDev().size()) {
                    FileSystemWorker.copyOrDelFile(onlinesFile, replaceStr, false);
                }
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
            if (repFile.length() > fileMAX.length()) {
                messageToUser.warn(repFile.getName(), fileMAX.getName() + " size difference", " = " + (repFile.length() - fileMAX.length()));
                List<String> readFileToList = FileSystemWorker.readFileToList(fileMAX.getAbsolutePath());
                readFileToList.stream().forEach(x->maxOnList.add(x));
                FileSystemWorker.copyOrDelFile(repFile, fileMAX.getAbsolutePath(), false);
            }
            repFile.deleteOnExit();
        }
        try {
            OutputStream outputStream = new FileOutputStream(onlinesFile);
            this.printStream = new PrintStream(outputStream);
            Deque<String> onList = NetScanFileWorker.getI().getListOfOnlineDev();
            printStream.println("Checked: " + new Date());
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
        sb.append("<b>ipconfig /flushdns = </b>").append(new String(AppComponents.ipFlushDNS().getBytes(), Charset.forName("IBM866"))).append("<br>");
        sb.append("Offline pc is <font color=\"red\"><b>").append(NET_LIST_KEEPER.getOffLines().size()).append(":</b></font><br>");
        sb.append("Online  pc is<font color=\"#00ff69\"> <b>").append(onLinesResolve.size()).append(":</b><br>");
        sb.append(new TForms().fromArray(onLinesResolve, true)).append("</font><br>");
        sb.append("<details><summary>Максимальное кол-во онлайн адресов: " + maxOnList.size() + "</summary>" + new TForms().fromArray(maxOnList, true) + "</details>");
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