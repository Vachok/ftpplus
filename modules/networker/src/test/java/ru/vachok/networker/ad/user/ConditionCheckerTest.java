// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;


/**
 @see ConditionChecker
 @since 23.06.2019 (15:11) */
@SuppressWarnings("ALL") public class ConditionCheckerTest {
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    
    @Test
    public void testGetInfoAbout() {
    
        InformationFactory informationFactory = new ConditionChecker("select * from velkompc where NamePP like ?");
        String infoWorkerString = informationFactory.getInfoAbout("do0045.eatmeat.ru:true");
        Assert.assertTrue(infoWorkerString.contains("kpivovarov"));
    
        informationFactory = new ConditionChecker("select * from pcuser where pcName like ?");
        infoWorkerString = informationFactory.getInfoAbout("do0213.eatmeat.ru:false");
        Assert.assertTrue(infoWorkerString.contains("ikudryashov"));
    }
    
    @Test
    public void testToString1() {
        InformationFactory informationFactory = new ConditionChecker("");
        Assert.assertEquals(informationFactory.toString().getBytes(), "ConditionChecker{}".getBytes());
    }
}