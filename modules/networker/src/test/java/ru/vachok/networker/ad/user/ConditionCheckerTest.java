// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.InfoWorker;


/**
 @see ConditionChecker
 @since 23.06.2019 (15:11) */
@SuppressWarnings("ALL") public class ConditionCheckerTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    
    @Test
    public void testGetInfoAbout() {
    
        InfoWorker infoWorker = new ConditionChecker("select * from velkompc where NamePP like ?", "do0045.eatmeat.ru:true");
        String infoWorkerString = infoWorker.getInfoAbout();
        Assert.assertTrue(infoWorkerString.contains("kpivovarov"));
    
        infoWorker = new ConditionChecker("select * from pcuser where pcName like ?", "do0213.eatmeat.ru:false");
        infoWorkerString = infoWorker.getInfoAbout();
        Assert.assertTrue(infoWorkerString.contains("ikudryashov"));
    }
    
    @Test
    public void testSetInfo() {
        InfoWorker infoWorker = new ConditionChecker("select * from pcuser where pcName like ?", "do0213.eatmeat.ru:false");
        try {
            infoWorker.setInfo();
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testToString1() {
        InfoWorker infoWorker = new ConditionChecker("select * from pcuser where pcName like ?", "do0213.eatmeat.ru:false");
        Assert.assertEquals(infoWorker.toString().getBytes(), "ConditionChecker{}".getBytes());
    }
}