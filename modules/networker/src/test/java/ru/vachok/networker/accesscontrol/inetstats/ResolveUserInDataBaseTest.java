// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.ad.user.ResolveUserInDataBase;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.statistics.Stats;


/**
 @see ResolveUserInDataBase
 @since 09.06.2019 (21:24) */
@SuppressWarnings("ALL")
public class ResolveUserInDataBaseTest {
    
    
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
     @see ResolveUserInDataBase#getUsage(String)
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
        @NotNull Stats inetStats = InternetUse.getInetUse();
        String statistics = inetStats.getInfoAbout("do0001");
        
        Assert.assertTrue(statistics.contains("do0001 : "), statistics);
    
    }
    
    @Test
    public void testToString() {
        String toStr = informationFactory.toString();
        Assert.assertTrue(toStr.contains("InetUserPCName{"));
    }
}