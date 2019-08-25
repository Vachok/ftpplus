package ru.vachok.networker.componentsrepo;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.scanner.NetListsTest;

import java.net.InetAddress;


/**
 @see NameOrIPChecker
 @since 25.08.2019 (13:30) */
public class NameOrIPCheckerTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
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
    public void testIsLocalAddress() {
        boolean address = new NameOrIPChecker("do0213").isLocalAddress();
        Assert.assertTrue(address);
    }
    
    @Test
    public void testResolveInetAddress() {
        InetAddress inetAddress = new NameOrIPChecker("do0213").resolveInetAddress();
        String hostName = inetAddress.getHostName();
        String hostAddress = inetAddress.getHostAddress();
        Assert.assertEquals(hostAddress, "10.200.213.85");
        Assert.assertEquals(hostName, "do0213.eatmeat.ru");
    }
    
    @Test
    public void resolveBYIP() {
        InetAddress inetAddress = new NameOrIPChecker("10.200.213.85").resolveInetAddress();
        InetAddress inetAddressNat = new NameOrIPChecker("192.168.13.30").resolveInetAddress();
        Assert.assertEquals(inetAddress.toString(), "/10.200.213.85");
        Assert.assertEquals(inetAddress.getHostName(), "do0213.eatmeat.ru");
    }
    
    @Test
    public void testTestToString() {
        String toStr = new NameOrIPChecker("10.200.213.85").toString();
        Assert.assertTrue(toStr.contains("NameOrIPChecker{"), toStr);
    }
}