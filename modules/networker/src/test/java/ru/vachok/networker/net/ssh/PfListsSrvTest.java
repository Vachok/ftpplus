package ru.vachok.networker.net.ssh;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


public class PfListsSrvTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(PfListsSrv.class.getSimpleName(), System.nanoTime());

    private PfListsSrv pfListsSrv;

    @BeforeMethod
    public void initListsSrv() {
        this.pfListsSrv = new PfListsSrv(new PfLists());
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
    public void testGetCommandForNatStr() {
        String str = pfListsSrv.getCommandForNatStr();
        Assert.assertEquals(str, "sudo cat /etc/pf/allowdomain && exit");
    }

    @Test
    public void testGetDefaultConnectSrv() {
        String srv = PfListsSrv.getDefaultConnectSrv();
        Assert.assertEquals(srv, "192.168.13.42");
    }

    @Test
    public void testRunCom() {
        String s = pfListsSrv.runCom();
        Assert.assertTrue(s.contains(".networker.vachok.ru<br>"));
    }

    @Test
    public void testMakeListRunner() {
        Assert.assertTrue(pfListsSrv.makeListRunner());
    }

    @Test
    public void testTestToString() {
        String s = pfListsSrv.toString();
        Assert.assertTrue(s.contains("PfListsSrv{"));
    }
}