// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


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
    public void testTheSETOfPcNames() {
    
    }
    
    @Test
    public void testTheSETOfPCNamesPref() {
        throw new InvokeEmptyMethodException("17.08.2019 (16:50)");
    }
    
    @Test
    public void testTestToString() {
        String toStr = netScannerSvc.toString();
        Assert.assertTrue(toStr.contains("NetScannerSvc{"));
    }
    
    @Test
    public void testCheckMapSizeAndDoAction() {
        Model model = new ExtendedModelMap();
        HttpServletRequest request = new MockHttpServletRequest();
        try {
            netScannerSvc.checkMapSizeAndDoAction(model, request, 0);
        }
        catch (ExecutionException | TimeoutException | InterruptedException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = netScannerSvc.getInfoAbout("do0213");
        Assert.assertTrue(infoAbout.contains("ikudryashov"), infoAbout);
    }
    
    @Test
    public void testSetClassOption() {
        netScannerSvc.setClassOption("do0001");
        String svcInfo = netScannerSvc.getInfo();
        System.out.println("svcInfo = " + svcInfo);
        System.out.println("netScannerSvc = " + netScannerSvc.toString());
    }
    
    @Test
    public void testGetInfo() {
    }
    
    @Test
    public void testTestToString1() {
    }
}