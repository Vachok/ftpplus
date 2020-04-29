package ru.vachok.networker.net.ssh;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;


/**
 @see VpnHelper */
public class VpnHelperTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(VpnHelper.class.getSimpleName(), System.nanoTime());

    private final VpnHelper vpnHelper = new VpnHelper();

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
    public void testGetStatus() {
        String statusVpn = vpnHelper.getStatus();
        Assert.assertTrue(statusVpn.contains(ConstantsFor.VPN_LIST), statusVpn);
    }

    /**
     @see VpnHelper#getConfig(String)
     */
    @Test
    public void testGetConfig() {
        String kudrConf = vpnHelper.getConfig("kudr");
        Assert.assertTrue(kudrConf.contains("kudrhome.crt"), kudrConf);
        System.out.println("kudrConf = " + kudrConf);
    }

    @Test
    public void testTestToString() {
        String s = vpnHelper.toString();
        Assert.assertTrue(s.contains("VpnHelper["), s);
    }
}