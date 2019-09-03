// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.NetScanService;

import java.util.UnknownFormatConversionException;


/**
 @see PCInfo
 @since 16.08.2019 (10:43) */
public class PCInfoTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(PCInfoTest.class.getSimpleName(), System
        .nanoTime());
    
    private PCInfo informationFactory = PCInfo.getInstance("do0213");
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testTestToString() {
        informationFactory.setClassOption("do0001");
        String toStr = informationFactory.toString();
        Assert.assertTrue(toStr.contains("do0001"), toStr);
    }
    
    @Test
    public void testCheckValidName() {
        String doIp = PCInfo.checkValidNameWithoutEatmeat("10.200.213.85").toLowerCase();
        String do0213Dom = PCInfo.checkValidNameWithoutEatmeat("do0213.eatmeat.ru").toLowerCase();
        String do0213 = PCInfo.checkValidNameWithoutEatmeat("do0213").toLowerCase();
    
        Assert.assertEquals(do0213, "do0213");
        Assert.assertEquals(do0213Dom, "do0213");
        Assert.assertEquals(doIp, "do0213");
        
        try {
            String unknown = PCInfo.checkValidNameWithoutEatmeat("jdoe");
        }
        catch (UnknownFormatConversionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        
        
    }
    
    @Test
    public void checkInvalidName() {
        try {
            String invalidNameCheck = PCInfo.checkValidNameWithoutEatmeat("tt05");
        }
        catch (UnknownFormatConversionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        try {
            String validIP = PCInfo.checkValidNameWithoutEatmeat("8.8.8.8");
        }
        catch (IllegalArgumentException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        
    }
    
    @Test
    public void testGetInstance() {
        if (NetScanService.isReach("do0213")) {
            Assert.assertTrue(informationFactory.toString().contains("PCOn["));
        }
        else {
            Assert.assertTrue(informationFactory.toString().contains("PCOn["), informationFactory.toString());
        }
    }
    
    @Test
    public void testGetInfoAbout() {
        this.informationFactory = PCInfo.getInstance("10.200.213.85");
        String infoAbout = informationFactory.getInfoAbout("10.200.213.85");
        if (NetScanService.isReach("10.200.213.85")) {
            Assert.assertTrue(infoAbout.toLowerCase().contains("do0213 - ikudryashov"), infoAbout);
        }
        this.informationFactory = PCInfo.getInstance("do0045");
        infoAbout = informationFactory.getInfoAbout("do0045");
        Assert.assertTrue(infoAbout.contains("do0045 - kpivovarov"), infoAbout);
        
        informationFactory = PCInfo.getInstance("do0045");
        infoAbout = informationFactory.getInfoAbout("do0045");
        Assert.assertTrue(infoAbout.contains("Крайнее имя пользователя на ПК"), infoAbout);
    }
    
    @Test
    public void testCheckValidNameWithoutEatmeat() {
        String do0213 = informationFactory.checkValidNameWithoutEatmeat("do0213");
        Assert.assertEquals(do0213, "do0213");
        
        String do0213E = informationFactory.checkValidNameWithoutEatmeat("do0213.eatmeat.ru");
        Assert.assertEquals(do0213E, "do0213");
    }
    
    @Test
    public void testGetInfo() {
        String info = informationFactory.getInfo();
        Assert.assertTrue(info.contains("a href=\"/ad?"), info);
    }
    
    @Test
    public void testDefaultInformation() {
        String do0045 = PCInfo.defaultInformation("do0045", true);
        Assert.assertTrue(do0045.contains("font color=\"white\""), do0045);
        Assert.assertTrue(do0045.contains("TOTAL"), do0045);
        
        String do0213 = PCInfo.defaultInformation("do0213", false);
        Assert.assertTrue(do0213.contains("Last online"), do0213);
    }
    
    private void checkFactory(@NotNull InformationFactory factory) {
        String getInfoToString = factory.toString();
        String factoryInfo = factory.getInfo();
        System.out.println("getInfoToString = " + getInfoToString);
        System.out.println("factoryInfo = " + factoryInfo);
    }
}