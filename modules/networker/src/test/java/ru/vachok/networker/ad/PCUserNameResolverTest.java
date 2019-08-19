// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.net.scanner.NetListsTest;


/**
 @see PCUserNameResolver
 @since 16.08.2019 (20:37) */
public class PCUserNameResolverTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    private InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.TYPE_INETUSAGE);
    
    private String pcName = "do0001";
    
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
    public void testGetInfo() {
        String infoAboutName = informationFactory.getInfoAbout(pcName);
        Assert.assertTrue(infoAboutName.contains("/200 GET"), infoAboutName);
        this.pcName = "10.200.213.85";
        String infoAboutIP = informationFactory.getInfoAbout(pcName);
        Assert.assertTrue(infoAboutIP.contains("a href"), infoAboutIP);
    }
    
    @Test
    public void testGetInfoAbout() {
    }
    
    @Test
    public void testTestToString() {
        String toStr = informationFactory.toString();
        Assert.assertTrue(toStr.contains("InetUserPCName{"), toStr);
    }
}