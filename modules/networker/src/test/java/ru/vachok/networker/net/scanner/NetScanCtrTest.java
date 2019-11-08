// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.inet.InternetUse;
import ru.vachok.networker.componentsrepo.FakeRequest;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.ModelAttributeNames;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

import static org.testng.Assert.assertTrue;
import static ru.vachok.networker.data.enums.ConstantsFor.STR_P;


/**
 @see NetScanCtr */
public class NetScanCtrTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private PcNamesScanner pcNamesScanner;
    
    private NetScanCtr netScanCtr = new NetScanCtr();
    
    private HttpServletRequest request = new MockHttpServletRequest();
    
    private HttpServletResponse response = new MockHttpServletResponse();
    
    private Model model = new ExtendedModelMap();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @BeforeMethod
    public void initScan() {
        netScanCtr.setModel(model);
        netScanCtr.setRequest(request);
        this.pcNamesScanner = new PcNamesScanner(netScanCtr);
    }
    
    @Test
    public void testNetScan() {
        try {
            Files.deleteIfExists(new File(FileNames.SCAN_TMP).toPath());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        NetScanCtr netScanCtr = null;
        try {
            netScanCtr = new NetScanCtr();
        }
        catch (RejectedExecutionException e) {
            Assert.assertNotNull(e, e.getMessage());
        }
        HttpServletRequest request = new FakeRequest();
        HttpServletResponse response = this.response;
        Model model = this.model;
        try {
            String netScanStr = netScanCtr.netScan(request, response, model);
            Assert.assertNotNull(netScanStr);
            assertTrue(netScanStr.equals(ModelAttributeNames.NETSCAN));
            assertTrue(model.asMap().size() >= 5, showModel(model.asMap()));
            assertTrue(model.asMap().get(ModelAttributeNames.FOOTER).toString().contains("Only Allow Domains"), showModel(model.asMap()));
        }
        catch (TaskRejectedException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testStarterNetScan() {
        netScanCtr.starterNetScan(pcNamesScanner);
        File file = new File(FileNames.SCAN_TMP);
        Assert.assertTrue(file.exists());
        Assert.assertTrue(file.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(30)));
    }
    
    @Test
    public void testAbstractGetInetUsageByUser() {
        String thePc = "strel";
        Future<String> infoF = AppComponents.threadConfig().getTaskExecutor().submit(()->getInformation(thePc));
        try {
            String info = infoF.get(30, TimeUnit.SECONDS);
            System.out.println("info = " + info);
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testPcNameForInfo() {
        Model model = this.model;
        HttpServletRequest request = this.request;
        HttpServletResponse response = this.response;
        pcNamesScanner.setThePc("do0001");
        try {
            String pcNameInfoStr = netScanCtr.pcNameForInfo(pcNamesScanner, model);
            Assert.assertTrue(pcNameInfoStr.contains("redirect:/ad"));
            Assert.assertTrue(model.asMap().get(ModelAttributeNames.THEPC).equals("do0001"));
            
        }
        catch (RejectedExecutionException e) {
            Assert.assertNotNull(e, e.getMessage());
        }
    }
    
    @Test
    public void testAbstractGetInetUsageByPc() {
        String thePC = "do0056";
        String info = getInformation(thePC);
        Assert.assertTrue(info.contains("do0056 : "), info);
        Assert.assertTrue(info.contains("время открытых сессий"), info);
    }
    
    private @NotNull String getInformation(String instanceType) {
        InternetUse informationFactory = InternetUse.getInstance("instanceType");
        informationFactory.setClassOption(instanceType);
        String infoAboutInet = informationFactory.getInfoAbout(instanceType);
        Assert.assertTrue(infoAboutInet.contains("время открытых сессий"), infoAboutInet);
        String detailedInfo = informationFactory.getInfo();
        return infoAboutInet + "\n" + detailedInfo;
    }
    
    private @NotNull String showModel(@NotNull Map<String, Object> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            stringBuilder.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        return stringBuilder.toString();
    }
    
    private @NotNull String testFromArray(@NotNull Map<String, String> mapDefObj) {
        StringBuilder brStringBuilder = new StringBuilder();
        brStringBuilder.append(STR_P);
        Set<?> keySet = mapDefObj.keySet();
        List<String> list = new ArrayList<>(keySet.size());
        keySet.forEach(x->list.add(x.toString()));
        Collections.sort(list);
        for (String keyMap : list) {
            String valueMap = mapDefObj.get(keyMap);
            brStringBuilder.append(keyMap).append(" ").append(valueMap).append("<br>");
        }
        return brStringBuilder.toString();
        
    }
}