// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.*;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 @see NetListKeeper
 @since 12.07.2019 (16:27) */
@SuppressWarnings("DuplicateStringLiteralInspection")
public class NetListKeeperTest {
    
    
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
    public void testToString1() {
        Assert.assertTrue(NetListKeeper.getI().toString().contains("offLines="));
    }
    
    @Test
    public void testCheckSwitchesAvail() {
        NetListKeeper.getI().checkSwitchesAvail();
        File fileResults = new File("sw.list.log");
        Assert.assertTrue(fileResults.exists());
        Assert.assertTrue(checkFileContent(fileResults));
    }
    
    @Test
    public void testGetOffLines() {
        NetListKeeper listKeeper = NetListKeeper.getI();
        Map<String, String> offLines = listKeeper.getOffLines();
        try {
            offLines.put("test", "test");
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e);
        }
        
    }
    
    @Test
    public void testSetOffLines() {
        Map<String, String> offLines = new ConcurrentHashMap<>();
        offLines.put("test", String.valueOf(System.currentTimeMillis()));
        NetListKeeper listKeeper = NetListKeeper.getI();
        listKeeper.setOffLines(offLines);
        boolean containsKey = listKeeper.getOffLines().containsKey("test");
        Assert.assertTrue(containsKey);
    }
    
    @Test
    public void testEditOffLines() {
        NetListKeeper listKeeper = NetListKeeper.getI();
        listKeeper.setOffLines(new ConcurrentHashMap<>());
        listKeeper.editOffLines().put("test", "test");
        boolean containsKey = listKeeper.getOffLines().containsKey("test");
        Assert.assertTrue(containsKey);
    }
    
    @Test
    public void testGetInetUniqMap() {
        NetListKeeper listKeeper = NetListKeeper.getI();
        Map<String, String> uniqMap = listKeeper.getInetUniqMap();
        Assert.assertTrue(new TForms().fromArray(uniqMap).contains("NOT UNIQUE"));
    }
    
    @Test
    public void testSetInetUniqMap() {
        NetListKeeper listKeeper = NetListKeeper.getI();
        ConcurrentHashMap<String, String> inetUniqMap = new ConcurrentHashMap<>();
        inetUniqMap.put("AccessListsCheckUniq", "parseListFiles");
        inetUniqMap.put("TemporaryFullInternet", "doAdd");
        listKeeper.setInetUniqMap(inetUniqMap);
        Assert.assertEquals(listKeeper.getInetUniqMap().get("AccessListsCheckUniq"), "parseListFiles");
        Assert.assertEquals(listKeeper.getInetUniqMap().get("TemporaryFullInternet"), "doAdd");
    }
    
    @Test
    public void testGetI() {
        NetListKeeper netListKeeper = NetListKeeper.getI();
        Assert.assertTrue(netListKeeper != null);
    }
    
    @Test
    public void testGetMapAddr() {
        Map<InetAddress, String> mapAddr = NetListKeeper.getMapAddr();
        Assert.assertTrue(new TForms().fromArray(mapAddr).contains("10.200.213.85 : DO0213_KUDR"));
    }
    
    @Test
    public void testGetOnLinesResolve() {
        NetListKeeper netListKeeper = NetListKeeper.getI();
        ConcurrentMap<String, String> onLinesResolve = netListKeeper.getOnLinesResolve();
        onLinesResolve.put("test", "test");
        Assert.assertTrue(new TForms().fromArray(onLinesResolve).contains("test"));
    }
    
    private boolean checkFileContent(File results) {
        String readFile = FileSystemWorker.readFile(results.getAbsolutePath());
        return readFile.contains("10.200.200.1");
    }
    
    @Test
    public void privateReadMapTesting() {
        try (InputStream inputStream = new FileInputStream(getClass().getSimpleName() + ConstantsFor.FILENALE_ONLINERES);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
        ) {
            Map<String, String> fromFileMap = (Map<String, String>) objectInputStream.readObject();
            NetListKeeper.getI().getOnLinesResolve().putAll(fromFileMap);
        }
        catch (IOException | ClassNotFoundException e) {
            Assert.assertNull(e); //fixme 16.07.2019 (20:57)
        }
    }
}