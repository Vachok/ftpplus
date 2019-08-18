// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;


/**
 @see InetIPUser
 @since 09.06.2019 (21:24) */
@SuppressWarnings("ALL") public class InetIPUserTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.INET_USAGE);
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    /**
     @see InetIPUser#getUsage(String)
     */
    @Test
    public void testGetUsage() {
    
        String infoAboutDO = informationFactory.getInfoAbout("do0001");
        String infoAboutUser = informationFactory.getInfoAbout("vinok");
        System.out.println("infoAboutUser = " + infoAboutUser);
    
        Assert.assertTrue(infoAboutDO.contains("Посмотреть сайты"), infoAboutDO);
        Assert.assertTrue(infoAboutUser.contains("Показаны только <b>уникальные</b> сайты"), infoAboutUser);
        
    }
    
    @Test
    public void testGetConnectStatistics() {
        String statistics = InternetUse.getConnectStatistics("do0001");
        Assert.assertTrue(statistics.contains("do0001 : "), statistics);
        String statisticsCast = ((InternetUse) informationFactory).getConnectStatistics("do0001");
        Assert.assertEquals(statistics, statisticsCast);
    }
    
    @Test
    public void testToString() {
        String toStr = informationFactory.toString();
        Assert.assertTrue(toStr.contains("InetUserPCName{"));
    }
}