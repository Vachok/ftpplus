// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info.inet;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;

import java.util.concurrent.*;


/**
 @see InternetUse
 @since 13.08.2019 (8:46) */
@SuppressWarnings("OverlyStrongTypeCast")
public class InternetUseTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(InternetUse.class.getSimpleName(), System.nanoTime());
    
    private InformationFactory internetUse = InformationFactory.getInstance(InformationFactory.INET_USAGE);
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        internetUse.getInfoAbout("do0001");
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testGetUsage() {
        String inetUsage = internetUse.getInfoAbout("do0001");
        Assert.assertTrue(inetUsage.contains("TCP_TUNNEL/200 CONNECT"), inetUsage);
        inetUsage = internetUse.getInfoAbout("do0001");
        System.out.println("inetUsage = " + inetUsage);
    }
    
    @Test
    public void testGetConnectStatistics() {
        internetUse.setClassOption("do0001");
        String inetStat = internetUse.getInfo();
        System.out.println("inetStat = " + inetStat);
    }
    
    @Test
    public void testCall() {
        Future<Object> submit = Executors.newSingleThreadExecutor().submit(((InternetUse) internetUse));
        try {
            Integer integer = (Integer) submit.get(30, TimeUnit.SECONDS);
            Assert.assertTrue(integer > 0, String.valueOf(integer));
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = internetUse.getInfoAbout("192.168.13.220");
        System.out.println("infoAbout = " + infoAbout);
    }
}