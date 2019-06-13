package ru.vachok.networker;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.accesscontrol.MatrixSRV;
import ru.vachok.networker.accesscontrol.PfLists;
import ru.vachok.networker.controller.MatrixCtr;
import ru.vachok.networker.controller.NetScanCtr;
import ru.vachok.networker.exe.runnabletasks.NetScannerSvc;
import ru.vachok.networker.exe.runnabletasks.PfListsCtr;
import ru.vachok.networker.exe.runnabletasks.PfListsSrv;
import ru.vachok.networker.exe.runnabletasks.ScanOnline;
import ru.vachok.networker.net.NetPinger;
import ru.vachok.networker.net.enums.ConstantsNet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


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
        matrixLogic(model, httpServletRequest, response);
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
        NetScanCtr netScanCtr = new NetScanCtr(netScannerSvc, new NetPinger(), (ScanOnline) new AppComponents().scanOnline());
        String netScanStr = netScanCtr.netScan(httpServletRequest, response, model);
        Assert.assertTrue(netScanStr.equals(ConstantsNet.ATT_NETSCAN));
        Assert.assertTrue(model.asMap().size() == 7, "Wrong model size netScanLogic: " + model.asMap().size());
    }
    
    private static void matrixLogic(Model model, HttpServletRequest httpServletRequest, HttpServletResponse response) {
        MatrixCtr matrixCtr = new MatrixCtr(AppComponents.versionInfo());
        MatrixSRV matrixSRV = new MatrixSRV();
        
        String matrixCtrFirst = matrixCtr.getFirst(httpServletRequest, model, response);
        
        Assert.assertTrue(matrixCtrFirst.equals("starting"), matrixCtrFirst + " is wrong!");
        Assert.assertTrue(model.asMap().size() == 11);
        
        matrixSRV.setWorkPos("адми");
//        matrixCtr.getWorkPosition(matrixSRV, model);
        try {
            matrixCtr.setMatrixSRV(matrixSRV);
            String showResultsStr = matrixCtr.showResults(httpServletRequest, response, model);
            Assert.assertTrue(showResultsStr.equals(ConstantsFor.BEANNAME_MATRIX));
            Assert.assertTrue(model.asMap().size() == 13);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
        try {
            matrixCtr.setCurrentProvider();
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage());
        }
        String gitOnStr = matrixCtr.gitOn(model, httpServletRequest);
        Assert.assertTrue(gitOnStr.equals("redirect:http://srv-git.eatmeat.ru:1234"));
    }
}
