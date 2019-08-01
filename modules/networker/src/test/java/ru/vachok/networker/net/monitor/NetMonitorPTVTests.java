// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @see NetMonitorPTV */
@SuppressWarnings("ALL") public class NetMonitorPTVTests {
    
    
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
    
    
    private String pingResultLast = "No pings yet.";
    
    private File pingTv = new File(ConstantsFor.FILENAME_PTV);
    
    private OutputStream outputStream;
    
    private PrintStream printStream;
    
    private Preferences preferences = AppComponents.getUserPref();
    
    /**
     @see NetMonitorPTV#run()
     */
    @Test
    public void testRun() {
        Path ptvFilePath = Paths.get(ConstantsFor.FILENAME_PTV);
        try {
            Files.deleteIfExists(ptvFilePath);
        }
        catch (IOException e) {
            ptvFilePath.toFile().deleteOnExit();
        }
        new NetMonitorPTV().run();
        Assert.assertTrue(ptvFilePath.toFile().exists() && ptvFilePath.toFile().isFile());
        Assert.assertTrue(FileSystemWorker.readFile(ptvFilePath.toAbsolutePath().normalize().toString()).contains("ptv1."), ptvFilePath.toString());
    }
    
    /**
     * @see NetMonitorPTV#toString()
     */
    @Test
    public void testToString1() {
        NetMonitorPTV netMonitorPTV = new NetMonitorPTV();
        netMonitorPTV.run();
        this.pingResultLast = netMonitorPTV.getPingResultLast();
        final StringBuilder sb = new StringBuilder("NetMonitorPTV{");
        sb.append("pingResultLast='").append(pingResultLast).append('\'');
        sb.append('}');
        Assert.assertTrue(sb.toString().contains("ptv1."));
    }
    
    @BeforeMethod
    public void createTestPTVFile() {
        File testPTV = new File(ConstantsFor.FILENAME_PTV);
        try (OutputStream outputStream = new FileOutputStream(testPTV);
             PrintStream printStream = new PrintStream(outputStream, true)) {
            printStream.println(testConfigureThreadsLogMaker.toString());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    /**
     @see NetMonitorPTV#ifPingTVIsBig()
     */
    @Test
    public void ptvIfBigTest() {
        String fileCopyPathString = "." + ConstantsFor.FILESYSTEM_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + "tv_" + System.currentTimeMillis() / 1000 + ".ping";
        Path pathToCopy = Paths.get(fileCopyPathString).toAbsolutePath().normalize();
        boolean isPingTvCopied = FileSystemWorker.copyOrDelFile(pingTv, pathToCopy, true);
        if (isPingTvCopied) {
            try {
                this.outputStream = new FileOutputStream(pingTv);
            }
            catch (FileNotFoundException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
            }
            this.printStream = new PrintStream(outputStream, true);
            preferences.put(ConstantsFor.FILENAME_PTV, new Date().toString() + "_renewed");
            try {
                preferences.sync();
            }
            catch (BackingStoreException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
            }
        }
        else {
            System.out.println(pingTv.getAbsolutePath() + " size in kb = " + pingTv.length() / ConstantsFor.KBYTE);
        }
        Assert.assertTrue(pathToCopy.toFile().exists());
        Assert.assertTrue(pingTv.exists());
        String readFileStr = FileSystemWorker.readFile(pathToCopy.toFile().getAbsolutePath());
        System.out.println("readFileStr = " + readFileStr);
    }
}