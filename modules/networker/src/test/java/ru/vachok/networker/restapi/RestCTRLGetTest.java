package ru.vachok.networker.restapi;


import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;

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
        Assert.assertTrue(domains.contains(".networker.vachok.ru"), domains);
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
        Assert.assertTrue(fileShow.contains(FileNames.CONSTANTSFOR_PROPERTIES), fileShow);
    }

    @Test
    public void testSshRest() {
        try {
            Thread.sleep(1500);
        }
        catch (InterruptedException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        finally {
            String sshStr = restCTRLGet.sshRest(new MockHttpServletRequest());
            Assert.assertTrue(sshStr.contains(ConstantsFor.JSON_OBJECT_STD_SQUID), sshStr);
        }
    }

    @Test
    public void testTestToString() {
        String s = restCTRLGet.toString();
        Assert.assertTrue(s.contains("RestCTRLGet["), s);
    }

    @Test
    public void testShowAppProps() {
        String s = restCTRLGet.showAppProps();
        Assert.assertTrue(s.contains("props_"));
        Assert.assertTrue(s.contains("pref_"));
    }
}