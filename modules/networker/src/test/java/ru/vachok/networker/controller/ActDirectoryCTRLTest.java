// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.jetbrains.annotations.NotNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.ad.PhotoConverterSRV;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import static org.testng.Assert.assertTrue;


/**
 @see ActDirectoryCTRL
 @since 13.06.2019 (16:46) */
public class ActDirectoryCTRLTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private Model model = new ExtendedModelMap();
    
    private InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.INET_USAGE);
    
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
        this.model = new ExtendedModelMap();
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
    
        String pcsAtt = model.asMap().get("pcs").toString();
        assertTrue(pcsAtt.contains("Посмотреть сайты (BETA)"), pcsAtt);
        assertTrue(model.asMap().get(ModelAttributeNames.USERS).toString().contains("ActDirectoryCTRL"));
        assertTrue(model.asMap().get("photoConverter").toString().contains("PhotoConverterSRV["));
        assertTrue(model.asMap().get("footer").toString().contains("плохие-поросята"));
    
        try {
            request.setQueryString("do0001");
            actDirectoryCTRL.adUsersComps(request, model);
            checkAssertions(Collections.unmodifiableMap(model.asMap()));
        }
        catch (RejectedExecutionException e) {
            messageToUser.error(MessageFormat.format("ActDirectoryCTRLTest.testAdUsersComps: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    private void checkAssertions(@NotNull Map<String, Object> modelAsMap) {
        Assert.assertTrue(modelAsMap.size() == 7);
    
        String attTitle = modelAsMap.get(ModelAttributeNames.TITLE).toString();
        String usersMod = modelAsMap.get(ModelAttributeNames.USERS).toString();
        String headAtt = modelAsMap.get("head").toString();
        String detailsAtt = modelAsMap.get("details").toString();
    
        Assert.assertTrue(attTitle.equalsIgnoreCase("do0001"), attTitle);
        Assert.assertTrue(usersMod.contains("ActDirectoryCTRL"), usersMod);
        Assert.assertTrue(headAtt.contains("время открытых сессий"), headAtt);
        Assert.assertTrue(detailsAtt.contains("Посмотреть сайты (BETA)"), detailsAtt);
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
        model.addAttribute(ModelAttributeNames.ATT_HEAD, informationFactory.getInfoAbout(address.getHostAddress()));
        try {
            model.addAttribute(ModelAttributeNames.DETAILS, informationFactory.getInfo());
        }
        catch (RejectedExecutionException e) {
            messageToUser.error(MessageFormat
                    .format("ActDirectoryCTRLTest.queryPC {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
    
        Assert.assertFalse(model.asMap().isEmpty());
        Assert.assertTrue(model.asMap().size() == 3, model.asMap().size() + " is UNEXPECTED model size");
        Assert.assertTrue(model.asMap().get("title").toString().equals("do0001"));
        Assert.assertTrue(model.asMap().get("head").toString().contains("10.200.213.103 : "));
    
        Assert.assertTrue(model.asMap().get(ModelAttributeNames.DETAILS).toString().contains("TCP_DENIED/403 CONNECT"));
        Assert.assertTrue(model.asMap().get(ModelAttributeNames.DETAILS).toString().contains("TCP_TUNNEL/200 CONNECT"));
    }
    
    @Test
    public void testTestToString() {
        String toString = new ActDirectoryCTRL(AppComponents.adSrv(), new PhotoConverterSRV()).toString();
        Assert.assertTrue(toString.contains("ActDirectoryCTRL{"), toString);
    }
}