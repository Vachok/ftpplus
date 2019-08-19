// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ad.PhotoConverterSRV;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.info.DatabaseInfo;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.PCInformation;
import ru.vachok.networker.info.PageGenerationHelper;

import javax.servlet.http.HttpServletRequest;

import static org.testng.Assert.assertTrue;


/**
 @see ActDirectoryCTRL
 @since 13.06.2019 (16:46) */
public class ActDirectoryCTRLTest {
    
    
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
    public void testAdUsersComps() {
        ActDirectoryCTRL actDirectoryCTRL = new ActDirectoryCTRL(AppComponents.adSrv(), new PhotoConverterSRV());
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
        ActDirectoryCTRL actDirectoryCTRL = new ActDirectoryCTRL(AppComponents.adSrv(), photoConverterSRV);
        HttpServletRequest request = new MockHttpServletRequest();
        Model model = new ExtendedModelMap();
        
        String adFotoStr = actDirectoryCTRL.adFoto(photoConverterSRV, model, request);
        assertTrue(adFotoStr.equals(ActDirectoryCTRL.STR_ADPHOTO));
        int modelSize = model.asMap().size();
        assertTrue((modelSize == 5), modelSize + " model.asMap().size()");
        String attTitle = model.asMap().get(ModelAttributeNames.ATT_TITLE).toString();
        assertTrue(attTitle.contains("PowerShell"), attTitle);
    }
    
    @Test
    public void queryPC() {
        String queryString = "do0001";
        Model model = new ExtendedModelMap();
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.TYPE_PCINFO);
        
        ((PCInformation) informationFactory).setCurrentPCName(queryString);
        model.addAttribute(ModelAttributeNames.ATT_TITLE, queryString);
        try {
            model.addAttribute(ModelAttributeNames.ATT_USERS, informationFactory.getInfoAbout(queryString));
        }
        catch (RuntimeException e) {
            model.addAttribute(ModelAttributeNames.ATT_USERS, new PageGenerationHelper().setColor("blue", queryString + " is offline"));
        }
        informationFactory = InformationFactory.getInstance(queryString);
        model.addAttribute(ModelAttributeNames.ATT_HEAD, ((DatabaseInfo) informationFactory).getConnectStatistics());
        informationFactory = InformationFactory.getInstance(InformationFactory.TYPE_INETUSAGE);
        model.addAttribute("ATT_DETAILS", informationFactory.getInfoAbout(queryString));
        Assert.assertFalse(model.asMap().isEmpty());
    }
}