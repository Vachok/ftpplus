// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


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
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.NetPingerService;
import ru.vachok.networker.net.enums.ConstantsNet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static ru.vachok.networker.ConstantsFor.STR_P;


/**
 @see NetScanCtr */
@SuppressWarnings("ALL")
public class NetScanCtrTest {
    
    
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
    public void testNetScan() {
        NetScanCtr netScanCtr = null;
        try {
            netScanCtr = new NetScanCtr(AppComponents.netScannerSvc(), new NetPingerService(), new AppComponents().scanOnline());
        }
        catch (RejectedExecutionException e) {
            Assert.assertNotNull(e, e.getMessage());
        }
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Model model = new ExtendedModelMap();
        try {
            String netScanStr = netScanCtr.netScan(request, response, model);
            Assert.assertNotNull(netScanStr);
            assertTrue(netScanStr.equals(ConstantsNet.ATT_NETSCAN));
            assertTrue(model.asMap().size() >= 7, showModel(model.asMap()));
            assertTrue(model.asMap().get(ConstantsFor.ATT_FOOTER).toString().contains("Only Allow Domains"), showModel(model.asMap()));
        }
        catch (TaskRejectedException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testPingAddr() {
        Model model = new ExtendedModelMap();
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        try {
            String pingAddrString = new NetScanCtr(AppComponents.netScannerSvc(), new NetPingerService(), new AppComponents().scanOnline())
                .pingAddr(model, request, response);
            Assert.assertTrue(pingAddrString.contains("ping"));
            Assert.assertTrue(model.asMap().get("pingTest").toString().contains("ptv"), model.asMap().get("pingTest").toString());
        }
        catch (RejectedExecutionException e) {
            Assert.assertNotNull(e, e.getMessage());
        }
    }
    
    @Test
    public void testPingPost() {
        Model model = new ExtendedModelMap();
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        NetPingerService instPinger = new NetPingerService();
        String pingPostStr = new NetScanCtr(AppComponents.netScannerSvc(), instPinger, new AppComponents().scanOnline())
            .pingPost(model, request, instPinger, response);
        Assert.assertTrue(pingPostStr.equals("ok"));
        Assert.assertNotNull(model.asMap().get("netPinger"));
    }
    
    @Test
    public void testPcNameForInfo() {
        Model model = new ExtendedModelMap();
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        try {
            String pcNameInfoStr = NetScanCtr.pcNameForInfo(AppComponents.netScannerSvc(), model);
            Assert.assertTrue(pcNameInfoStr.contains("redirect:/ad"));
            
        }
        catch (RejectedExecutionException e) {
            Assert.assertNotNull(e, e.getMessage());
        }
    }
    
    /**
     @see NetScanCtr#allDevices(Model, HttpServletRequest, HttpServletResponse)
     */
    @Test
    public void testAllDevices() {
        Model model = new ExtendedModelMap();
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
    
        NetScanCtr netScanCtr = new NetScanCtr(AppComponents.netScannerSvc(), new NetPingerService(), new AppComponents().scanOnline());
        String allDevStr = netScanCtr.allDevices(model, request, response);
    
        Assert.assertTrue(allDevStr.equals("ok"), allDevStr);
        Assert.assertTrue(model.asMap().get("ok").toString().contains("DiapazonScan"));
    
        String pcsObject = model.asMap().get("pcs").toString(); //fixme 18.07.2019 (14:30) NullPointerException
    
        Assert.assertTrue(pcsObject.contains("Since"));
        
        ((MockHttpServletRequest) request).setQueryString("needsopen");
        allDevStr = netScanCtr.allDevices(model, request, response);
    
        Assert.assertEquals(allDevStr, "ok");
        Assert.assertTrue(model.asMap().size() >= 5);
        Assert.assertFalse(pcsObject.contains("Since"));
    }
    
    @Test
    public void testScanIt() {
        try {
            new NetScanCtr(AppComponents.netScannerSvc(), new NetPingerService(), new AppComponents().scanOnline()).scanIt();
        }
        catch (IllegalComponentStateException e) {
            assertNotNull(e, e.getMessage());
        }
    }
    
    private String showModel(Map<String, Object> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            stringBuilder.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        return stringBuilder.toString();
    }
    
    private String testFromArray(Map<String, String> mapDefObj) {
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