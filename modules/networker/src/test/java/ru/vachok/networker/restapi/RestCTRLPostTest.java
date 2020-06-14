package ru.vachok.networker.restapi;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import static ru.vachok.networker.restapi.RestApiHelper.INVALID_USER;


public class RestCTRLPostTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(RestCTRLPostTest.class.getSimpleName(), System.nanoTime());

    private RestCTRLPost restCTRLPost;

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }

    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }

    @BeforeMethod
    public void initF() {
        this.restCTRLPost = new RestCTRLPost();
    }

    @Test
    public void testDelOldFiles() {
        String delOld = restCTRLPost.delOldFiles(new MockHttpServletRequest());
        Assert.assertTrue(delOld.contains("Total file size in DB now"), delOld);
    }

    @Test
    public void testHelpDomain() {
        String allowDomainStr = restCTRLPost.helpDomain(new MockHttpServletRequest(), new MockHttpServletResponse());
        Assert.assertEquals(allowDomainStr, "No content type. What are you mean?");
    }

    @Test
    public void testInetTemporary() {
        String inetTmpStr = restCTRLPost.inetTemporary(new MockHttpServletRequest(), new MockHttpServletResponse());
        Assert.assertEquals(inetTmpStr, INVALID_USER);
    }

    @Test
    public void testSshCommandExecute() {
        String sshComExecStr = restCTRLPost.sshCommandExecute(new MockHttpServletRequest());
        Assert.assertTrue(sshComExecStr.contains("NegativeArraySizeException:"), sshComExecStr);
    }

    @Test
    public void testDelDomain() {
        String delDomStr = restCTRLPost.delDomain(new MockHttpServletRequest());
        Assert.assertEquals(delDomStr, RestCTRLPost.INCORRECT_REQUEST);
    }

    @Test
    public void testGetVPNKey() {
        String keyGet = restCTRLPost.getVPNKey(new MockHttpServletRequest());
        Assert.assertEquals(keyGet, "getvpnkey error: No argument!");
    }

    @Test
    public void testTestToString() {
        String toS = restCTRLPost.toString();
        Assert.assertTrue(toS.contains("RestCTRLPost["), toS);
    }
}