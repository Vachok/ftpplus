// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @see DiapazonScan */
public class DiapazonScanTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private String testFilePathStr = ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "tmp" + ConstantsFor.FILESYSTEM_SEPARATOR;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    
    /**
     @see DiapazonScan#run()
     */
    @Test
    public void testRun() {
        Runnable diapazonScanRun = DiapazonScan.getInstance();
        try {
            diapazonScanRun.run();
        }
        catch (RejectedExecutionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        String instToString = diapazonScanRun.toString();
    
        Assert.assertTrue(instToString.contains("last ExecScan:"), instToString);
        Assert.assertTrue(instToString.contains("size in bytes:"), instToString);
        Assert.assertTrue(instToString.contains("<a href=\"/showalldev\">ALL_DEVICES"), instToString);
    }
    
    @Test
    public void makeFilesMapTest() {
        Map<String, File> map = copyOfMakeMap();
        String s = new TForms().fromArray(map, false);
        Assert.assertNotNull(s);
        Assert.assertTrue(s.contains("lan_11vsrv.txt"));
    }
    
    @Test
    public void testTheInfoToString() {
        System.out.println(new DiapazonScan().getExecution());
    }
    
    @Test
    public void testToString1() {
        System.out.println("DiapazonScan.getInstance().toString() = " + DiapazonScan.getInstance().toString());
    }
    
    @Test
    public void isOldFilesExistsTest() {
        DiapazonScan dsIst = DiapazonScan.getInstance();
        File fileOrig = Paths.get(testFilePathStr).toFile();
        List<String> currentScanLists = NetKeeper.getCurrentScanLists();
        for (String scanList : currentScanLists) {
            System.out.println("scanList = " + scanList);
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
        List<File> scanFiles = NetKeeper.getCurrentScanFiles();
        String fromArray = new TForms().fromArray(scanFiles);
        Assert.assertTrue(fromArray.contains("lan_210215.txt"), fromArray);
        Assert.assertTrue(fromArray.contains("lan_old0.txt"), fromArray);
        Assert.assertTrue(fromArray.contains("lan_old1.txt"), fromArray);
        Assert.assertTrue(fromArray.contains("lan_205210.txt"), fromArray);
        Assert.assertTrue(fromArray.contains("lan_200205.txt"), fromArray);
        Assert.assertTrue(fromArray.contains("lan_21vsrv.txt"), fromArray);
        Assert.assertTrue(fromArray.contains("lan_11vsrv.txt"), fromArray);
        Assert.assertTrue(fromArray.contains("lan_31vsrv.txt"), fromArray);
        Assert.assertTrue(fromArray.contains("lan_213220.txt"), fromArray);
    }
    
    @BeforeClass
    public void filesMake() {
        Path testFilePath = Paths.get(testFilePathStr);
        System.out.println(MessageFormat.format("init testpath = {0}", testFilePath.toAbsolutePath().normalize()));
        try {
            Path pathLog = Paths.get("test-lan_" + this.getClass().getSimpleName() + ".txt");
            Files.deleteIfExists(pathLog);
            testFilePath = Files.createFile(pathLog);
            this.testFilePathStr = testFilePath.toAbsolutePath().normalize().toString();
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    private @NotNull Map<String, File> copyOfMakeMap() {
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
    
        scanMap.putIfAbsent(FileNames.NEWLAN220, new File(FileNames.NEWLAN220));
        scanMap.putIfAbsent(FileNames.NEWLAN205, new File(FileNames.NEWLAN205));
        scanMap.putIfAbsent(FileNames.NEWLAN215, new File(FileNames.NEWLAN215));
        scanMap.putIfAbsent(FileNames.OLDLANTXT0, new File(FileNames.OLDLANTXT0));
        scanMap.putIfAbsent(FileNames.OLDLANTXT1, new File(FileNames.OLDLANTXT1));
        scanMap.putIfAbsent(FileNames.SERVTXT_10SRVTXT, new File(FileNames.SERVTXT_10SRVTXT));
        scanMap.putIfAbsent(FileNames.SERVTXT_21SRVTXT, new File(FileNames.SERVTXT_21SRVTXT));
        scanMap.putIfAbsent(FileNames.SERVTXT_31SRVTXT, new File(FileNames.SERVTXT_31SRVTXT));
        return scanMap;
    }
    
    private void checkIfCopied(@NotNull DiapazonScan dsIst) {
        try {
            String[] executionProcessArray = dsIst.getExecution().split("\n");
            
            File fileCopy = new File(executionProcessArray[0].replaceFirst("\n", ""));
            File fileOrig = new File(executionProcessArray[1]);
            String nameOrig = fileCopy.getName();
            String nameCopy = fileOrig.getName();
            
            System.out.println(MessageFormat.format("Copy {0} | Old {1}", nameOrig, nameCopy));
            Assert.assertFalse(nameOrig.split("\\Q.\\E")[0].equalsIgnoreCase(nameCopy.split("\\Q.\\E")[0]));
            Assert.assertTrue(fileCopy.exists());
    
        }
        catch (IndexOutOfBoundsException ignore) {
            //
        }
    }
}