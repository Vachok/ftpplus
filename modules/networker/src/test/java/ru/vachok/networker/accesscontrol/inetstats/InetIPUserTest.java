// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.DatabaseInfo;
import ru.vachok.networker.info.InformationFactory;


/**
 @see InetIPUser
 @since 09.06.2019 (21:24) */
@SuppressWarnings("ALL") public class InetIPUserTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private DatabaseInfo internetUse = (DatabaseInfo) InformationFactory.getInstance("do0001");
    
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
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.TYPE_INETUSAGE);
        String infoAboutDO = informationFactory.getInfoAbout("do0001");
        String infoAboutUser = informationFactory.getInfoAbout("vinok");
        System.out.println("infoAboutUser = " + infoAboutUser);
    
        Assert.assertTrue(infoAboutDO.contains("Посмотреть сайты, где был компьютер"), infoAboutDO);
        Assert.assertTrue(infoAboutUser.contains("Посмотреть сайты, где был юзер"), infoAboutUser);
        
    }
    
    @Test
    public void testGetConnectStatistics() {
        InformationFactory instance = InformationFactory.getInstance(InformationFactory.TYPE_INETUSAGE);
        String connectStatistics = ((InternetUse) instance).getConnectStatistics();
        System.out.println("connectStatistics = " + connectStatistics);
    }
    
    @Test
    public void testToString() {
        throw new InvokeEmptyMethodException("18.08.2019 (14:15)");
    }
}