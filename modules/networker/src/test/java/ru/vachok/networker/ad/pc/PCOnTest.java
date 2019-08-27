// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;


/**
 @see PCOn
 @since 23.06.2019 (15:11) */
@SuppressWarnings("ALL")
public class PCOnTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private PCInfo pcInfo = new PCOn("do0045");
    
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
        String infoAbout = pcInfo.getInfoAbout("do0045");
        System.out.println("infoAbout = " + infoAbout);
    }
    
    @Test
    public void trueInfoAbout() {
        PCInfo pcInfo = PCInfo.getInstance("do0045");
        String infoAbout = pcInfo.getInfoAbout("do0045");
        Assert.assertTrue(infoAbout.contains("<br><b><a href=\"/ad?do0045\">"), infoAbout);
        infoAbout = pcInfo.getInfoAbout("do0213");
        System.out.println("infoAbout = " + infoAbout);
    }
    
    @Test
    public void testToString1() {
        PCInfo informationFactory = new PCOn("do0001");
        Assert.assertTrue(informationFactory.toString().contains("PCOn["), informationFactory.toString());
        informationFactory.setClassOption("pp0001");
        Assert.assertTrue(informationFactory.toString().contains("pcName = 'pp0001'"));
    }
    
    @Test
    public void testGetInfo() {
        InformationFactory instance = InformationFactory.getInstance("do0045");
        instance.setClassOption("do0045");
        String toStr = instance.toString();
        Assert.assertTrue(toStr.contains("PCOn["), toStr);
        Assert.assertTrue(toStr.contains("pcName = 'do0045'"), toStr);
        instance.setClassOption("do0001");
        Assert.assertTrue(instance.toString().contains("pcName = 'do0001'"), instance.toString());
    }
}