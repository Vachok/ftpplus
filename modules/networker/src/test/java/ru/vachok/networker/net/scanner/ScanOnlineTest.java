// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.net.monitor.PingerFromFile;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentMap;


/**
 @see ScanOnline
 @since 09.06.2019 (21:38) */
@SuppressWarnings("ALL")
public class ScanOnlineTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testGetTimeToEndStr() {
        String timeToEnd = new AppInfoOnLoad().toString();
        Assert.assertTrue(timeToEnd.contains("thisDelay="), timeToEnd);
    }
    
    @Test
    public void testGetPingResultStr() {
        List<String> toSortFileList = FileSystemWorker.readFileToList(new File(FileNames.ONSCAN).getAbsolutePath());
        Collections.sort(toSortFileList);
        FileSystemWorker.writeFile(FileNames.ONSCAN, toSortFileList.stream());
        String fileOnScanSortedAsString = FileSystemWorker.readFile(FileNames.ONSCAN);
        Assert.assertTrue(fileOnScanSortedAsString.contains("Checked:"), fileOnScanSortedAsString);
    }
    
    @Test
    public void testIsReach() {
        Deque<InetAddress> dev = NetKeeper.getDequeOfOnlineDev();
        dev.clear();
        Assert.assertTrue(dev.size() == 0);
        try {
            dev.add(InetAddress.getByAddress(InetAddress.getByName("10.200.200.1").getAddress()));
        }
        catch (UnknownHostException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    
        NetScanService scanOnline = new PingerFromFile();
        boolean reachableIP = false;
        InetAddress poll = dev.poll();
        reachableIP = NetScanService.isReach(poll.getHostAddress());
        Assert.assertTrue(reachableIP, new TForms().fromArray(dev) + " is unreachable!?");
    }
    
    @Test
    public void testRun() {
        NetScanService scanOnline = new ScanOnline();
        scanOnline.run();
        Assert.assertTrue(new File("ScanOnline.onList").exists());
        Assert.assertTrue(FileSystemWorker.readFile("ScanOnline.onList").contains("Checked:"));
    }
    
    @Test
    public void testToStringTester() {
        String newScanOnline = new AppComponents().scanOnline().toString();
        Assert.assertTrue(newScanOnline.contains("Since "), newScanOnline);
    }
    
    @Test
    public void testGetMonitoringRunnable() {
        Runnable runnable = new ScanOnline().getMonitoringRunnable();
        Assert.assertNotNull(runnable);
    }
    
    @Test
    public void testGetStatistics() {
        String statistics = new ScanOnline().getStatistics();
        Assert.assertTrue(statistics.contains("<p>"));
    }
    
    @Test
    public void testGetExecution() {
        String execution = new ScanOnline().getExecution();
        if (new File(FileNames.ONSCAN).exists()) {
            Assert.assertFalse(execution.isEmpty());
        }
        else {
            Assert.assertTrue(execution.isEmpty());
        }
    }
    
    @Test
    public void testPingDevices() {
        try {
            List<String> pingedDevices = new ScanOnline().pingDevices(NetKeeper.getMapAddr());
            Assert.assertNotNull(pingedDevices);
            if (UsefulUtilities.thisPC().toLowerCase().contains("home")) {
                Assert.assertTrue(pingedDevices.size() == 17, pingedDevices.size() + " pingedDevices: " + new TForms().fromArray(pingedDevices));
            }
            else {
                Assert.assertTrue(pingedDevices.size() == 16, pingedDevices.size() + " pingedDevices: " + new TForms().fromArray(pingedDevices));
            }
        }
        catch (TODOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testWriteLogToFile() {
        Assert.assertTrue(new ScanOnline().writeLog().equals("true"));
    }
    
    @Test
    public void testToString1() {
        String toStr = new ScanOnline().toString();
        Assert.assertTrue(toStr.contains("Максимальное кол-во онлайн адресов"), toStr);
    }
    
    @Test
    public void testGetFileMAXOnlines() {
        File onlinesMax = new ScanOnline().getFileMAXOnlines();
        Assert.assertTrue(onlinesMax.getAbsolutePath().contains("lan\\onlines.max"));
    }
    
    @Test
    public void testGetOnlinesFile() {
        File fileOn = new ScanOnline().getOnlinesFile();
        Assert.assertFalse(fileOn.getAbsolutePath().contains("lan"), fileOn.getAbsolutePath());
    }
    
    @Test
    public void testGetReplaceFileNamePattern() {
        String pattern = new ScanOnline().getReplaceFileNamePattern();
        Assert.assertEquals(pattern, "scanonline.last");
    }
    
    @Test
    public void testScanOnlineLastBigger() {
        List<String> strings = new ScanOnline().scanOnlineLastBigger();
        Assert.assertTrue(strings.size() > 1);
    }
    
    @Test
    public void fileOnToLastCopyTest() {
        MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
        NetScanService scanOnline = new ScanOnline();
        File scanOnlineLast = new File(FileNames.ONSCAN);
        List<String> onlineLastStrings = FileSystemWorker.readFileToList(scanOnlineLast.getAbsolutePath());
        Collections.sort(onlineLastStrings);
        Collection<String> onLastAsTreeSet = new TreeSet<>(onlineLastStrings);
        Deque<InetAddress> lanFilesDeque = NetKeeper.getDequeOfOnlineDev();
        List<String> maxOnList = ((ScanOnline) scanOnline).scanOnlineLastBigger();
        boolean isCopyOk = true;
        if (!new File(FileNames.ONLINES_MAX).exists()) {
            isCopyOk = FileSystemWorker
                .copyOrDelFile(scanOnlineLast, Paths.get(new File(FileNames.ONLINES_MAX).getAbsolutePath()).toAbsolutePath().normalize(), false);
        }
        Assert.assertTrue(isCopyOk);
    }
    
    private void copyOfIsReach() {
        File onlinesFile = new File(FileNames.ONSCAN);
        String inetAddrStr = "";
        ConcurrentMap<String, String> onLinesResolve = NetKeeper.getOnLinesResolve();
        Map<String, String> offLines = NetKeeper.editOffLines();
        boolean xReachable = true;
        
        try (OutputStream outputStream = new FileOutputStream(onlinesFile, true);
             PrintStream printStream = new PrintStream(outputStream)
        ) {
            byte[] addressBytes = InetAddress.getByName(inetAddrStr.split(" ")[0]).getAddress();
            InetAddress inetAddress = InetAddress.getByAddress(addressBytes);
            xReachable = inetAddress.isReachable(300);
            if (!xReachable) {
                printStream.println(inetAddrStr + " <font color=\"red\">offline</font>.");
                String removeOnline = onLinesResolve.remove(inetAddress.toString());
                if (!(removeOnline == null)) {
                    offLines.putIfAbsent(inetAddress.toString(), new Date().toString());
                    System.err.println(inetAddrStr + " offline" + " = " + removeOnline);
                }
            }
            else {
                printStream.println(inetAddrStr + " <font color=\"green\">online</font>.");
                String ifAbsent = onLinesResolve.putIfAbsent(inetAddress.toString(), LocalTime.now().toString());
                String removeOffline = offLines.remove(inetAddress.toString());
                if (!(removeOffline == null)) {
                    System.out.println(inetAddrStr + ScanOnline.STR_ONLINE + " = " + removeOffline);
                }
            }
        }
        catch (IOException | ArrayIndexOutOfBoundsException e) {
            Assert.assertNull(e, e.getMessage());
        }
        Assert.assertTrue(xReachable);
    }
}