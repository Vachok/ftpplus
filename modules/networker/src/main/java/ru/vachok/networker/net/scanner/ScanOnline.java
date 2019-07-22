// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AbstractNetworkerFactory;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.Keeper;
import ru.vachok.networker.abstr.monitors.NetFactory;
import ru.vachok.networker.abstr.monitors.PingerService;
import ru.vachok.networker.ad.user.MoreInfoWorker;
import ru.vachok.networker.exe.runnabletasks.ExecScan;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.InfoWorker;
import ru.vachok.networker.net.NetPingerServiceFactory;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.restapi.message.DBMessenger;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;


/**
 Сканирование только тех, что он-лайн
 <p>
 
 @see ru.vachok.networker.net.scanner.ScanOnlineTest
 @since 26.01.2019 (11:18) */
@Service
public class ScanOnline implements PingerService {
    
    
    private static final Pattern COMPILE = Pattern.compile(ConstantsFor.FILEEXT_ONLIST, Pattern.LITERAL);
    
    protected static final String STR_ONLINE = "online";
    
    private final String ss = ConstantsFor.FILESYSTEM_SEPARATOR;
    
    private List<String> maxOnList = new ArrayList<>();
    
    private @NotNull File fileMAXOnlines = new File(ConstantsFor.FILENAME_MAXONLINE);
    
    private File onlinesFile = new File(ConstantsFor.FILENAME_ONSCAN);
    
    private PingerService checkerIp = new CheckerIp();
    
    /**
     {@link MessageLocal}
     */
    private MessageToUser messageToUser = new DBMessenger(getClass().getSimpleName());
    
    private InfoWorker tvInfo = new MoreInfoWorker("tv");
    
    private Keeper netFilesKeeper = new NetScanFileWorker();
    
    private String replaceFileNamePattern;
    
    public ScanOnline() {
        initialMeth();
    }
    
    @Override
    public void run() {
        AppComponents.threadConfig().execByThreadConfig(()->NetListKeeper.getI().checkSwitchesAvail());
        
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
        Set<String> filesOnLineRead = new TreeSet<>();
        filesOnLineRead.addAll(DiapazonScan.getCurrentPingStats());
        return new TForms().fromArray(filesOnLineRead, true);
    }
    
    @Override
    public String getExecution() {
        return FileSystemWorker.readFile(onlinesFile.getAbsolutePath());
    }
    
    @Override
    public List<String> pingDevices(Map<InetAddress, String> ipAddressAndDeviceNameToShow) {
        NetFactory netFactory = (NetFactory) AbstractNetworkerFactory.getInstance(NetPingerServiceFactory.class.getTypeName());
        return netFactory.pingDevices(ipAddressAndDeviceNameToShow);
    }
    
    @Override
    public String getPingResultStr() {
        Deque<InetAddress> address = netFilesKeeper.getOnlineDevicesInetAddress();
        return new TForms().fromArray(address, true);
    }
    
    private boolean isReach(String hostAddress) {
        boolean xReachable = true;
    
        try (OutputStream outputStream = new FileOutputStream(onlinesFile, true);
             PrintStream printStream = new PrintStream(outputStream);
        ) {
            xReachable = ((CheckerIp) this.checkerIp).checkIP();
        }
        catch (IOException | ArrayIndexOutOfBoundsException e) {
            messageToUser.error(e.getMessage());
        }
        
        return xReachable;
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
    
    @Override
    public boolean isReach(@NotNull InetAddress inetAddrStr) {
        return isReach(inetAddrStr.getHostAddress());
    }
    
    @Override
    public String writeLogToFile() {
        return String.valueOf(writeOnLineFile());
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
        sb.append("<details><summary>Максимальное кол-во онлайн адресов: ").append(maxOnList.size()).append("</summary>")
            .append(new TForms().fromArray(maxOnList, true))
            .append(ConstantsFor.HTMLTAG_DETAILSCLOSE);
        sb.append("<b>ipconfig /flushdns = </b>").append(new String(AppComponents.ipFlushDNS().getBytes(), Charset.forName("IBM866"))).append("<br>");
        sb.append(checkerIp);
        return sb.toString();
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
    
    private void initialMeth() {
        this.onlinesFile = new File(ConstantsFor.FILENAME_ONSCAN);
        this.replaceFileNamePattern = onlinesFile.getName().toLowerCase().replace(".onlist", ".last");
        String fileMaxName = ConstantsFor.FILENAME_MAXONLINE;
        this.fileMAXOnlines = new File(fileMaxName);
    
        maxOnList = FileSystemWorker.readFileToList(fileMAXOnlines.getAbsolutePath());
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
            Deque<InetAddress> onDeq = netFilesKeeper.getOnlineDevicesInetAddress();
            printStream.println("Checked: " + new Date());
            while (!onDeq.isEmpty()) {
                InetAddress inetAddrPool = onDeq.poll();
                printStream.println(inetAddrPool.toString() + " " + isReach(inetAddrPool));
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
            this.maxOnList = FileSystemWorker.readFileToList(fileMAXOnlines.getAbsolutePath());
        }
        catch (NullPointerException e) {
            this.maxOnList = new ArrayList<>();
        }
    }
    
    private void onListFileCopyToLastAndMax() {
        File scanOnlineLast = new File(replaceFileNamePattern);
        List<String> onlineLastStrings = FileSystemWorker.readFileToList(scanOnlineLast.getAbsolutePath());
        Collections.sort(onlineLastStrings);
        Collection<String> onLastAsTreeSet = new TreeSet<>(onlineLastStrings);
        Deque<InetAddress> lanFilesDeque = netFilesKeeper.getOnlineDevicesInetAddress();
    
        if (onLastAsTreeSet.size() < netFilesKeeper.getOnlineDevicesInetAddress().size()) { //скопировать ScanOnline.onList в ScanOnline.last
            FileSystemWorker.copyOrDelFile(onlinesFile, Paths.get(replaceFileNamePattern).toAbsolutePath().normalize(), false);
        }
        if (scanOnlineLast.length() > fileMAXOnlines.length()) {
            messageToUser.warn(onlinesFile.getName(), scanOnlineLast.getName() + " size difference", " = " + (scanOnlineLast.length() - scanOnlineLast.length()));
            scanOnlineLastBigger();
            boolean isCopyOk = FileSystemWorker.copyOrDelFile(scanOnlineLast, Paths.get(fileMAXOnlines.getAbsolutePath()).toAbsolutePath().normalize(), false);
        }
        scanOnlineLast.deleteOnExit(); //удалить ScanOnline.last при выходе.
    }
    
}
