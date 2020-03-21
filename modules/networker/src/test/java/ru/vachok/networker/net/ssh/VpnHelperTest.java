package ru.vachok.networker.net.ssh;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see VpnHelper */
public class VpnHelperTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(VpnHelper.class.getSimpleName(), System.nanoTime());

    private VpnHelper vpnHelper = new VpnHelper();

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
    public void getStatusTest() {
        String statusVpn = vpnHelper.getStatus();
        System.out.println("statusVpn = " + statusVpn);
    }
}