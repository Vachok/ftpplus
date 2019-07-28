// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;


/**
 @see LastNetScan
 @since 16.07.2019 (21:06) */
public class LastNetScanTest {
    
    
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
    
    @Test
    public void testToString1() {
        Assert.assertTrue(LastNetScan.getLastNetScan().toString().contains("serialVersionUID=1984"));
    }
    
    @Test
    public void testGetLastNetScan() {
        LastNetScan scan = LastNetScan.getLastNetScan();
        Assert.assertNotNull(scan);
    }
    
    @Test
    public void testGetTimeLastScan() {
        LastNetScan scan = LastNetScan.getLastNetScan();
        Date lastScan = scan.getTimeLastScan();
        Assert.assertTrue(lastScan.getTime() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(1)));
    }
    
    @Test
    public void testSetTimeLastScan() {
        LastNetScan scan = LastNetScan.getLastNetScan();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            scan.setTimeLastScan(dateFormat.parse("07-01-1984"));
            Date lastScan = scan.getTimeLastScan();
            Assert.assertTrue(lastScan.getTime() < (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10000)));
        }
        catch (ParseException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetNetWork() {
        ConcurrentNavigableMap<String, Boolean> concurrentNavigableMap = LastNetScan.getLastNetScan().getNetWork();
    }
    
    @Test
    public void testSetNetWork() {
        ConcurrentSkipListMap<String, Boolean> networkMap = new ConcurrentSkipListMap<>();
        networkMap.put("test", true);
        NetKeeper.getNetwork().clear();
        NetKeeper.getNetwork().putAll(networkMap);
    
        Assert.assertTrue(NetKeeper.getNetwork().containsKey("test"));
    }
}