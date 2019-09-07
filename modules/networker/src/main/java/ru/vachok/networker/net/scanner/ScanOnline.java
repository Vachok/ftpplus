// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.NetKeeper;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.FileNames;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.net.monitor.ExecScan;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/**
 Сканирование только тех, что он-лайн
 <p>
 
 @see ru.vachok.networker.net.scanner.ScanOnlineTest
 @since 26.01.2019 (11:18) */
@Service
public class ScanOnline implements NetScanService {
    
    
    protected static final String STR_ONLINE = "online";
    
    private List<String> maxOnList = new ArrayList<>();
    
    private @NotNull File fileMAXOnlines = new File(FileNames.MAXONLINE);
    
    private File onlinesFile = new File(FileNames.FILENAME_ONSCAN);
    
    private CheckerIpHTML checkerIpHTML;
    
    /**
     {@link MessageLocal}
     */
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.DB, getClass().getSimpleName());
    
    private InformationFactory tvInfo = InformationFactory.getInstance(InformationFactory.TV);
    
    private String replaceFileNamePattern;
    
    public ScanOnline() {
        initialMeth();
    }
    
    @Override
    public String getExecution() {
        return FileSystemWorker.readFile(onlinesFile.getAbsolutePath());
    }
    
    @Override
    public void run() {
        AppComponents.threadConfig().execByThreadConfig(SwitchesAvailability::new);
        
        setMaxOnlineListFromFile();
        
        if (onlinesFile.exists()) {
            onListFileCopyToLastAndMax();
        }
        messageToUser.info(String.valueOf(writeOnLineFile()), "writeOnLineFile: ", " = " + onlinesFile.getAbsolutePath());
    }
    
    @Override
    public Runnable getMonitoringRunnable() {
        return this;
    }
    
    @Override
    public String getStatistics() {
        Set<String> filesOnLineRead = new TreeSet<>(NetKeeper.getCurrentScanLists());
        return new TForms().fromArray(filesOnLineRead, true);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<b>Since ");
        sb.append("<i>");
        sb.append(new Date(AppComponents.getUserPref().getLong(ExecScan.class.getSimpleName(), UsefulUtilities.getMyTime())));
        sb.append(" last ExecScan: ");
        sb.append("</i>");
        sb.append(tvInfo.getInfoAbout("tv"));
        sb.append("</b><br><br>");
        sb.append("<details><summary>Максимальное кол-во онлайн адресов: ").append(maxOnList.size()).append("</summary>")
                .append(new TForms().fromArray(maxOnList, true))
                .append(ConstantsFor.HTMLTAG_DETAILSCLOSE);
        sb.append("<b>ipconfig /flushdns = </b>").append(new String(UsefulUtilities.ipFlushDNS().getBytes(), Charset.forName("IBM866"))).append("<br>");
        sb.append(checkerIpHTML);
        return sb.toString();
    }
    
    private void initialMeth() {
        this.onlinesFile = new File(FileNames.FILENAME_ONSCAN);
        this.replaceFileNamePattern = onlinesFile.getName().toLowerCase().replace(".onlist", ".last");
        String fileMaxName = FileNames.MAXONLINE;
        this.fileMAXOnlines = new File(fileMaxName);
    
        maxOnList = FileSystemWorker.readFileToList(fileMAXOnlines.getAbsolutePath());
    }
    
    @Override
    public List<String> pingDevices(Map<InetAddress, String> ipAddressAndDeviceNameToShow) {
        List<String> pingedDevices = new ArrayList<>();
        for (Map.Entry<InetAddress, String> addressStringEntry : ipAddressAndDeviceNameToShow.entrySet()) {
            String entryValue = addressStringEntry.getValue();
            InetAddress entryKey = addressStringEntry.getKey();
            boolean reach = NetScanService.isReach(entryKey.getHostAddress());
            pingedDevices.add(entryValue + " " + reach);
        }
        Collections.sort(pingedDevices);
        return pingedDevices;
    }
    
    private void setMaxOnlineListFromFile() {
        try {
            this.maxOnList = FileSystemWorker.readFileToList(fileMAXOnlines.getAbsolutePath());
        }
        catch (NullPointerException e) {
            this.maxOnList = new ArrayList<>();
        }
    }
    
    @Override
    public String getPingResultStr() {
        Deque<InetAddress> address = NetKeeper.getDequeOfOnlineDev();
        return new TForms().fromArray(address, true);
    }
    
    @Override
    public String writeLog() {
        return String.valueOf(writeOnLineFile());
    }
    
    private void onListFileCopyToLastAndMax() {
        File scanOnlineLast = new File(replaceFileNamePattern);
        if (!scanOnlineLast.exists()) {
            FileSystemWorker.copyOrDelFile(onlinesFile, Paths.get(replaceFileNamePattern).toAbsolutePath().normalize(), false);
        }
        List<String> onlineLastStrings = FileSystemWorker.readFileToList(scanOnlineLast.getAbsolutePath());
        Collection<String> onLastAsTreeSet = new TreeSet<>(onlineLastStrings);
        Deque<InetAddress> lanFilesDeque = NetKeeper.getDequeOfOnlineDev();
    
        if (onLastAsTreeSet.size() < NetKeeper.getDequeOfOnlineDev().size()) { //скопировать ScanOnline.onList в ScanOnline.last
            FileSystemWorker.copyOrDelFile(onlinesFile, Paths.get(replaceFileNamePattern).toAbsolutePath().normalize(), false);
        }
        if (scanOnlineLast.length() > fileMAXOnlines.length()) {
            messageToUser.warn(onlinesFile.getName(), scanOnlineLast.getName() + " size difference", " = " + (scanOnlineLast.length() - scanOnlineLast.length()));
            scanOnlineLastBigger();
            boolean isCopyOk = FileSystemWorker.copyOrDelFile(scanOnlineLast, Paths.get(fileMAXOnlines.getAbsolutePath()).toAbsolutePath().normalize(), false);
        }
        scanOnlineLast.deleteOnExit(); //удалить ScanOnline.last при выходе.
    }
    
    /**
     когда размер в байтах файла ScanOnline.last, больше чем \lan\max.online, добавить содержание max.online в список maxOnList
     
     @since 12.07.2019 (22:56)
     */
    protected List<String> scanOnlineLastBigger() {
        List<String> readFileToList = FileSystemWorker.readFileToList(fileMAXOnlines.getAbsolutePath());
        this.maxOnList.addAll(readFileToList);
        Collections.sort(maxOnList);
        return maxOnList;
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
            Deque<InetAddress> onDeq = NetKeeper.getDequeOfOnlineDev();
            printStream.println("Checked: " + new Date());
            while (!onDeq.isEmpty()) {
                InetAddress inetAddrPool = onDeq.poll();
                printStream.println(inetAddrPool.toString() + " " + NetScanService.isReach(inetAddrPool.getHostAddress()));
            }
            retBool = true;
        }
        catch (IOException e) {
            retBool = false;
        }
        return retBool;
    }
    
    protected File getFileMAXOnlines() {
        return fileMAXOnlines;
    }
    
    protected File getOnlinesFile() {
        return onlinesFile;
    }
    
    /**
     @return {@link #replaceFileNamePattern}
     
     @see ru.vachok.networker.net.scanner.ScanOnlineTest#fileOnToLastCopyTest()
     @since 12.07.2019 (23:08)
     */
    protected String getReplaceFileNamePattern() {
        return replaceFileNamePattern;
    }
    
    private boolean isReach(String hostAddress) {
        boolean xReachable = false;
        try (OutputStream outputStream = new FileOutputStream(onlinesFile, true);
             PrintStream printStream = new PrintStream(outputStream);
        ) {
            this.checkerIpHTML = new CheckerIpHTML(hostAddress, printStream);
            xReachable = this.checkerIpHTML.checkIP();
        }
        catch (IOException | ArrayIndexOutOfBoundsException e) {
            messageToUser.error(e.getMessage());
        }
        
        return xReachable;
    }
    
}
