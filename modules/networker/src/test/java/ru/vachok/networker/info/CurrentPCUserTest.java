// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see CurrentPCUser
 @since 23.06.2019 (15:11) */
@SuppressWarnings("ALL")
public class CurrentPCUserTest {
    
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
    
        InformationFactory informationFactory = new CurrentPCUser("do0045.eatmeat.ru");
        String infoWorkerString = informationFactory.getInfoAbout("do0045");
        Assert.assertTrue(infoWorkerString.contains("kpivovarov"), infoWorkerString);
    
        informationFactory = new CurrentPCUser("do0213");
        infoWorkerString = informationFactory.getInfoAbout("do0213.eatmeat.ru");
        Assert.assertTrue(infoWorkerString.contains("ikudryashov"));
    }
    
    @Test
    public void testToString1() {
        InformationFactory informationFactory = new CurrentPCUser("do0001");
        Assert.assertTrue(informationFactory.toString().contains("ConditionChecker["));
        informationFactory.setClassOption("pp0001");
        Assert.assertTrue(informationFactory.toString().contains("pcName = 'pp0001'"));
    }
    
    @Test
    public void testGetInfo() {
    }
    
    @Test
    public void testSetClassOption() {
    }
}