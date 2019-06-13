package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.net.NetPinger;
import ru.vachok.networker.net.enums.ConstantsNet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertTrue;


public class NetScanCtrTest {
    
    
    @Test
    public void testNetScan() {
        NetScanCtr netScanCtr = new NetScanCtr(AppComponents.netScannerSvc(), new NetPinger(), new AppComponents().scanOnline());
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Model model = new ExtendedModelMap();
        
        String netScanStr = netScanCtr.netScan(request, response, model);
        assertTrue(netScanStr.equals(ConstantsNet.ATT_NETSCAN));
        assertTrue(model.asMap().size() == 7);
        assertTrue(model.asMap().get("pc").toString().contains("<p>"));
    }
    
    @Test
    public void testPingAddr() {
    }
    
    @Test
    public void testPingPost() {
    }
    
    @Test
    public void testPcNameForInfo() {
    }
    
    @Test
    public void testAllDevices() {
    }
    
    @Test
    public void testScanIt() {
    }
}