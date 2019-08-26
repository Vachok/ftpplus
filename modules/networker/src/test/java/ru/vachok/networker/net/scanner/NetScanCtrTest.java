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
import ru.vachok.networker.componentsrepo.data.enums.ModelAttributeNames;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;

import static org.testng.Assert.assertTrue;
import static ru.vachok.networker.componentsrepo.data.enums.ConstantsFor.STR_P;


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