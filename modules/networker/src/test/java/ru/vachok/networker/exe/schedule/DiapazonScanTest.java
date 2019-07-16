// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.exe.runnabletasks.ExecScan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static ru.vachok.networker.net.enums.ConstantsNet.*;


/**
 @see DiapazonScan */
@SuppressWarnings("ALL")
public class DiapazonScanTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private String testFilePathStr = Paths.get(".").toAbsolutePath().normalize().toString();
    
    private MessageToUser messageToUser = new MessageSwing();
    
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
    public void testTheInfoToString() {
        System.out.println(new DiapazonScan().theInfoToString());
    }
    
    @Test
    public void testToString1() {
        System.out.println("DiapazonScan.getInstance().toString() = " + DiapazonScan.getInstance().toString());
    }
    
    @Test
    public void isOldFilesExistsTest() {
        DiapazonScan dsIst = DiapazonScan.getInstance();
        File fileOrig = Paths.get(testFilePathStr).toFile();
        dsIst.checkAlreadyExistingFiles();
        checkIfCopied(dsIst);
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
        Map<String, File> scanFiles = DiapazonScan.getInstance().editScanFiles();
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
        scanMap.putIfAbsent(FILENAME_NEWLAN215, new File(FILENAME_NEWLAN215));
        scanMap.putIfAbsent(FILENAME_OLDLANTXT0, new File(FILENAME_OLDLANTXT0));
        scanMap.putIfAbsent(FILENAME_OLDLANTXT1, new File(FILENAME_OLDLANTXT1));
        scanMap.putIfAbsent(FILENAME_SERVTXT_10SRVTXT, new File(FILENAME_SERVTXT_10SRVTXT));
        scanMap.putIfAbsent(FILENAME_SERVTXT_21SRVTXT, new File(FILENAME_SERVTXT_21SRVTXT));
        scanMap.putIfAbsent(FILENAME_SERVTXT_31SRVTXT, new File(FILENAME_SERVTXT_31SRVTXT));
        return scanMap;
    }
    
    private void checkIfCopied(@NotNull DiapazonScan dsIst) { //fixme 13.07.2019 (6:13)
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
        catch (IndexOutOfBoundsException e) {
            messageToUser
                .infoTimer(10, MessageFormat.format("DiapazonScanTest.checkIfCopied says: {0}. Parameters: \n[dsIst]: {1}", e.getMessage(), dsIst.toString()));
        }
    }
}