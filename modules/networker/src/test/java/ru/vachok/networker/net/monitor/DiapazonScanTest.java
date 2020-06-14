// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.monitor;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @see DiapazonScan */
public class DiapazonScanTest {


    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());

    private String testFilePathStr = ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "tmp" + ConstantsFor.FILESYSTEM_SEPARATOR;

    private final MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }

    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }

    private static long getRunMin() {
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
        Assert.assertTrue(NetScanService.getInstance(NetScanService.DIAPAZON).toString().contains("DiapazonScan["));
    }

    /**
     @see DiapazonScan#run()
     */
    @Test
    public void testRun() {
        Assert.assertFalse(ConstantsFor.argNORUNExist(ConstantsFor.REGRUHOSTING_PC));
        Runnable diapazonScanRun = DiapazonScan.getInstance();
        try {
            AppConfigurationLocal.getInstance().execute(diapazonScanRun, 30);
        }
        catch (RejectedExecutionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        String instToString = diapazonScanRun.toString();

        Assert.assertTrue(instToString.contains("stopClassStampLong"), instToString);
    }

    @Test
    public void scanFilesTest() {
        List<File> scanFiles = NetKeeper.getCurrentScanFiles();
        String fromArray = new TForms().fromArray(scanFiles);
        Assert.assertTrue(fromArray.contains(FileNames.LAN_210215_TXT), fromArray);
        Assert.assertTrue(fromArray.contains(FileNames.LAN_OLD0_TXT), fromArray);
        Assert.assertTrue(fromArray.contains(FileNames.LAN_OLD1_TXT), fromArray);
        Assert.assertTrue(fromArray.contains(FileNames.LAN_205210_TXT), fromArray);
        Assert.assertTrue(fromArray.contains(FileNames.LAN_200205_TXT), fromArray);
        Assert.assertTrue(fromArray.contains("lan_21vsrv.txt"), fromArray);
        Assert.assertTrue(fromArray.contains("lan_11vsrv.txt"), fromArray);
        Assert.assertTrue(fromArray.contains("lan_31vsrv.txt"), fromArray);
        Assert.assertTrue(fromArray.contains(FileNames.LAN_213220_TXT), fromArray);
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

        scanMap.putIfAbsent(FileNames.LAN_213220_TXT, new File(FileNames.LAN_213220_TXT));
        scanMap.putIfAbsent(FileNames.LAN_200205_TXT, new File(FileNames.LAN_200205_TXT));
        scanMap.putIfAbsent(FileNames.LAN_210215_TXT, new File(FileNames.LAN_210215_TXT));
        scanMap.putIfAbsent(FileNames.LAN_OLD0_TXT, new File(FileNames.LAN_OLD0_TXT));
        scanMap.putIfAbsent(FileNames.LAN_OLD1_TXT, new File(FileNames.LAN_OLD1_TXT));
        scanMap.putIfAbsent(FileNames.LAN_11V_SERV_TXT, new File(FileNames.LAN_11V_SERV_TXT));
        scanMap.putIfAbsent(FileNames.LAN_21V_SERV_TXT, new File(FileNames.LAN_21V_SERV_TXT));
        scanMap.putIfAbsent(FileNames.LAN_31V_SERV_TXT, new File(FileNames.LAN_31V_SERV_TXT));
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