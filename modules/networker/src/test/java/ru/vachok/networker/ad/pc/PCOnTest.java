// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLInfo;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.net.scanner.PcNamesScanner;


/**
 @see PCOn
 @since 23.06.2019 (15:11) */
@SuppressWarnings("ALL")
public class PCOnTest {
    
    
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
    public void testGetInfoAbout() {
    
        HTMLInfo htmlInfo = new PcNamesScanner();
        String infoWorkerString = htmlInfo.fillAttribute("do0045");
        Assert.assertTrue(infoWorkerString.contains("kpivovarov"), infoWorkerString);
    
        htmlInfo.setClassOption("do0213.eatmeat.ru");
        infoWorkerString = htmlInfo.fillAttribute("do0213.eatmeat.ru");
        Assert.assertTrue(infoWorkerString.contains("ikudryashov"));
    }
    
    @Test
    public void testToString1() {
        InformationFactory informationFactory = new PCOn("do0001");
        Assert.assertTrue(informationFactory.toString().contains("CurrentPCUser["), informationFactory.toString());
        informationFactory.setClassOption("pp0001");
        Assert.assertTrue(informationFactory.toString().contains("pcName = 'pp0001'"));
    }
    
    @Test
    public void testGetInfo() {
        InformationFactory instance = InformationFactory.getInstance(InformationFactory.LOCAL);
        instance.setClassOption("do0213");
        String toStr = instance.toString();
        Assert.assertTrue(toStr.contains("PCOn["), toStr);
        Assert.assertTrue(toStr.contains("pcName = 'do0213'"), toStr);
        instance.setClassOption("do0001");
        Assert.assertTrue(instance.toString().contains("pcName = 'do0001'"), instance.toString());
    }
}