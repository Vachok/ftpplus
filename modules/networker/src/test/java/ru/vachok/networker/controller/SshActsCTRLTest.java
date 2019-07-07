// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.accesscontrol.PfLists;
import ru.vachok.networker.accesscontrol.sshactions.SshActs;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import javax.servlet.http.HttpServletRequest;
import java.net.UnknownHostException;
import java.nio.file.AccessDeniedException;


/**
 @since 19.06.2019 (13:31) */
public class SshActsCTRLTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    
    @Test
    public void testSshActsPOST() {
        PfLists pfLists = new PfLists();
        SshActs sshActs = new SshActs();
        Model model = new ExtendedModelMap();
        HttpServletRequest request = new MockHttpServletRequest();
        
        SshActsCTRL sshActsCTRL = new SshActsCTRL(pfLists, sshActs);
        
        try {
            sshActsCTRL.sshActsPOST(sshActs, model, request);
            
        }
        catch (AccessDeniedException e) {
            Assert.assertNotNull(e, e.getMessage());
        }
        ((MockHttpServletRequest) request).setRemoteAddr("0:0:0:0");
        try {
            String actsPOSTString = sshActsCTRL.sshActsPOST(sshActs, model, request);
            Assert.assertEquals(actsPOSTString, "sshworks");
            
        }
        catch (AccessDeniedException e) {
            Assert.assertNull(e, e.getMessage());
        }
        Assert.assertTrue(model.asMap().size() >= 3);
        Assert.assertTrue(model.asMap().get("head").toString().contains("Главная"));
        Assert.assertEquals(sshActs, model.asMap().get("sshActs"));
    }
    
    @Test
    public void testSshActsGET() {
        PfLists pfLists = new PfLists();
        SshActs sshActs = new SshActs();
        Model model = new ExtendedModelMap();
        HttpServletRequest request = new MockHttpServletRequest();
        
        SshActsCTRL sshActsCTRL = new SshActsCTRL(pfLists, sshActs);
        try {
            sshActsCTRL.sshActsGET(model, request);
        }
        catch (AccessDeniedException e) {
            Assert.assertNotNull(e, e.getMessage());
        }
        ((MockHttpServletRequest) request).setRemoteAddr("0:0:0:0");
        try {
            String actsGET = sshActsCTRL.sshActsGET(model, request);
            Assert.assertEquals(actsGET, "sshworks");
        }
        catch (AccessDeniedException e) {
            Assert.assertNull(e, e.getMessage());
        }
        Assert.assertTrue(model.asMap().size() >= 4);
        Assert.assertEquals(model.asMap().get("sshActs"), sshActs);
    }
    
    @Test
    public void testAllowPOST() {
        PfLists pfLists = new PfLists();
        SshActs sshActs = new SshActs();
        Model model = new ExtendedModelMap();
        HttpServletRequest request = new MockHttpServletRequest();
        String allowDomain = "velkomfood.ru";
        sshActs.setAllowDomain(allowDomain);
        
        SshActsCTRL sshActsCTRL = new SshActsCTRL(pfLists, sshActs);
        String postString = sshActsCTRL.allowPOST(sshActs, model);
        Assert.assertEquals("ok", postString);
        Assert.assertTrue(model.asMap().size() >= 4);
        Assert.assertTrue(model.asMap().get("title").toString().contains(allowDomain));
    }
    
    @Test
    public void testDelDomPOST() {
        PfLists pfLists = new PfLists();
        SshActs sshActs = new SshActs();
        Model model = new ExtendedModelMap();
        HttpServletRequest request = new MockHttpServletRequest();
        String delDom = "velkomfood.ru";
        sshActs.setDelDomain(delDom);
        
        SshActsCTRL sshActsCTRL = new SshActsCTRL(pfLists, sshActs);
        String delDomPOSTString = sshActsCTRL.delDomPOST(sshActs, model);
        Assert.assertEquals("ok", delDomPOSTString);
        Assert.assertTrue(model.asMap().get("title").toString().contains(delDom));
        
    }
    
    @Test
    public void testTempFullInetAccess() {
        PfLists pfLists = new PfLists();
        SshActs sshActs = new SshActs();
        Model model = new ExtendedModelMap();
        HttpServletRequest request = new MockHttpServletRequest();
        
        SshActsCTRL sshActsCTRL = new SshActsCTRL(pfLists, sshActs);
        try {
            sshActs.setUserInput(ConstantsFor.HOSTNAME_DO213);
            String fullInetAccessString = sshActsCTRL.tempFullInetAccess(sshActs, model);
            Assert.assertEquals("ok", fullInetAccessString);
            Assert.assertTrue(model.asMap().size() >= 4);
        }
        catch (UnknownHostException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test
    public void testParseReq() {
        PfLists pfLists = new PfLists();
        SshActs sshActs = new SshActs();
        Model model = new ExtendedModelMap();
        HttpServletRequest request = new MockHttpServletRequest();
        
        SshActsCTRL sshActsCTRL = new SshActsCTRL(pfLists, sshActs);
        try {
            sshActsCTRL.parseReq(request.getQueryString());
        }
        catch (NullPointerException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testWhatSrvNeed() {
        SshActs sshActs = new SshActs();
        String neededSrvToConnect = sshActs.whatSrvNeed();
        Assert.assertTrue(neededSrvToConnect.contains("13.42"), neededSrvToConnect);
    }
}