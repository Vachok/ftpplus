package ru.vachok.networker.restapi;


import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;


/**
 @see RestCTRL */
public class RestCTRLTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(RestCTRLTest.class.getSimpleName(), System.nanoTime());

    private InformationFactory instance;

    private RestCTRL restCTRL;

    @BeforeMethod
    public void initInst() {
        this.instance = InformationFactory.getInstance(InformationFactory.REST_PC_UNIQ);
        this.restCTRL = new RestCTRL();
    }

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
    public void testAppStatus() {
        String s = restCTRL.appStatus();
        Assert.assertTrue(s.contains("amd64 Arch"));
    }

    @Test
    public void uniqPC() {

        String info = instance.getInfo();
        Assert.assertTrue(info.contains("10.10.10.1"));
        instance.setClassOption(true);
        info = instance.getInfo();
        Assert.assertTrue(info.contains("{\"ip\":\"10.10.10.1\",\"pcname\":\"10.10.10.1\"}"));
    }

    @Test
    public void testFileShow() {
        String fS = restCTRL.fileShow(new MockHttpServletRequest());
        Assert.assertTrue(fS.contains("exit.last"));
    }

    @Test
    public void testDbInfoRest() {
        String dbInfo = restCTRL.dbInfoRest();
        System.out.println("dbInfo = " + dbInfo);
    }
}