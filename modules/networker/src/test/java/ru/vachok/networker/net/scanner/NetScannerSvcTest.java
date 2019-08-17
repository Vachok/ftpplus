// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
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
        throw new InvokeEmptyMethodException("17.08.2019 (16:50)");
    }
    
    @Test
    public void testTheSETOfPcNames() {
        throw new InvokeEmptyMethodException("17.08.2019 (16:50)");
    }
    
    @Test
    public void testTheSETOfPCNamesPref() {
        throw new InvokeEmptyMethodException("17.08.2019 (16:50)");
    }
    
    @Test
    public void testTestToString() {
        throw new InvokeEmptyMethodException("17.08.2019 (16:50)");
    }
    
    @Test
    public void testCheckMapSizeAndDoAction() {
        throw new InvokeEmptyMethodException("17.08.2019 (16:50)");
    }
}