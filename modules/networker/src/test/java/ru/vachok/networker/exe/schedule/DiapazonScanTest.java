// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.exe.runnabletasks.ExecScan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static ru.vachok.networker.net.enums.ConstantsNet.*;


@SuppressWarnings("ALL") public class DiapazonScanTest {
    
    
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
            Assert.assertTrue(pingSWList.size() == 43, pingSWList.size() + " devices in " + pingSWList.getClass().getSimpleName());
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
        scanMap.putIfAbsent(FILENAME_NEWLAN210, new File(FILENAME_NEWLAN210));
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
}