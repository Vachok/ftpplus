// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.accesscontrol.PfLists;
import ru.vachok.networker.controller.NetScanCtr;
import ru.vachok.networker.exe.runnabletasks.NetScannerSvc;
import ru.vachok.networker.exe.runnabletasks.PfListsCtr;
import ru.vachok.networker.exe.runnabletasks.PfListsSrv;
import ru.vachok.networker.net.NetPinger;
import ru.vachok.networker.net.enums.ConstantsNet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 @since 13.06.2019 (14:10) */
public class TestOfAll {
    
    
    @Test(enabled = false)
    public void runLogic() {
        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Model model = new ExtendedModelMap();
        pfListsCTRLLogic(model, httpServletRequest, response);
        netScanLogic(model, response, httpServletRequest);
    }
    
    private static void pfListsCTRLLogic(Model model, HttpServletRequest httpServletRequest, HttpServletResponse response) {
        PfLists pfLists = new PfLists();
        PfListsSrv pfListsSrv = new PfListsSrv(pfLists);
        PfListsCtr pfListsCtr = new PfListsCtr(pfLists, pfListsSrv);
        
        try {
            String pfBean = pfListsCtr.pfBean(model, httpServletRequest, response);
            Assert.assertTrue(model.asMap().size() == 11, model.asMap().size() + " - wrong model size");
            Assert.assertTrue(pfBean.equals(ConstantsFor.BEANNAME_PFLISTS));
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage());
        }
        try {
            String commandResponse = pfListsCtr.runCommand(model, pfListsSrv);
            System.out.println(commandResponse);
            Assert.assertTrue(model.asMap().size() == 13, model.asMap().size() + " - wrong model size");
            Assert.assertTrue(commandResponse.equals("ok"));
            
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    private static void netScanLogic(Model model, HttpServletResponse response, HttpServletRequest httpServletRequest) {
        model.asMap().clear();
        NetScannerSvc netScannerSvc = AppComponents.netScannerSvc();
        System.out.println(netScannerSvc.toString());
        NetScanCtr netScanCtr = new NetScanCtr(netScannerSvc, new NetPinger(), new AppComponents().scanOnline());
        String netScanStr = netScanCtr.netScan(httpServletRequest, response, model);
        Assert.assertTrue(netScanStr.equals(ConstantsNet.ATT_NETSCAN));
        Assert.assertTrue(model.asMap().size() == 7, "Wrong model size netScanLogic: " + model.asMap().size());
    }
}
