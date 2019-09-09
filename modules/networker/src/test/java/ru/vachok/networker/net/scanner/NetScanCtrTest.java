// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.inet.InternetUse;
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
import java.util.concurrent.RejectedExecutionException;

import static org.testng.Assert.assertTrue;
import static ru.vachok.networker.data.enums.ConstantsFor.STR_P;


/**
 @see NetScanCtr */
@SuppressWarnings("ALL")
public class NetScanCtrTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private final PcNamesScanner pcNamesScanner = new PcNamesScanner();
    
    private HttpServletRequest request = new MockHttpServletRequest();
    
    private HttpServletResponse response = new MockHttpServletResponse();
    
    private Model model = new ExtendedModelMap();
    
    private NetScanCtr netScanCtr = new NetScanCtr(pcNamesScanner);
    
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
    public void testNetScan() {
        try {
            Files.deleteIfExists(new File(FileNames.SCAN_TMP).toPath());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        NetScanCtr netScanCtr = null;
        try {
            netScanCtr = new NetScanCtr(pcNamesScanner);
        }
        catch (RejectedExecutionException e) {
            Assert.assertNotNull(e, e.getMessage());
        }
        HttpServletRequest request = this.request;
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
    
    @NotNull
    private String showModel(@NotNull Map<String, Object> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            stringBuilder.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        return stringBuilder.toString();
    }
    
    @Test(invocationCount = 3)
    public void testAbstractGetInetUsageByPc() {
        String thePC = "do0056";
        String info = getInformation(thePC);
        Assert.assertTrue(info.contains("do0056 : "), info);
        Assert.assertTrue(info.contains("время открытых сессий"), info);
    }
    
    @NotNull
    private String getInformation(String instanceType) {
        InternetUse informationFactory = InternetUse.getInstance(instanceType);
        informationFactory.setClassOption(instanceType);
        String infoAboutInet = informationFactory.getInfoAbout(instanceType);
        Assert.assertTrue(infoAboutInet.contains("время открытых сессий"), infoAboutInet);
        String detailedInfo = informationFactory.getInfo();
        return infoAboutInet + "\n" + detailedInfo;
    }
    
    @Test
    public void testAbstractGetInetUsageByUser() {
        String thePc = "strel";
        String info = getInformation(thePc);
        System.out.println("info = " + info);
    }
    
    @NotNull
    private String testFromArray(@NotNull Map<String, String> mapDefObj) {
        StringBuilder brStringBuilder = new StringBuilder();
        brStringBuilder.append(STR_P);
        Set<?> keySet = mapDefObj.keySet();
        List<String> list = new ArrayList<>(keySet.size());
        keySet.forEach(x->list.add(x.toString()));
        Collections.sort(list);
        for (String keyMap : list) {
            String valueMap = mapDefObj.get(keyMap).toString();
            brStringBuilder.append(keyMap).append(" ").append(valueMap).append("<br>");
        }
        return brStringBuilder.toString();
        
    }
}