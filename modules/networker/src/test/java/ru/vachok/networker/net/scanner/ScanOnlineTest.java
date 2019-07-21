// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.Keeper;
import ru.vachok.networker.abstr.monitors.PingerService;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetPingerServiceFactory;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;


/**
 @see ScanOnline
 @since 09.06.2019 (21:38) */
@SuppressWarnings("ALL") public class ScanOnlineTest {
    
    
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
        List<String> toSortFileList = FileSystemWorker.readFileToList(new File(ConstantsFor.FILENAME_ONSCAN).getAbsolutePath());
        Collections.sort(toSortFileList);
        FileSystemWorker.writeFile(ConstantsFor.FILENAME_ONSCAN, toSortFileList.stream());
        String fileOnScanSortedAsString = FileSystemWorker.readFile(ConstantsFor.FILENAME_ONSCAN);
        Assert.assertTrue(fileOnScanSortedAsString.contains("Checked:"), fileOnScanSortedAsString);
    }
    
    @Test
    public void testIsReach() {
        Keeper netScanFiles = NetScanFileWorker.getI();
        Deque<InetAddress> dev = netScanFiles.getOnlineDevicesInetAddress();
        Assert.assertTrue(dev.size() == 0);
    
        dev.clear();
        try {
            dev.add(InetAddress.getByAddress(InetAddress.getByName("10.200.200.1").getAddress()));
        }
        catch (UnknownHostException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    
        PingerService scanOnline = new NetPingerServiceFactory();
        boolean reachableIP = false;
        InetAddress poll = dev.poll();
        reachableIP = scanOnline.isReach(poll);
        Assert.assertTrue(reachableIP, new TForms().fromArray(dev) + " is unreachable!?");
    }
    
    @Test
    public void testRun() {
        PingerService scanOnline = new ScanOnline();
        scanOnline.run();
        Assert.assertTrue(new File("ScanOnline.onList").exists());
        Assert.assertTrue(FileSystemWorker.readFile("ScanOnline.onList").contains("Checked:"));
    }
    
    @Test(enabled = false)
    public void offlineNotEmptTEST() {
        NetListKeeper NET_LIST_KEEPER = NetListKeeper.getI();
        ConcurrentMap<String, String> onLinesResolve = NET_LIST_KEEPER.getOnLinesResolve();
        SwitchesAvailability switchesAvailability = new SwitchesAvailability();
        Future<?> submit = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(switchesAvailability);
        try {
            Object swAvailObj = submit.get(ConstantsFor.DELAY * 2, TimeUnit.SECONDS);
            Assert.assertNull(swAvailObj);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        Set<String> availabilityOkIP = switchesAvailability.getOkIP();
        availabilityOkIP.forEach(x->onLinesResolve.put(x, LocalDateTime.now().toString()));
        String availOk = new TForms().fromArray(availabilityOkIP, false);
        Assert.assertTrue(availOk.contains("10.200.200.1"), availOk);
        String swAvailResultsStr = switchesAvailability.getPingResultStr();
        File fileSwAvLog = new File("sw.list.log");
        Assert.assertTrue(fileSwAvLog.exists() & fileSwAvLog.lastModified() > (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)));
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
        Assert.assertEquals(statistics, "<p>");
    }
    
    @Test
    public void testGetExecution() {
        String execution = new ScanOnline().getExecution();
        if (new File(ConstantsFor.FILENAME_ONSCAN).exists()) {
            Assert.assertFalse(execution.isEmpty());
        }
        else {
            Assert.assertTrue(execution.isEmpty());
        }
    }
    
    @Test
    public void testPingDevices() {
        try {
            List<String> strings = new ScanOnline().pingDevices(NetListKeeper.getMapAddr());
        }
        catch (TODOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testWriteLogToFile() {
        Assert.assertTrue(new ScanOnline().writeLogToFile().equals("true"));
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
    
    private void copyOfIsReach() {
        NetListKeeper NET_LIST_KEEPER = NetListKeeper.getI();
        File onlinesFile = new File(ConstantsFor.FILENAME_ONSCAN);
        String inetAddrStr = "";
        ConcurrentMap<String, String> onLinesResolve = NET_LIST_KEEPER.getOnLinesResolve();
        Map<String, String> offLines = NET_LIST_KEEPER.editOffLines();
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
    
    @Test
    public void fileOnToLastCopyTest() {
        MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
        Keeper keeper = new NetScanFileWorker();
        PingerService scanOnline = new ScanOnline();
    
        File scanOnlineLast = new File(ConstantsFor.FILENAME_ONSCAN);
        List<String> onlineLastStrings = FileSystemWorker.readFileToList(scanOnlineLast.getAbsolutePath());
        Collections.sort(onlineLastStrings);
        Collection<String> onLastAsTreeSet = new TreeSet<>(onlineLastStrings);
        Deque<InetAddress> lanFilesDeque = keeper.getOnlineDevicesInetAddress();
        List<String> maxOnList = ((ScanOnline) scanOnline).scanOnlineLastBigger();
        boolean isCopyOk = true;
        if (!new File(ConstantsFor.FILENAME_MAXONLINE).exists()) {
            isCopyOk = FileSystemWorker
                .copyOrDelFile(scanOnlineLast, Paths.get(new File(ConstantsFor.FILENAME_MAXONLINE).getAbsolutePath()).toAbsolutePath().normalize(), false);
        }
        Assert.assertTrue(isCopyOk);
    }
}