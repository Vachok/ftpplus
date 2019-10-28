package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.mail.ExSRV;
import ru.vachok.networker.mail.RuleSet;

import javax.servlet.http.HttpServletRequest;


/**
 @see ExCTRL
 @since 09.08.2019 (12:46) */
public class ExCTRLTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(ExCTRL.class.getSimpleName(), System
        .nanoTime());
    
    private ExCTRL exCTRL = new ExCTRL(new ExSRV(), new RuleSet());
    
    private Model extendedModelMap = new ExtendedModelMap();
    
    private HttpServletRequest request = new MockHttpServletRequest();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testExchangeWorks() {
        String exchangeWorks = exCTRL.exchangeWorks(extendedModelMap, request);
        Assert.assertEquals(exchangeWorks, "exchange");
        Assert.assertTrue(extendedModelMap.asMap().size() == 4);
        Assert.assertTrue(extendedModelMap.asMap().get(ModelAttributeNames.ATT_EXSRV).toString().contains("ExSRV{"));
        Assert.assertTrue(extendedModelMap.asMap().get(ModelAttributeNames.AT_NAME_RULESET).toString().contains("RuleSet{"));
        Assert.assertTrue(extendedModelMap.asMap().get("file").toString().contains("<b>Exchange</b>"));
        Assert.assertTrue(extendedModelMap.asMap().get(ModelAttributeNames.FOOTER).toString().contains("a href"));
    }
    
    @Test
    public void testUplFile() {
        MultipartFile multipartFile = new MockMultipartFile("test", "test".getBytes());
        String uplFileStr = exCTRL.uplFile(multipartFile, extendedModelMap);
        Assert.assertEquals(uplFileStr, "exchange");
    }
    
    @Test
    public void testRuleSetPost() {
        try {
            String ruleSetPost = exCTRL.ruleSetPost(new RuleSet(), extendedModelMap);
            System.out.println("ruleSetPost = " + ruleSetPost);
        }
        catch (NullPointerException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testRuleSetGet() {
        String rulesetGet = exCTRL.ruleSetGet(extendedModelMap, new MockHttpServletResponse());
        Assert.assertEquals(rulesetGet, "redirect:/ok?FromAddressMatchesPatterns");
        Assert.assertTrue(extendedModelMap.asMap().get(ModelAttributeNames.AT_NAME_RULESET).toString().contains("RuleSet{"));
        Assert.assertNull(extendedModelMap.asMap().get("ok"));
    }
    
    @Test
    public void testTestToString() {
        String toStr = exCTRL.toString();
        Assert.assertTrue(toStr.contains("ExCTRL{"), toStr);
    }
}