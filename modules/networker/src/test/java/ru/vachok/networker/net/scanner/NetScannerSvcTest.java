// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


public class NetScannerSvcTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    private NetScannerSvc netScannerSvc = AppComponents.netScannerSvc();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testGetThePc() {
        String thePC = netScannerSvc.getThePc();
        Assert.assertTrue(thePC.contains("PC"));
    }
    
    @Test
    public void testSetThePc() {
    }
    
    @Test
    public void testTheSETOfPcNames() {
    }
    
    @Test
    public void testTheSETOfPCNamesPref() {
    }
    
    @Test
    public void testTestToString() {
    }
    
    @Test
    public void testCheckMapSizeAndDoAction() {
    }
}