package ru.vachok.networker.controller;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.ModelAttributeNames;

import javax.servlet.http.HttpServletRequest;


/**
 @see UserWebCTRL
 @since 09.08.2019 (13:46) */
public class UserWebCTRLTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(UserWebCTRL.class.getSimpleName(), System
        .nanoTime());
    
    private UserWebCTRL userWebCTRL = new UserWebCTRL();
    
    private Model model = new ExtendedModelMap();
    
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
    public void testUserGet() {
        String userGet = userWebCTRL.userGet(model, request);
        Assert.assertEquals(userGet, "user");
        Assert.assertEquals(model.asMap().get("title"), ModelAttributeNames.USERWEB);
    }
    
    @Test
    public void testUserPost() {
        String userPost = userWebCTRL.userPost(model, request, new ADUser());
        Assert.assertEquals(userPost, "user");
        Assert.assertEquals(model.asMap().get("title"), ModelAttributeNames.USERWEB);
    }
    
    @Test
    public void testAdSrvForUser() {
        ADSrv forUser = UserWebCTRL.adSrvForUser(new ADUser());
        Assert.assertTrue(forUser.toString().contains("ADSrv{"));
    }
    
    @Test
    public void testTestToString() {
        Assert.assertTrue(userWebCTRL.toString().contains("UserWebCTRL{"), userWebCTRL.toString());
    }
}