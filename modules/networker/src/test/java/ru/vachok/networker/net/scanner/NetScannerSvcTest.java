// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.mock.web.MockHttpServletRequest;
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
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToTray;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @see NetScannerSvc */
@SuppressWarnings("StaticVariableOfConcreteClass")
public class NetScannerSvcTest {
    
    
    private static final TForms T_FORMS = new TForms();
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    private static final Properties PROPERTIES = AppComponents.getProps();
    
    private static final long startClassTime = System.currentTimeMillis();
    
    private static final MessageToUser messageToUser = new MessageLocal(NetScannerSvcTest.class.getSimpleName());
    
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
        NetScanCtr netScanCtr = new NetScanCtr(netScannerSvc);
        netScannerSvc.setClassOption(netScanCtr);
        String svcInfo = netScannerSvc.fillWebModel();
        System.out.println("svcInfo = " + svcInfo);
    }
    
    @Test
    public void testTheSETOfPCNamesPref() {
        NetScanService scanner = new NetScannerSvcTest.Scanner(new Date());
        String resultStr = scanner.getPingResultStr();
        System.out.println("resultStr = " + resultStr);
    }
    
    @Test
    public void testTestToString() {
        String toStr = netScannerSvc.toString();
        Assert.assertFalse(toStr.contains("NetScannerSvc{"));
    }
    
    @Test
    public void testSetClassOption() {
        NetScanCtr scanCtr = new NetScanCtr(new NetScannerSvc());
        netScannerSvc.setClassOption(scanCtr);
        String toString = netScannerSvc.toString();
        Assert.assertTrue(toString.contains("NetScanCtr{"), toString);
        netScannerSvc.setClassOption("test");
        toString = netScannerSvc.toString();
        Assert.assertTrue(toString.contains("thePc='test'"), toString);
    }
    
    @Test
    public void testFillAttribute() {
        String infoAbout = netScannerSvc.fillAttribute("do0213");
        Assert.assertTrue(infoAbout.contains("ikudryashov"), infoAbout);
        System.out.println("infoAbout = " + infoAbout);
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
        NetScannerSvc localSVC = new NetScannerSvc();
        Assert.assertNotEquals(netScannerSvc, localSVC);
        NetScanCtr option = new NetScanCtr(localSVC);
    
        netScannerSvc.setClassOption(option);
        String modMap = netScannerSvc.fillWebModel();
        System.out.println("modMap = " + modMap);
    }
    
    private static String writeDB() throws SQLException {
        int exUpInt = 0;
        List<String> list = new ArrayList<>();
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_TESTING)) {
            try (PreparedStatement p = connection.prepareStatement("insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow) values (?,?,?,?)")) {
                List<String> toSort = new ArrayList<>(NetKeeper.getPcNamesSet());
                toSort.sort(null);
                for (String x : toSort) {
                    String pcSegment = "Я не знаю...";
                    if (x.contains("200.200")) {
                        pcSegment = "Торговый дом";
                    }
                    if (x.contains("200.201")) {
                        pcSegment = "IP телефоны";
                    }
                    if (x.contains("200.202")) {
                        pcSegment = "Техслужба";
                    }
                    if (x.contains("200.203")) {
                        pcSegment = "СКУД";
                    }
                    if (x.contains("200.204")) {
                        pcSegment = "Упаковка";
                    }
                    if (x.contains("200.205")) {
                        pcSegment = "МХВ";
                    }
                    if (x.contains("200.206")) {
                        pcSegment = "Здание склада 5";
                    }
                    if (x.contains("200.207")) {
                        pcSegment = "Сырокопоть";
                    }
                    if (x.contains("200.208")) {
                        pcSegment = "Участок убоя";
                    }
                    if (x.contains("200.209")) {
                        pcSegment = "Да ладно?";
                    }
                    if (x.contains("200.210")) {
                        pcSegment = "Мастера колб";
                    }
                    if (x.contains("200.212")) {
                        pcSegment = "Мастера деликатесов";
                    }
                    if (x.contains("200.213")) {
                        pcSegment = "2й этаж. АДМ.";
                    }
                    if (x.contains("200.214")) {
                        pcSegment = "WiFiCorp";
                    }
                    if (x.contains("200.215")) {
                        pcSegment = "WiFiFree";
                    }
                    if (x.contains("200.217")) {
                        pcSegment = "1й этаж АДМ";
                    }
                    if (x.contains("200.218")) {
                        pcSegment = "ОКК";
                    }
                    if (x.contains("192.168")) {
                        pcSegment = "Может быть в разных местах...";
                    }
                    if (x.contains("172.16.200")) {
                        pcSegment = "Open VPN авторизация - сертификат";
                    }
                    boolean onLine = false;
                    if (x.contains("true")) {
                        onLine = true;
                    }
                    String x1 = x.split(":")[0];
                    p.setString(1, x1);
                    String x2 = x.split(":")[1];
                    p.setString(2, x2.split("<")[0]);
                    p.setString(3, pcSegment);
                    p.setBoolean(4, onLine);
                    exUpInt += p.executeUpdate();
                    list.add(x1 + " " + x2 + " " + pcSegment + " " + onLine);
                }
            }
        }
        messageToUser.warn(NetScannerSvcTest.class.getSimpleName() + ".writeDB", "executeUpdate: ", " = " + exUpInt);
        return T_FORMS.fromArray(list, true);
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
    
    private static class Scanner implements NetScanService {
        
        
        private static NetScannerSvcTest.Scanner scanner = new NetScannerSvcTest.Scanner();
        
        private Date lastScanDate;
        
        @Contract(pure = true)
        Scanner(Date lastScanDate) {
            this.lastScanDate = lastScanDate;
        }
        
        @Contract(pure = true)
        private Scanner() {
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
            try {
                pcsString = writeDB();
                messageToUser.info(pcsString);
            }
            catch (SQLException e) {
                messageToUser.error(e.getMessage());
            }
            String elapsedTime = "<b>Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethTime) + " sec.</b> " + LocalTime.now();
            NetKeeper.getPcNamesSet().add(elapsedTime);
            return NetKeeper.getPcNamesSet();
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
                model.addAttribute(ModelAttributeNames.ATT_TITLE, new Date().toString())
                    .addAttribute("pc", T_FORMS.fromArray(pcNames, true));
            }
            else {
                NetKeeper.getNetworkPCs().clear();
                PROPERTIES.setProperty(PropertiesNames.PR_ONLINEPC, "0");
                AppComponents.getUserPref().putInt(PropertiesNames.PR_ONLINEPC, 0);
                
                Set<String> pCsAsync = theSETOfPcNames();
                
                model.addAttribute(ModelAttributeNames.ATT_TITLE, lastScanDate).addAttribute("pc", T_FORMS.fromArray(pCsAsync, true));
                PROPERTIES.setProperty(PropertiesNames.PR_LASTSCAN, String.valueOf(System.currentTimeMillis()));
            }
        }
        
        Set<String> theSETOfPcNames() {
            messageToUser.info("fileScanTMPCreate(true);");
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
        public String toString() {
            final StringBuilder sb = new StringBuilder("Scanner{");
            sb.append("lastScanDate=").append(lastScanDate);
            sb.append('}');
            return sb.toString();
        }
    }
}