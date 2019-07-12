// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.monitors.Pinger;
import ru.vachok.networker.ad.user.MoreInfoWorker;
import ru.vachok.networker.exe.runnabletasks.ExecScan;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.InfoWorker;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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
    
    private final String sep = ConstantsFor.FILESYSTEM_SEPARATOR;
    
    private File onlinesFile;
    
    private CheckerIp checkerIp;
    
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
    
    @Override
    public String getTimeToEndStr() {
        return new AppInfoOnLoad().toString();
    }
    
    @Override
    public String getPingResultStr() {
        return FileSystemWorker.readFile(ConstantsFor.FILENAME_ONSCAN);
    }
    
    @Override
    public boolean isReach(String inetAddrStr) {
        boolean xReachable = true;
    
        try (OutputStream outputStream = new FileOutputStream(onlinesFile, true);
             PrintStream printStream = new PrintStream(outputStream);
        ) {
            this.checkerIp = new CheckerIp(inetAddrStr, printStream);
            xReachable = checkerIp.checkIP();
        }
        catch (IOException | ArrayIndexOutOfBoundsException e) {
            messageToUser.error(e.getMessage());
        }
        
        return xReachable;
    }
    
    @Override
    public String writeLogToFile() {
        return String.valueOf(writeOnLineFile());
    }
    
    @Override
    public void run() {
        setMaxOnlineListFromFile();
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
        sb.append(checkerIp);
        return sb.toString();
    }
    
    private boolean writeOnLineFile() {
        boolean retBool;
        try {
            Files.deleteIfExists(onlinesFile.toPath());
        }
        catch (IOException e) {
            onlinesFile.deleteOnExit();
        }
        try (OutputStream outputStream = new FileOutputStream(onlinesFile);
             PrintStream printStream = new PrintStream(outputStream, true)
        ) {
            Deque<String> onDeq = NetScanFileWorker.getDequeOfOnlineDev();
            printStream.println("Checked: " + new Date());
            while (!onDeq.isEmpty()) {
                isReach(onDeq.poll());
            }
            retBool = true;
        }
        catch (IOException e) {
            retBool = false;
        }
        return retBool;
    }
    
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
    
    private void onListFileCopyToLastAndMax(File onlinesFileLoc, File fileMAX) {
        String replaceFileNamePattern = COMPILE.matcher(onlinesFileLoc.getAbsolutePath()).replaceAll(Matcher.quoteReplacement(".last"));
        File scanOnlineLast = new File(replaceFileNamePattern);
        List<String> onlineLastStrings = FileSystemWorker.readFileToList(scanOnlineLast.getAbsolutePath());
        Collections.sort(onlineLastStrings);
        Collection<String> onLastAsTreeSet = new TreeSet<>(onlineLastStrings);
        Deque<String> lanFilesDeque = NetScanFileWorker.getDequeOfOnlineDev();
    
        if (onLastAsTreeSet.size() < lanFilesDeque.size()) { //скопировать ScanOnline.onList в ScanOnline.last
            FileSystemWorker.copyOrDelFile(onlinesFileLoc, Paths.get(replaceFileNamePattern).toAbsolutePath().normalize(), false);
        }
        if (scanOnlineLast.length() > fileMAX.length()) { //когда размер в байтах файла ScanOnline.last, больше чем \lan\max.online, добавить содержание max.online в список maxOnList
            messageToUser.warn(scanOnlineLast.getName(), fileMAX.getName() + " size difference", " = " + (scanOnlineLast.length() - fileMAX.length()));
            List<String> readFileToList = FileSystemWorker.readFileToList(fileMAX.getAbsolutePath());
            this.maxOnList.addAll(readFileToList);
            Collections.sort(maxOnList);
            FileSystemWorker.copyOrDelFile(scanOnlineLast, Paths.get(fileMAX.getAbsolutePath()).toAbsolutePath().normalize(), false); //скопировать ScanOnline.last в \lan\max.online
        }
        scanOnlineLast.deleteOnExit(); //удалить ScanOnline.last при выходе.
    }
    
}
