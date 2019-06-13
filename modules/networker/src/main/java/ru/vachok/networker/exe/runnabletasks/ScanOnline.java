// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.Pinger;
import ru.vachok.networker.ad.user.MoreInfoWorker;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.InfoWorker;
import ru.vachok.networker.net.NetListKeeper;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.net.SwitchesAvailability;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 Сканирование только тех, что он-лайн
 <p>
 
 @see DiapazonScan
 @since 26.01.2019 (11:18) */
@Service
public class ScanOnline implements Runnable, Pinger {
    
    
    private static final Pattern COMPILE = Pattern.compile(ConstantsFor.FILEEXT_ONLIST, Pattern.LITERAL);
    
    private File onlinesFile;
    
    /**
     {@link NetListKeeper#getI()}
     */
    private static final NetListKeeper NET_LIST_KEEPER = AppComponents.netKeeper();
    
    private final String sep = ConstantsFor.FILESYSTEM_SEPARATOR;
    
    private final ThreadConfig threadConfig = AppComponents.threadConfig();
    
    /**
     {@link NetListKeeper#getOnLinesResolve()}
     */
    private ConcurrentMap<String, String> onLinesResolve = NET_LIST_KEEPER.getOnLinesResolve();
    
    /**
     {@link MessageLocal}
     */
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private InfoWorker tvInfo = new MoreInfoWorker("tv");
    
    private List<String> maxOnList = FileSystemWorker.readFileToList(new File(new File(ConstantsFor.FILENAME_ONSCAN).getAbsolutePath()
        .replace(ConstantsFor.FILENAME_ONSCAN, "lan" + sep + ConstantsFor.FILENAME_MAXONLINE)).getAbsolutePath());
    
    public ScanOnline() {
        this.onlinesFile = new File(ConstantsFor.FILENAME_ONSCAN);
    }
    
    @Override public String getTimeToEndStr() {
        return new AppInfoOnLoad().toString();
    }
    
    @Override public String getPingResultStr() {
        return FileSystemWorker.readFile(ConstantsFor.FILENAME_ONSCAN);
    }
    
    @Override public boolean isReach(String inetAddrStr) {
        Map<String, String> offLines = NET_LIST_KEEPER.getOffLines();
        boolean xReachable = true;
        try (OutputStream outputStream = new FileOutputStream(onlinesFile, true);
             PrintStream printStream = new PrintStream(outputStream)) {
            byte[] addressBytes = InetAddress.getByName(inetAddrStr.split(" ")[0]).getAddress();
            InetAddress inetAddress = InetAddress.getByAddress(addressBytes);
            xReachable = inetAddress.isReachable(ConstantsFor.TIMEOUT_650 / 2);
            if (!xReachable) {
                printStream.println(inetAddrStr + " <font color=\"red\">offline</font>.");
                String removeOnline = onLinesResolve.remove(inetAddress.toString());
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
        NET_LIST_KEEPER.setOffLines(offLines);
        return xReachable;
    }
    
    @Override
    public void run() {
        setMaxOnlineListFromFile();
        threadConfig.execByThreadConfig(this::checkSwitchesAvail);
        File fileMAX = new File(onlinesFile.toPath().toAbsolutePath().toString().replace(ConstantsFor.FILENAME_ONSCAN, sep + "lan" + sep + ConstantsFor.FILENAME_MAXONLINE));
    
        if (onlinesFile.exists()) {
            onListFileCopyToLastAndMax(onlinesFile, fileMAX);
        }
        messageToUser.info(String.valueOf(writeOnLineFile()), "writeOnLineFile: ", " = " + onlinesFile.getAbsolutePath());
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<b>Since ");
        sb.append("<i>");
        sb.append(new Date(AppComponents.getUserPref().getLong(ExecScan.class.getSimpleName(), ConstantsFor.getMyTime())));
        sb.append(" last ExecScan: ");
        sb.append("</i>");
        sb.append(tvInfo.getInfoAbout());
        sb.append("</b><br><br>");
        sb.append("<details><summary>Максимальное кол-во онлайн адресов: ").append(maxOnList.size()).append("</summary>").append(new TForms().fromArray(maxOnList, true))
            .append(ConstantsFor.HTMLTAG_DETAILSCLOSE);
        sb.append("<b>ipconfig /flushdns = </b>").append(new String(AppComponents.ipFlushDNS().getBytes(), Charset.forName("IBM866"))).append("<br>");
        sb.append("Offline pc is <font color=\"red\"><b>").append(NET_LIST_KEEPER.getOffLines().size()).append(":</b></font><br>");
        sb.append("Online  pc is<font color=\"#00ff69\"> <b>").append(onLinesResolve.size()).append(":</b><br>");
        sb.append(new TForms().fromArray(onLinesResolve, true)).append("</font><br>");
        return sb.toString();
    }
    
    /**
     Заполнение {@link #maxOnList} данными из файла C:\Users\ikudryashov\IdeaProjects\ftpplus\modules\networker\lan\max.online
     */
    private void setMaxOnlineListFromFile() {
        try {
            File onFile = new File(ConstantsFor.FILENAME_ONSCAN);
            String newPath = onFile.getAbsolutePath().replace(ConstantsFor.FILENAME_ONSCAN, "lan" + sep + ConstantsFor.FILENAME_MAXONLINE);
            
            this.maxOnList = FileSystemWorker.readFileToList(newPath);
        }
        catch (NullPointerException e) {
            this.maxOnList = new ArrayList<>();
        }
    }
    
    /**
     Пишет {@code ScanOnline.onList} из {@link NetScanFileWorker#getDequeOfOnlineDev()}, проверяя на доступность.
     <p>
     
     @return записано успешно.
     
     @see ScanOnline#isReach(java.lang.String)
     */
    private boolean writeOnLineFile() {
        boolean retBool;
        try {
            Files.deleteIfExists(onlinesFile.toPath());
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        try (OutputStream outputStream = new FileOutputStream(onlinesFile);
             PrintStream printStream = new PrintStream(outputStream, true)) {
            Deque<String> onDeq = NetScanFileWorker.getDequeOfOnlineDev();
            printStream.println("Checked: " + new Date());
            while (!onDeq.isEmpty()) {
                isReach(onDeq.poll());
            }
            retBool = true;
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
            retBool = false;
        }
        return retBool;
    }
    
    /**
     Анализ файла {@code ScanOnline.last} и его копирование в {@code ScanOnline.last} или {@code \lan\max.online}, при необходимости.
     <p>
     {@code replaceStr} = C:\Users\ikudryashov\IdeaProjects\ftpplus\modules\networker\ScanOnline.last <br>
     {@code scanOnlineLast} = C:\Users\ikudryashov\IdeaProjects\ftpplus\modules\networker\ScanOnline.last (stringsLastScan) <br>
     {@code lastScanTreeSet} = {@code repFile}, отсортированный как {@link TreeSet} <br>
     <p>
     @param onlinesFileLoc ScanOnline.onList
     @param fileMAX C:\Users\ikudryashov\IdeaProjects\ftpplus\modules\networker\lan\max.online
     */
    private void onListFileCopyToLastAndMax(File onlinesFileLoc, File fileMAX) {
        String replaceFileNamePattern = COMPILE.matcher(onlinesFileLoc.getAbsolutePath()).replaceAll(Matcher.quoteReplacement(".last"));
        File scanOnlineLast = new File(replaceFileNamePattern);
        List<String> onlineLastStrings = FileSystemWorker.readFileToList(scanOnlineLast.getAbsolutePath());
        Collections.sort(onlineLastStrings);
        Collection<String> onLastAsTreeSet = new TreeSet<>(onlineLastStrings);
        Deque<String> lanFilesDeque = NetScanFileWorker.getDequeOfOnlineDev();
    
        if (onLastAsTreeSet.size() < lanFilesDeque.size()) { //скопировать ScanOnline.onList в ScanOnline.last
            FileSystemWorker.copyOrDelFile(onlinesFileLoc, replaceFileNamePattern, false);
        }
        if (scanOnlineLast.length() > fileMAX.length()) { //когда размер в байтах файла ScanOnline.last, больше чем \lan\max.online, добавить содержание max.online в список maxOnList
            messageToUser.warn(scanOnlineLast.getName(), fileMAX.getName() + " size difference", " = " + (scanOnlineLast.length() - fileMAX.length()));
            List<String> readFileToList = FileSystemWorker.readFileToList(fileMAX.getAbsolutePath());
            this.maxOnList.addAll(readFileToList);
            Collections.sort(maxOnList);
            FileSystemWorker.copyOrDelFile(scanOnlineLast, fileMAX.getAbsolutePath(), false); //скопировать ScanOnline.last в \lan\max.online
        }
        scanOnlineLast.deleteOnExit(); //удалить ScanOnline.last при выходе.
    }
    
    /**
     * Проверка доступности свичей.
     */
    private void checkSwitchesAvail() {
//        messageToUser.info("ПИНГ СВИЧЕЙ");
        Pinger switchesAvailability = new SwitchesAvailability();
        Future<?> submit = threadConfig.getTaskExecutor().submit((Runnable) switchesAvailability);
        try {
            submit.get(ConstantsFor.DELAY * 2, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            messageToUser.error(e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    
        Set<String> availabilityOkIP = ((SwitchesAvailability) switchesAvailability).getOkIP();
        availabilityOkIP.forEach(x->onLinesResolve.put(x, LocalDateTime.now().toString()));
    }
}
