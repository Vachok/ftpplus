// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.NetKeeper;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsNet;
import ru.vachok.networker.componentsrepo.data.enums.PropertiesNames;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.net.scanner.NetLists;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.DBMessenger;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


/**
 @since 09.06.2019 (23:54)
 @see ExecScan
 */
@SuppressWarnings("ALL") public class ExecScanTest {
    
    
    private File vlanFile;
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = DBMessenger.getInstance(this.getClass().getSimpleName());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
        UsefulUtilities.ipFlushDNS();
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
    
    @Test(enabled = false)
    public void realExecScanTest() {
        List<File> scanFiles = NetKeeper.getCurrentScanFiles();
        long expectedFileSize = 6;
        for (File fileEntry : scanFiles) {
            if (fileEntry.getName().contains("220")) {
                Runnable execTest = new ExecScan(213, 214, "10.200.", fileEntry, true);
                execTest.run();
                Assert.assertTrue(fileEntry.exists());
                Assert.assertTrue((fileEntry.length() > expectedFileSize),
                    MessageFormat.format("File {0} size is smaller that {1}", fileEntry.getAbsolutePath(), expectedFileSize));
            }
        }
        Deque<InetAddress> webDeque = NetKeeper.getDequeOfOnlineDev();
        System.out.println("webDeque = " + new TForms().fromArray(webDeque));
    }
    
    @Test
    public void toStringTest() {
        Assert.assertTrue(new ExecScan().toString().contains("ExecScan["));
    }
    
    /**
     @see ExecScan#cpOldFile()
     */
    @Test
    public void cpOldFile$$COPY() {
        List<File> scanFiles = NetKeeper.getCurrentScanFiles();
        this.vlanFile = scanFiles.get(new Random().nextInt(scanFiles.size() - 1));
        long epochSec = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(3));
        String fileSepar = System.getProperty(PropertiesNames.PRSYS_SEPARATOR);
        String replaceInName = "_" + epochSec + ".scan";
        String vlanFileName = vlanFile.getName();
        vlanFileName = vlanFileName.replace(".txt", "_" + LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(3)) + ".scan");
        String toPath = ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + vlanFileName;
        Path copyPath = Paths.get(toPath).toAbsolutePath().normalize();
        if (vlanFile.length() > 5) {
            FileSystemWorker.copyOrDelFile(vlanFile, copyPath, true);
        }
    }
    
    @NotNull
    private Collection<String> getAllDevLocalDeq() {
        final int MAX_IN_ONE_VLAN = 255;
        final int IPS_IN_VELKOM_VLAN = Integer.parseInt(AppComponents.getProps().getProperty(PropertiesNames.PR_VLANNUM, "59")) * MAX_IN_ONE_VLAN;
        final BlockingDeque<String> ALL_DEVICES = new LinkedBlockingDeque<>(IPS_IN_VELKOM_VLAN);
        
        int vlanNum = IPS_IN_VELKOM_VLAN / MAX_IN_ONE_VLAN;
        AppComponents.getProps().setProperty(PropertiesNames.PR_VLANNUM, String.valueOf(vlanNum));
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
    
        if (UsefulUtilities.thisPC().equalsIgnoreCase("HOME")) {
            timeOutMSec = (int) (ConstantsFor.DELAY * 2);
            AppComponents.getUserPref().putLong(this.getClass().getSimpleName(), System.currentTimeMillis());
        }
        
        boolean isReachable = byAddress.isReachable(timeOutMSec);
        if (isReachable) {
            NetLists.getI().getOnLinesResolve().put(hostAddress, hostName);
            messageToUser.info(byAddress.toString() + " is " + true);
            getAllDevLocalDeq().add("<font color=\"green\">" + hostName + FONT_BR_CLOSE);
            stringBuilder.append(hostAddress).append(" ").append(hostName).append(ExecScan.PAT_IS_ONLINE);
        }
        else {
            NetLists.getI().editOffLines().put(byAddress.getHostAddress(), hostName);
    
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
    
    private void writeToFile() {
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(vlanFile));
             PrintStream printStream = new PrintStream(outputStream, true)) {
            printStream.println(new TForms().fromArray(NetKeeper.getCurrentScanFiles()));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}