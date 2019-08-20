// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.jetbrains.annotations.NotNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.accesscontrol.inetstats.InternetUse;
import ru.vachok.networker.ad.PhotoConverterSRV;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.PageGenerationHelper;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.concurrent.RejectedExecutionException;

import static org.testng.Assert.assertTrue;


/**
 @see ActDirectoryCTRL
 @since 13.06.2019 (16:46) */
public class ActDirectoryCTRLTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private Model model = new ExtendedModelMap();
    
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
        MockHttpServletRequest request = new MockHttpServletRequest();
        Model model = new ExtendedModelMap();
        String adUsersCompsStr = "null";
        try {
            adUsersCompsStr = actDirectoryCTRL.adUsersComps(request, model);
        }
        catch (RejectedExecutionException e) {
            messageToUser.error(MessageFormat
                    .format("ActDirectoryCTRLTest.testAdUsersComps {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        assertTrue(adUsersCompsStr.equals("ad"));
        assertTrue(model.asMap().size() == 4);
        assertTrue(model.asMap().get("pcs").toString().contains("<p>"));
        request.setQueryString("do0001");
        actDirectoryCTRL.adUsersComps(request, model);
        Assert.assertFalse(model.asMap().isEmpty());
        String usersMod = model.asMap().get("users").toString();
        Assert.assertTrue(usersMod.contains("estrelyaeva"), usersMod);
        Assert.assertTrue(model.asMap().get("title").toString().equalsIgnoreCase("do0001"));
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
        String attTitle = model.asMap().get(ModelAttributeNames.TITLE).toString();
        assertTrue(attTitle.contains("PowerShell"), attTitle);
    }
    
    @Test
    public void queryPC() {
        String queryString = "do0001";
        InetAddress address = new NameOrIPChecker(queryString).resolveIP();
        model.addAttribute(ModelAttributeNames.TITLE, queryString);
    
        checkPCOnline(address);
        
        model.addAttribute(ModelAttributeNames.ATT_HEAD, InternetUse.getUserStatistics("do0001"));
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.INET_USAGE);
        try {
            model.addAttribute("ATT_DETAILS", informationFactory.getInfoAbout(queryString));
        }
        catch (RejectedExecutionException e) {
            messageToUser.error(MessageFormat
                    .format("ActDirectoryCTRLTest.queryPC {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        Assert.assertFalse(model.asMap().isEmpty());
        Assert.assertTrue(model.asMap().size() == 4, model.asMap().size() + " is UNEXPECTED model size");
        Assert.assertTrue(model.asMap().get("title").toString().equals("do0001"));
        Assert.assertTrue(model.asMap().get("head").toString().contains("do0001 "));
        Assert.assertTrue(model.asMap().get("ATT_DETAILS").toString().contains("TCP_DENIED/403 CONNECT"));
        Assert.assertTrue(model.asMap().get("ATT_DETAILS").toString().contains("TCP_TUNNEL/200 CONNECT"));
        String users = model.asMap().get("users").toString();
        Assert.assertTrue(users.contains("strel"), users);
    }
    
    private void checkPCOnline(@NotNull InetAddress address) {
        InformationFactory informationFactory;
        if (NetScanService.isReach(address.getHostAddress())) {
            informationFactory = InformationFactory.getInstance(InformationFactory.LOCAL);
            model.addAttribute(ModelAttributeNames.USERS, informationFactory.getInfoAbout(address.getHostName()));
        }
        else {
            informationFactory = InformationFactory.getInstance(InformationFactory.SEARCH_PC_IN_DB);
            model.addAttribute(ModelAttributeNames.USERS, new PageGenerationHelper()
                    .setColor(ConstantsFor.COLOR_SILVER, informationFactory.getInfo() + " is offline"));
        }
    }
    
    @Test
    public void testTestToString() {
        String toString = new ActDirectoryCTRL(AppComponents.adSrv(), new PhotoConverterSRV()).toString();
        Assert.assertTrue(toString.contains("ActDirectoryCTRL{"), toString);
    }
}