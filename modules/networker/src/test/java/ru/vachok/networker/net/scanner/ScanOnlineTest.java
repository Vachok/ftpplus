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
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.net.InetAddress;
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
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
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
        Deque<String> dev = NetScanFileWorker.getI().getDequeOfOnlineDev();
        Assert.assertFalse(dev.size() == 0);
        System.out.println("new TForms().fromArray(dev) = " + new TForms().fromArray(dev));
        dev.clear();
        dev.add("10.200.200.1 core");
        ScanOnline scanOnline = new ScanOnline();
        boolean reachableIP = scanOnline.isReach(dev.poll());
        Assert.assertTrue(reachableIP, new TForms().fromArray(dev) + " is unreachable!?");
    }
    
    @Test
    public void testRun() {
        ScanOnline scanOnline = new ScanOnline();
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
        ScanOnline scanOnline = new ScanOnline();
        
        File scanOnlineLast = new File(scanOnline.getReplaceFileNamePattern());
        List<String> onlineLastStrings = FileSystemWorker.readFileToList(scanOnlineLast.getAbsolutePath());
        Collections.sort(onlineLastStrings);
        Collection<String> onLastAsTreeSet = new TreeSet<>(onlineLastStrings);
        Deque<String> lanFilesDeque = NetScanFileWorker.getDequeOfOnlineDev();
        
        messageToUser
            .warn(scanOnline.getOnlinesFile().getName(), scanOnlineLast.getName() + " size difference", " = " + (scanOnlineLast.length() - scanOnlineLast.length()));
        scanOnline.scanOnlineLastBigger();
        boolean isCopyOk = FileSystemWorker
            .copyOrDelFile(scanOnlineLast, Paths.get(scanOnline.getFileMAXOnlines().getAbsolutePath()).toAbsolutePath().normalize(), false);
        Assert.assertTrue(isCopyOk);
    }
}