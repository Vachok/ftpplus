// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;

import java.util.UnknownFormatConversionException;


/**
 @see PCInfo
 @since 16.08.2019 (10:43) */
public class PCInfoTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(PCInfoTest.class.getSimpleName(), System
        .nanoTime());
    
    private InformationFactory informationFactory = InformationFactory.getInstance("do0213");
    
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
        informationFactory.setOption("do0001");
        String toStr = informationFactory.toString();
        Assert.assertTrue(toStr.contains("do0001"), toStr);
    }
    
    @Test
    public void testCheckValidName() {
        String doIp = PCInfo.checkValidName("10.200.213.85").toLowerCase();
        String do0213Dom = PCInfo.checkValidName("do0213.eatmeat.ru").toLowerCase();
        String do0213 = PCInfo.checkValidName("do0213").toLowerCase();
    
        Assert.assertEquals(do0213, "do0213");
        Assert.assertEquals(do0213Dom, "do0213");
        Assert.assertEquals(doIp, "do0213");
        
        try {
            String unknown = PCInfo.checkValidName("jdoe");
        }
        catch (UnknownFormatConversionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        
        
    }
    
    @Test
    public void checkInvalidName() {
        try {
            String invalidNameCheck = PCInfo.checkValidName("tt05");
        }
        catch (UnknownFormatConversionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        try {
            String validIP = PCInfo.checkValidName("8.8.8.8");
        }
        catch (IllegalArgumentException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        
    }
    
    private void checkFactory(@NotNull InformationFactory factory) {
        String getInfoToString = factory.toString();
        String factoryInfo = factory.getInfo();
        System.out.println("getInfoToString = " + getInfoToString);
        System.out.println("factoryInfo = " + factoryInfo);
    }
}