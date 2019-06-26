// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TestConfigure;
import ru.vachok.networker.ad.PhotoConverterSRV;

import javax.servlet.http.HttpServletRequest;

import static org.testng.Assert.assertTrue;


/**
 @since 13.06.2019 (16:46) */
public class ActDirectoryCTRLTest {
    
    
    private final TestConfigure testConfigure = new TestConfigure(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigure.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigure.afterClass();
    }
    
    
    @Test
    public void testAdUsersComps() {
        ActDirectoryCTRL actDirectoryCTRL = new ActDirectoryCTRL(AppComponents.adSrv(), new PhotoConverterSRV(), new AppComponents().sshActs());
        HttpServletRequest request = new MockHttpServletRequest();
        Model model = new ExtendedModelMap();
        
        String adUsersCompsStr = actDirectoryCTRL.adUsersComps(request, model);
        assertTrue(adUsersCompsStr.equals("ad"));
        assertTrue(model.asMap().size() == 4);
        assertTrue(model.asMap().get("pcs").toString().contains("<p>"));
    }
    
    @Test
    public void testAdFoto() {
        PhotoConverterSRV photoConverterSRV = new PhotoConverterSRV();
        ActDirectoryCTRL actDirectoryCTRL = new ActDirectoryCTRL(AppComponents.adSrv(), photoConverterSRV, new AppComponents().sshActs());
        HttpServletRequest request = new MockHttpServletRequest();
        Model model = new ExtendedModelMap();
        
        String adFotoStr = actDirectoryCTRL.adFoto(photoConverterSRV, model, request);
        assertTrue(adFotoStr.equals("adphoto"));
        int modelSize = model.asMap().size();
        assertTrue((modelSize == 5), modelSize + " model.asMap().size()");
        String attTitle = model.asMap().get(ConstantsFor.ATT_TITLE).toString();
        assertTrue(attTitle.contains("PowerShell"), attTitle);
    }
}