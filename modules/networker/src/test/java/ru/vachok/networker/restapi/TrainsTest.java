package ru.vachok.networker.restapi;


import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.tutu.conf.BackEngine;
import ru.vachok.tutu.parser.SiteParser;

import java.util.Date;
import java.util.List;


public class TrainsTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
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
    public void getTrains() {
        BackEngine backEngine = new SiteParser();
        List<Date> comingTrains = backEngine.getComingTrains();
        System.out.println("comingTrains = " + AbstractForms.fromArray(comingTrains));
    }
}
