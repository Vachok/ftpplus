// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.scanner.NetListKeeper;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.DBMessenger;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;


/**
 @since 09.06.2019 (23:54)
 @see ExecScan
 */
@SuppressWarnings("ALL") public class ExecScanTest {
    
    
    private File vlanFile;
    
    private Collection<File> scanFiles = DiapazonScan.getScanFiles().values();
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new DBMessenger(this.getClass().getSimpleName());
    
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
    public void testRun() {
        File fileTestVlan = new File("test-213.scan");
        ExecScan scan = new ExecScan(213, 214, "10.200.", fileTestVlan, true);
        scan.run();
        Assert.assertTrue(fileTestVlan.exists());
        String fileAsString = FileSystemWorker.readFile(fileTestVlan.getAbsolutePath());
        Assert.assertTrue(fileAsString.contains("10.200.213.1"), fileAsString);
        fileTestVlan.deleteOnExit();
    }
    
    @Test
    public void oneIPScanTest() {
        File vlanFile = new File(ConstantsNet.FILENAME_SERVTXT_10SRVTXT);
        try {
            OutputStream outputStream = new FileOutputStream(vlanFile);
            PrintStream printStream = new PrintStream(outputStream, true);
            for (int i = 0; i < 3; i++) {
                System.out.println(oneIpScanAndPrintToFile(200, i, printStream));
            }
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test
    public void realExecScanTest() {
        Map<String, File> scanFiles = DiapazonScan.getInstance().editScanFiles();
        long expectedFileSize = 6;
        for (Map.Entry<String, File> fileEntry : scanFiles.entrySet()) {
            if (fileEntry.getKey().contains("220")) {
                Runnable execTest = new ExecScan(213, 214, "10.200.", fileEntry.getValue(), true);
                execTest.run();
                Assert.assertTrue(fileEntry.getValue().exists());
                Assert.assertTrue((fileEntry.getValue().length() > expectedFileSize),
                    MessageFormat.format("File {0} size is smaller that {1}", fileEntry.getValue().getAbsolutePath(), expectedFileSize));
            }
        }
        Deque<InetAddress> webDeque = NetScanFileWorker.getDequeOfOnlineDev();
        System.out.println("webDeque = " + new TForms().fromArray(webDeque));
    }
    
    @Test
    public void toStringTest() {
        Assert.assertTrue(new ExecScan().toString().contains("ExecScan["));
    }
    
    @Test
    public void testWithTrueFiles() {
        Map<String, File> scanFiles = DiapazonScan.getInstance().editScanFiles();
        Queue<Runnable> allExecScans = new ConcurrentLinkedQueue<>();
        ThreadPoolTaskExecutor executor = AppComponents.threadConfig().getTaskExecutor();
        
        for (Map.Entry<String, File> fileEntry : scanFiles.entrySet()) {
            Runnable runNow = new ExecScan(10, 20, "10.10.", scanFiles.get(fileEntry.getKey()));
            allExecScans.add(runNow);
        }
        
        Assert.assertTrue(allExecScans.size() == 9);
        
        while (allExecScans.iterator().hasNext()) {
            Runnable runNow = allExecScans.poll();
            executor.execute(runNow);
        }
        System.out.println(ConstantsFor.TOSTRING_EXECUTOR + executor.getThreadPoolExecutor().toString());
    }
    
    private Collection<String> getAllDevLocalDeq() {
        final int MAX_IN_ONE_VLAN = 255;
        final int IPS_IN_VELKOM_VLAN = Integer.parseInt(AppComponents.getProps().getProperty(ConstantsFor.PR_VLANNUM, "59")) * MAX_IN_ONE_VLAN;
        final BlockingDeque<String> ALL_DEVICES = new LinkedBlockingDeque<>(IPS_IN_VELKOM_VLAN);
        
        int vlanNum = IPS_IN_VELKOM_VLAN / MAX_IN_ONE_VLAN;
        AppComponents.getProps().setProperty(ConstantsFor.PR_VLANNUM, String.valueOf(vlanNum));
        return ALL_DEVICES;
    }
    
    @NotNull
    private String oneIpScanAndPrintToFile(int iThree, int jFour, PrintStream printStream) throws IOException {
        final String FILENAME_SERVTXT = "srv.txt";
        final ThreadConfig threadConfig = AppComponents.threadConfig();
        final String FONT_BR_CLOSE = "</font><br>";
        final File vlanFile = new File(ConstantsNet.FILENAME_SERVTXT_10SRVTXT);
        String whatVlan = "10.200.";
        
        threadConfig.thrNameSet(String.valueOf(iThree));
        
        int timeOutMSec = (int) ConstantsFor.DELAY;
        byte[] aBytes = InetAddress.getByName(whatVlan + iThree + "." + jFour).getAddress();
        StringBuilder stringBuilder = new StringBuilder();
        InetAddress byAddress = InetAddress.getByAddress(aBytes);
        String hostName = byAddress.getHostName();
        String hostAddress = byAddress.getHostAddress();
        
        if (ConstantsFor.thisPC().equalsIgnoreCase("HOME")) {
            timeOutMSec = (int) (ConstantsFor.DELAY * 2);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
        }
        
        boolean isReachable = byAddress.isReachable(timeOutMSec);
        if (isReachable) {
            NetListKeeper.getI().getOnLinesResolve().put(hostAddress, hostName);
            
            getAllDevLocalDeq().add("<font color=\"green\">" + hostName + FONT_BR_CLOSE);
            stringBuilder.append(hostAddress).append(" ").append(hostName).append(ExecScan.PAT_IS_ONLINE);
        }
        else {
            NetListKeeper.getI().editOffLines().put(byAddress.getHostAddress(), hostName);
            
            getAllDevLocalDeq().add("<font color=\"red\">" + hostName + FONT_BR_CLOSE);
            stringBuilder.append(hostAddress).append(" ").append(hostName);
        }
        if (stringBuilder.toString().contains(ExecScan.PAT_IS_ONLINE)) {
            {
                printStream.println(hostAddress + " " + hostName);
                System.out.println((getClass().getSimpleName() + ".oneIpScanAndPrintToFile ip online " + whatVlan + iThree + "." + jFour + vlanFile.getName() + " = " + vlanFile
                    .length() + ConstantsFor.STR_BYTES));
                
            }
        }
        return stringBuilder.toString();
    }
    
    /**
     @see ExecScan#cpOldFile()
     */
    @Test(invocationCount = 5)
    public void copyOld() {
        int fileIndexToGet = new Random().nextInt(scanFiles.size() - 1);
        Object fileObj = scanFiles.toArray()[fileIndexToGet];
        this.vlanFile = (File) fileObj;
        writeToFile();
        String fileSepar = System.getProperty(ConstantsFor.PRSYS_SEPARATOR);
        long epochSec = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(3));
        String replaceInName = "_" + epochSec + ".scan";
        Path copyPath = Paths.get(ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + vlanFile).toAbsolutePath().normalize();
        Assert.assertTrue(FileSystemWorker.copyOrDelFile(vlanFile, copyPath, true));
    }
    
    private void writeToFile() {
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(vlanFile));
             PrintStream printStream = new PrintStream(outputStream, true)) {
            printStream.println(new TForms().fromArray(scanFiles));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat
                .format("ExecScanTest.writeToFile\n{0}: {1}\nParameters: []\nReturn: void\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms()
                    .fromArray(e)));
        }
    }
}