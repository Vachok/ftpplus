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
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.net.LongNetScanServiceFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;

import static org.testng.Assert.assertTrue;
import static ru.vachok.networker.ConstantsFor.STR_P;


/**
 @see NetScanCtr */
@SuppressWarnings("ALL")
public class NetScanCtrTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private final NetScannerSvc netScannerSvc = (NetScannerSvc) AppComponents.netScannerSvc();
    
    private HttpServletRequest request = new MockHttpServletRequest();
    
    private HttpServletResponse response = new MockHttpServletResponse();
    
    private Model model = new ExtendedModelMap();
    
    private NetScanCtr netScanCtr = new NetScanCtr(AppComponents.netScannerSvc(), new LongNetScanServiceFactory());
    
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
        NetScanCtr netScanCtr = null;
        try {
            netScanCtr = new NetScanCtr(netScannerSvc, new LongNetScanServiceFactory());
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
            assertTrue(netScanStr.equals(ConstantsNet.ATT_NETSCAN));
            assertTrue(model.asMap().size() >= 7, showModel(model.asMap()));
            assertTrue(model.asMap().get(ModelAttributeNames.ATT_FOOTER).toString().contains("Only Allow Domains"), showModel(model.asMap()));
        }
        catch (TaskRejectedException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testPingAddr() {
        try {
            String pingAddrString = new NetScanCtr(netScannerSvc, new LongNetScanServiceFactory()).pingAddr(model, request, response);
            String pingTest = model.asMap().get("pingTest").toString();
            Assert.assertNotNull(pingTest);
            Assert.assertTrue(pingAddrString.equals("ping"));
        }
        catch (RejectedExecutionException e) {
            Assert.assertNotNull(e, e.getMessage());
        }
        catch (NullPointerException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testPingPost() {
        Model model = this.model;
        HttpServletRequest request = this.request;
        HttpServletResponse response = this.response;
        LongNetScanServiceFactory instPinger = new LongNetScanServiceFactory();
        String pingPostStr = new NetScanCtr(netScannerSvc, instPinger)
            .pingPost(model, request, instPinger, response);
        Assert.assertTrue(pingPostStr.equals("ok"));
        Assert.assertNotNull(model.asMap().get("netPinger"));
    }
    
    @Test
    public void testPcNameForInfo() {
        Model model = this.model;
        HttpServletRequest request = this.request;
        HttpServletResponse response = this.response;
        try {
            String pcNameInfoStr = netScanCtr.pcNameForInfo(netScannerSvc, model);
            Assert.assertTrue(pcNameInfoStr.contains("redirect:/ad"));
            
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