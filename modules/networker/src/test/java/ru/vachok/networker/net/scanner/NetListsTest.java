// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 @see NetLists
 @since 12.07.2019 (16:27) */
@SuppressWarnings("DuplicateStringLiteralInspection")
public class NetListsTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testToString1() {
        Assert.assertTrue(NetLists.getI().toString().contains("offLines="));
    }
    
    @Test
    public void testGetOffLines() {
        NetLists listKeeper = NetLists.getI();
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
        NetLists listKeeper = NetLists.getI();
        listKeeper.setOffLines(offLines);
        boolean containsKey = listKeeper.getOffLines().containsKey("test");
        Assert.assertTrue(containsKey);
    }
    
    @Test
    public void testEditOffLines() {
        NetLists listKeeper = NetLists.getI();
        listKeeper.setOffLines(new ConcurrentHashMap<>());
        listKeeper.editOffLines().put("test", "test");
        boolean containsKey = listKeeper.getOffLines().containsKey("test");
        Assert.assertTrue(containsKey);
    }
    
    @Test
    public void testGetI() {
        NetLists netLists = NetLists.getI();
        Assert.assertTrue(netLists != null);
    }
    
    @Test
    public void testGetMapAddr() {
        Map<InetAddress, String> mapAddr = NetLists.getMapAddr();
        Assert.assertTrue(new TForms().fromArray(mapAddr).contains("10.200.213.85 : DO0213_KUDR"));
    }
    
    @Test
    public void testGetOnLinesResolve() {
        NetLists netLists = NetLists.getI();
        ConcurrentMap<String, String> onLinesResolve = netLists.getOnLinesResolve();
        onLinesResolve.put("test", "test");
        Assert.assertTrue(new TForms().fromArray(onLinesResolve).contains("test"));
    }
    
    private boolean checkFileContent(@NotNull File results) {
        String readFile = FileSystemWorker.readFile(results.getAbsolutePath());
        return readFile.contains("10.200.200.1");
    }
}