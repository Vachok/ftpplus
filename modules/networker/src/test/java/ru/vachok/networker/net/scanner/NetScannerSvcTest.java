// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.net.NetKeeper;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageToTray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @see NetScannerSvc
 */
@SuppressWarnings("StaticVariableOfConcreteClass")
public class NetScannerSvcTest {
    
    
    private static final TForms T_FORMS = new TForms();
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    private static final Properties PROPERTIES = AppComponents.getProps();
    
    private static final long startClassTime = System.currentTimeMillis();
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, NetScannerSvcTest.class.getSimpleName());
    
    private NetScannerSvc netScannerSvc = AppComponents.netScannerSvc();
    
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
        NetScanCtr netScanCtr = new NetScanCtr(netScannerSvc);
        try {
            netScannerSvc.setClassOption(netScanCtr);
            Set<String> setPCs = netScannerSvc.fillSETOfPcNames();
            String svcInfo = new TForms().fromArray(setPCs);
            System.out.println("svcInfo = " + svcInfo);
        }
        catch (NullPointerException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            testOverController(netScanCtr);
        }
    }
    
    private void testOverController(@NotNull NetScanCtr ctr) {
        netScannerSvc.setThePc("do0213");
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
    public void testTheSETOfPCNamesPref() {
        NetScanService scanner = new NetScannerSvcTest.ScannerUSR(new Date());
        String resultStr = scanner.getPingResultStr();
        System.out.println("resultStr = " + resultStr);
    }
    
    @Test
    public void testTestToString() {
        String toStr = netScannerSvc.toString();
        Assert.assertTrue(toStr.contains("NetScannerSvc{"), toStr);
    }
    
    @Test
    public void testFillWebModel() {
        try {
            String filledModel = netScannerSvc.fillWebModel();
            System.out.println("filledModel = " + filledModel);
        }
        catch (InvokeIllegalException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Assert.assertEquals(testSetClassOption(), "netscan");
    
    }
    
    @Test
    public void testFillAttribute() {
        String infoAbout = netScannerSvc.fillAttribute("do0213");
        Assert.assertTrue(infoAbout.contains("ikudryashov"), infoAbout);
    }
    
    private String testSetClassOption() {
        NetScanCtr netScanCtr = new NetScanCtr(new NetScannerSvc());
        
        @NotNull String scanCtr = netScanCtr.netScan(request, response, model);
        System.out.println("scanCtr = " + scanCtr);
        netScannerSvc.setClassOption(netScanCtr);
        String toString = netScannerSvc.toString();
        
        Assert.assertFalse(toString.contains("NetScanCtr{"), toString);
        netScannerSvc.setClassOption("test");
        toString = netScannerSvc.toString();
        Assert.assertTrue(toString.contains("thePc='test'"), toString);
        return scanCtr;
    }
    
    @Test
    public void scannerTest() {
        NetScanService scanUsr = netScannerSvc.getScannerUSR;
        String toStr = scanUsr.toString();
        Assert.assertTrue(toStr.contains("Scanner{"), toStr);
        toStr = scanUsr.writeLog();
        System.out.println("toStr = " + toStr);
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
    
    private static class ScannerUSR implements NetScanService {
        
        
        private static NetScannerSvcTest.ScannerUSR scannerUSR = new NetScannerSvcTest.ScannerUSR();
        
        private Date lastScanDate;
        
        @Contract(pure = true)
        ScannerUSR(Date lastScanDate) {
            this.lastScanDate = lastScanDate;
        }
        
        @Contract(pure = true)
        private ScannerUSR() {
        }
        
        @Override
        public String getExecution() {
            throw new TODOException("18.08.2019 (22:17)");
        }
        
        @Override
        public String getPingResultStr() {
            ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
            String retStr;
            mxBean.setThreadContentionMonitoringEnabled(true);
            mxBean.resetPeakThreadCount();
            mxBean.setThreadCpuTimeEnabled(true);
            try {
                new MessageToTray(this.getClass().getSimpleName())
                        .info("NetScannerSvc started scan", UsefulUtilities.getUpTime(), MessageFormat.format("Last online {0} PCs\n File: {1}",
                                PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC), new File("scan.tmp").getAbsolutePath()));
            }
            catch (NoClassDefFoundError e) {
                messageToUser.error(getClass().getSimpleName(), "METH_GETPCSASYNC", T_FORMS.fromArray(e.getStackTrace(), false));
            }
            catch (InvokeIllegalException e) {
                messageToUser.error(MessageFormat
                        .format("Scanner.getPingResultStr {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
            }
            AppComponents.threadConfig().execByThreadConfig(this::scanPCPrefix);
            long[] deadlockedThreads = mxBean.findDeadlockedThreads();
            if (deadlockedThreads != null) {
                retStr = "You have a deadLock(s): " + Arrays.toString(deadlockedThreads);
                System.err.println(retStr);
            }
            else {
                long cpuTimeTotal = 0;
                for (long threadId : mxBean.getAllThreadIds()) {
                    cpuTimeTotal += mxBean.getThreadCpuTime(threadId);
                }
                cpuTimeTotal = TimeUnit.NANOSECONDS.toSeconds(cpuTimeTotal);
                retStr = MessageFormat
                        .format("Peak was {0} threads, now: {1}. Time: {2} millis.", mxBean.getPeakThreadCount(), mxBean.getThreadCount(), cpuTimeTotal);
                messageToUser.info("minimessageToUser.add(retStr);");
            }
            return retStr;
        }
        
        private void scanPCPrefix() {
            for (String s : ConstantsNet.getPcPrefixes()) {
                Thread.currentThread().setName(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + "-sec");
                NetKeeper.getPcNamesSet().clear();
                NetKeeper.getPcNamesSet().addAll(theSETOfPCNamesPref(s));
                AppComponents.threadConfig().thrNameSet("pcGET");
            }
            String elapsedTime = "Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + " sec.";
            NetKeeper.getPcNamesSet().add(elapsedTime);
        }
        
        Set<String> theSETOfPCNamesPref(String prefixPcName) {
            InformationFactory databaseInfo = InformationFactory.getInstance(InformationFactory.LOCAL);
            final long startMethTime = System.currentTimeMillis();
            String pcsString;
            for (String pcName : getCycleNames(prefixPcName)) {
                databaseInfo.getInfoAbout(pcName);
            }
            NetKeeper.getNetworkPCs().put("<h4>" + prefixPcName + "     " + NetKeeper.getPcNamesSet().size() + "</h4>", true);
            pcsString = "writeDB()";
            messageToUser.info(pcsString);
            String elapsedTime = "<b>Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethTime) + " sec.</b> " + LocalTime.now();
            NetKeeper.getPcNamesSet().add(elapsedTime);
            return NetKeeper.getPcNamesSet();
        }
    
        @Override
        public String writeLog() {
            throw new TODOException("18.08.2019 (22:17)");
        }
    
        @Override
        public Runnable getMonitoringRunnable() {
            throw new TODOException("18.08.2019 (22:17)");
        }
    
        @Override
        public String getStatistics() {
            throw new TODOException("18.08.2019 (22:17)");
        }
        
        @Override
        public void run() {
            scanIt();
        }
        
        @Async
        private void scanIt() {
            HttpServletRequest request = new MockHttpServletRequest();
            Model model = new ExtendedModelMap();
            if (request != null && request.getQueryString() != null) {
                NetKeeper.getNetworkPCs().clear();
                PROPERTIES.setProperty(PropertiesNames.PR_ONLINEPC, "0");
                AppComponents.getUserPref().putInt(PropertiesNames.PR_ONLINEPC, 0);
                Set<String> pcNames = theSETOfPCNamesPref(request.getQueryString());
                model.addAttribute(ModelAttributeNames.TITLE, new Date().toString())
                        .addAttribute("pc", T_FORMS.fromArray(pcNames, true));
            }
            else {
                NetKeeper.getNetworkPCs().clear();
                PROPERTIES.setProperty(PropertiesNames.PR_ONLINEPC, "0");
                AppComponents.getUserPref().putInt(PropertiesNames.PR_ONLINEPC, 0);
                
                Set<String> pCsAsync = theSETOfPcNames();
    
                model.addAttribute(ModelAttributeNames.TITLE, lastScanDate).addAttribute("pc", T_FORMS.fromArray(pCsAsync, true));
                PROPERTIES.setProperty(PropertiesNames.PR_LASTSCAN, String.valueOf(System.currentTimeMillis()));
            }
        }
        
        Set<String> theSETOfPcNames() {
            scanPCPrefix();
            return NetKeeper.getPcNamesSet();
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Scanner{");
            sb.append("lastScanDate=").append(lastScanDate);
            sb.append('}');
            return sb.toString();
        }
    }
}