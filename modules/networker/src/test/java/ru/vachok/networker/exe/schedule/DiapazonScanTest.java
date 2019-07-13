// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.exe.runnabletasks.ExecScan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static ru.vachok.networker.net.enums.ConstantsNet.*;


@SuppressWarnings("ALL") public class DiapazonScanTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private String testFilePathStr = Paths.get(".").toAbsolutePath().normalize().toString();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    
    /**
     @see DiapazonScan#run()
     */
    @Test
    public void testRun() {
        DiapazonScan instanceDS = DiapazonScan.getInstance();
        instanceDS.run();
        String instToString = instanceDS.toString();
        Assert.assertTrue(instToString.contains("last ExecScan:"));
        Assert.assertTrue(instToString.contains("size in bytes:"));
        Assert.assertTrue(instToString.contains("<a href=\"/showalldev\">ALL_DEVICES"));
    }
    
    @Test
    public void makeFilesMapTest() {
        Map<String, File> map = copyOfMakeMap();
        String s = new TForms().fromArray(map, false);
        Assert.assertNotNull(s);
        Assert.assertTrue(s.contains("lan_11vsrv.txt"));
    }
    
    @Test
    public void testPingSwitch() {
        try {
            List<String> pingSWList = DiapazonScan.pingSwitch();
            Assert.assertNotNull(pingSWList);
            Assert.assertTrue(pingSWList.size() == 46, pingSWList.size() + " devices in " + pingSWList.getClass().getSimpleName());
            Collections.sort(pingSWList);
            Assert.assertTrue(pingSWList.get(1).equals("10.1.1.228"));
        }
        catch (IllegalAccessException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test
    public void testTheInfoToString() {
        System.out.println(new DiapazonScan().theInfoToString());
    }
    
    @Test
    public void testToString1() {
        System.out.println("DiapazonScan.getInstance().toString() = " + DiapazonScan.getInstance().toString());
    }
    
    private Map<String, File> copyOfMakeMap() {
        Path absolutePath = Paths.get("").toAbsolutePath();
        Map<String, File> scanMap = new ConcurrentHashMap<>();
        try {
            for (File scanFile : Objects.requireNonNull(new File(absolutePath.toString()).listFiles())) {
                if (scanFile.getName().contains("lan_")) {
                    System.out.println("PUT ON INSTANCE MAP\n key: " + scanFile.getName() + "\nval: " + scanFile);
                }
            }
        }
        catch (NullPointerException e) {
            Assert.assertNull(e, e.getMessage());
        }
        
        scanMap.putIfAbsent(FILENAME_NEWLAN220, new File(FILENAME_NEWLAN220));
        scanMap.putIfAbsent(FILENAME_NEWLAN205, new File(FILENAME_NEWLAN205));
        scanMap.putIfAbsent(FILENAME_NEWLAN213, new File(FILENAME_NEWLAN213));
        scanMap.putIfAbsent(FILENAME_OLDLANTXT0, new File(FILENAME_OLDLANTXT0));
        scanMap.putIfAbsent(FILENAME_OLDLANTXT1, new File(FILENAME_OLDLANTXT1));
        scanMap.putIfAbsent(FILENAME_SERVTXT_10SRVTXT, new File(FILENAME_SERVTXT_10SRVTXT));
        scanMap.putIfAbsent(FILENAME_SERVTXT_21SRVTXT, new File(FILENAME_SERVTXT_21SRVTXT));
        scanMap.putIfAbsent(FILENAME_SERVTXT_31SRVTXT, new File(FILENAME_SERVTXT_31SRVTXT));
        return scanMap;
    }
    
    private void setScanInMin() {
        final BlockingDeque<String> allDevLocalDeq = getAllDevices();
        
        if (allDevLocalDeq.remainingCapacity() > 0 && TimeUnit.MILLISECONDS.toMinutes(getRunMin()) > 0 && allDevLocalDeq.size() > 0) {
            long scansItMin = allDevLocalDeq.size() / TimeUnit.MILLISECONDS.toMinutes(getRunMin());
            AppComponents.getProps().setProperty(ConstantsFor.PR_SCANSINMIN, String.valueOf(scansItMin));
            System.out.println(getClass().getSimpleName() + "scansItMin" + " = " + scansItMin);
            try {
                new AppComponents().updateProps();
            }
            catch (IOException e) {
                Assert.assertNull(e, e.getMessage());
            }
        }
    }
    
    private long getRunMin() {
        Preferences preferences = Preferences.userRoot();
        try {
            preferences.sync();
            return preferences.getLong(ExecScan.class.getSimpleName(), 1);
        }
        catch (BackingStoreException e) {
            InitProperties initProperties = new FileProps(ConstantsFor.PROPS_FILE_JAVA_ID);
            Properties props = initProperties.getProps();
            return Long.parseLong(props.getProperty(ExecScan.class.getSimpleName()));
        }
    }
    
    @Test
    public void scanFilesTest() {
        Map<String, File> scanFiles = DiapazonScan.getInstance().getScanFiles();
        String fromArray = new TForms().fromArray(scanFiles);
        Assert.assertTrue(scanFiles.size() == 9, fromArray);
    }
    
    @BeforeClass
    public void filesMake() {
        Path testFilePath = Paths.get(testFilePathStr);
        System.out.println(MessageFormat.format("init testpath = {0}", testFilePath.toAbsolutePath().normalize()));
        try {
            testFilePath = Files.createFile(Paths.get("test-lan_" + this.getClass().getSimpleName() + ".txt"));
            this.testFilePathStr = testFilePath.toAbsolutePath().normalize().toString();
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    @Test
    public void copyOldScansTest() {
        DiapazonScan dsIst = DiapazonScan.getInstance();
        File fileOrig = Paths.get(testFilePathStr).toFile();
        dsIst.copyOldScans(fileOrig);
        checkIfCopied(dsIst);
    }
    
    private void checkIfCopied(@NotNull DiapazonScan dsIst) {
        try {
            String[] executionProcessArray = dsIst.getExecution().split(" old file: ");
            
            File fileCopy = new File(executionProcessArray[0].replaceFirst("\n", ""));
            File fileOrig = new File(executionProcessArray[1]);
            String nameOrig = fileCopy.getName();
            String nameCopy = fileOrig.getName();
            
            System.out.println(MessageFormat.format("Copy {0} | Old {1}", nameOrig, nameCopy));
            Assert.assertFalse(nameOrig.split("\\Q.\\E")[0].equalsIgnoreCase(nameCopy.split("\\Q.\\E")[0]));
            Assert.assertTrue(fileCopy.exists());
    
        }
        catch (IndexOutOfBoundsException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
}