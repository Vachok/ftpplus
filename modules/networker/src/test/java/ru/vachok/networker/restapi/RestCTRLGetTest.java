package ru.vachok.networker.restapi;


import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import javax.servlet.http.HttpServletRequest;


/**
 @see RestCTRLGet */
public class RestCTRLGetTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(RestCTRLGetTest.class.getSimpleName(), System.nanoTime());

    private RestCTRLGet restCTRLGet;

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
    public void initField() {
        this.restCTRLGet = new RestCTRLGet();
    }

    @Test
    public void testGetAllowDomains() {
        String domains = restCTRLGet.getAllowDomains();
        Assert.assertTrue(domains.contains(".velkomfood.ru"), domains);
    }

    @Test
    public void testCollectOldFiles() {
        String oldFilesStr = restCTRLGet.collectOldFiles();
        Assert.assertTrue(oldFilesStr.contains("Total file size in DB now"), oldFilesStr);
    }

    @Test
    public void testUniqPC() {
        HttpServletRequest request = new MockHttpServletRequest();
        String uPc = restCTRLGet.uniqPC(request);
        Assert.assertTrue(uPc.contains("\"ip\":\"10.200.200.1\",\"pcname\":\"10.200.200.1\""), uPc);
    }

    @Test
    public void testAppStatus() {
        String appSt = restCTRLGet.appStatus();
        Assert.assertTrue(appSt.contains("CPU information"), appSt);
    }

    @Test
    public void testDbInfoRest() {
        String dbInfo = restCTRLGet.dbInfoRest();
        Assert.assertTrue(dbInfo.contains("BYTES_RECEIVED"), dbInfo);
    }

    @Test
    public void testFileShow() {
        String fileShow = restCTRLGet.fileShow(new MockHttpServletRequest());
        Assert.assertTrue(fileShow.contains("ConstantsFor.properties"), fileShow);
    }

    @Test
    public void testSshRest() {
        String sshStr = restCTRLGet.sshRest(new MockHttpServletRequest());
        Assert.assertTrue(sshStr.contains("stdSquid"), sshStr);
    }

    @Test
    public void testTestToString() {
        String s = restCTRLGet.toString();
        Assert.assertTrue(s.contains("RestCTRLGet["), s);
    }
}