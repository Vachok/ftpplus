// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.MessageToUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


/**
 @see PcNamesScanner
 */
@SuppressWarnings("StaticVariableOfConcreteClass")
public class PcNamesScannerTest {
    
    
    private static final TForms T_FORMS = new TForms();
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    private static final Properties PROPERTIES = AppComponents.getProps();
    
    private static final long startClassTime = System.currentTimeMillis();
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PcNamesScannerTest.class.getSimpleName());
    
    private PcNamesScanner pcNamesScanner = AppComponents.netScannerSvc();
    
    private HttpServletRequest request = new MockHttpServletRequest();
    
    private HttpServletResponse response = new MockHttpServletResponse();
    
    private Model model = new ExtendedModelMap();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        File scanTmp = new File("scan.tmp");
        if (scanTmp.exists()) {
            messageToUser.warn(MessageFormat.format("File scan.tmp is {0} .", scanTmp.delete()));
        }
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testTheSETOfPcNames() {
        NetScanCtr netScanCtr = new NetScanCtr(pcNamesScanner);
        try {
            pcNamesScanner.setClassOption(netScanCtr);
            Set<String> setPCs = NetKeeper.getPcNamesSet();
            try {
                setPCs = pcNamesScanner.fillSETOfPcNames();
            }
            catch (ExecutionException | InterruptedException | TimeoutException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
            
            String svcInfo = new TForms().fromArray(setPCs);
            System.out.println("svcInfo = " + svcInfo);
        }
        catch (NullPointerException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            testOverController(netScanCtr);
        }
    }
    
    private void testOverController(@NotNull NetScanCtr ctr) {
        pcNamesScanner.setThePc("do0213");
        String netScanResult = ctr.netScan(request, response, model);
        
        Assert.assertEquals(netScanResult, "netscan");
        Assert.assertTrue(model.asMap().size() >= 7, model.asMap().size() + " UNEXPECTED model size!");
        Assert.assertTrue(model.asMap().size() <= 8, model.asMap().size() + " UNEXPECTED model size!");
        Float lastScanInMin = Float.parseFloat(model.asMap().get(ModelAttributeNames.SERVICEINFO).toString());
        Assert.assertFalse(lastScanInMin.isNaN(), lastScanInMin + " UNEXPECTED model serviceinfo! lastScanInMin is not number!");
        Assert.assertFalse(model.asMap().get(ModelAttributeNames.PC).toString().isEmpty());
        
        String attTitle = model.asMap().get(ModelAttributeNames.TITLE).toString();
        String beanNetScannerSvc = model.asMap().get(ConstantsFor.BEANNAME_NETSCANNERSVC).toString();
        String attThePc = model.asMap().get(ModelAttributeNames.THEPC).toString();
        String attFooter = model.asMap().get(ModelAttributeNames.FOOTER).toString();
        String attNewPC = model.asMap().get(ModelAttributeNames.PC).toString();
        
        Assert.assertTrue(attTitle.contains("MSK"), attTitle);
        Assert.assertTrue(beanNetScannerSvc.contains("NetScannerSvc{"), beanNetScannerSvc);
        Assert.assertTrue(attFooter.contains("icons8-плохие-поросята-100g.png"), attFooter);
        Assert.assertTrue(attNewPC.contains("<p>"), attNewPC);
        Assert.assertEquals(attThePc, "do0213");
    }
    
    @Test
    public void testTestToString() {
        String toStr = pcNamesScanner.toString();
        Assert.assertTrue(toStr.contains("NetScannerSvc{"), toStr);
    }
    
    @Test
    public void testFillWebModel() {
        try {
            String filledModel = pcNamesScanner.fillWebModel();
            System.out.println("filledModel = " + filledModel);
        }
        catch (InvokeIllegalException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertEquals(testSetClassOption(), "netscan");
    
    }
    
    private String testSetClassOption() {
        NetScanCtr netScanCtr = new NetScanCtr(new PcNamesScanner());
        
        @NotNull String scanCtr = netScanCtr.netScan(request, response, model);
    
        pcNamesScanner.setClassOption(netScanCtr);
        String toString = pcNamesScanner.toString();
        
        Assert.assertFalse(toString.contains("NetScanCtr{"), toString);
        pcNamesScanner.setClassOption("test");
        toString = pcNamesScanner.toString();
        Assert.assertTrue(toString.contains("thePc='test'"), toString);
        return scanCtr;
    }
    
    @Test
    public void testFillAttribute() {
        String infoAbout = pcNamesScanner.fillAttribute("do0213");
        Assert.assertTrue(infoAbout.contains("ikudryashov"), infoAbout);
    }
    
    @Test
    public void scannerTest() {
        NetScanService scanUsr = pcNamesScanner.getScannerUSR;
        String toStr = scanUsr.toString();
        Assert.assertTrue(toStr.contains("Scanner{"), toStr);
        toStr = scanUsr.writeLog();
        Assert.assertTrue(toStr.contains("pcName = 'savelogs'"), toStr);
        Runnable monitoringRunnable = scanUsr.getMonitoringRunnable();
        Assert.assertEquals(monitoringRunnable, scanUsr);
    
        String usrStatistics = scanUsr.getStatistics();
        System.out.println("usrStatistics = " + usrStatistics);
    }
    
    private static @NotNull Collection<String> getCycleNames(String namePCPrefix) {
        if (namePCPrefix == null) {
            namePCPrefix = "pp";
        }
        int inDex = getNamesCount(namePCPrefix);
        String nameCount;
        Collection<String> list = new ArrayList<>();
        int pcNum = 0;
        for (int i = 1; i < inDex; i++) {
            if (namePCPrefix.equals("no") || namePCPrefix.equals("pp") || namePCPrefix.equals("do") || namePCPrefix.equals("notd") || namePCPrefix.equals("dotd")) {
                nameCount = String.format("%04d", ++pcNum);
            }
            else {
                nameCount = String.format("%03d", ++pcNum);
            }
            list.add(namePCPrefix + nameCount + ConstantsFor.DOMAIN_EATMEATRU);
        }
        return list;
    }
    
    private static int getNamesCount(@NotNull String qer) {
        int inDex = 0;
        if (qer.equals("no")) {
            inDex = ConstantsNet.NOPC;
        }
        if (qer.equals("pp")) {
            inDex = ConstantsNet.PPPC;
        }
        if (qer.equals("do")) {
            inDex = ConstantsNet.DOPC;
        }
        if (qer.equals("a")) {
            inDex = ConstantsNet.APC;
        }
        if (qer.equals("td")) {
            inDex = ConstantsNet.TDPC;
        }
        if (qer.equals("dotd")) {
            inDex = ConstantsNet.DOTDPC;
        }
        if (qer.equals("notd")) {
            inDex = ConstantsNet.NOTDPC;
        }
        return inDex;
    }
}