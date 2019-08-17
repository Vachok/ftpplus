// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.util.UnknownFormatConversionException;


/**
 @see DatabaseInfo
 @since 16.08.2019 (10:43) */
public class DatabaseInfoTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(DatabaseInfoTest.class.getSimpleName(), System
            .nanoTime());
    
    private InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.TYPE_SEARCHDB);
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
    public void testGetUserPCFromDB() {
    }
    
    @Test
    public void testGetPCUsersFromDB() {
    }
    
    @Test
    public void testGetInfo() {
        InformationFactory informationUser = InformationFactory.getInstance("kudr");
        checkFactory(informationUser);
        InformationFactory informationPC = InformationFactory.getInstance("do0213");
        checkFactory(informationPC);
    }
    
    @Test
    public void testGetCurrentPCUsers() {
    }
    
    @Test
    public void testGetInfoInstance() {
    
    }
    
    @Test
    public void testCleanTrash() {
    }
    
    @Test
    public void testGetConnectStatistics() {
        String conStat;
        try {
            conStat = ((DatabaseInfo) informationFactory).getConnectStatistics();
        }
        catch (UnknownFormatConversionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            informationFactory.setClassOption("do0001");
            conStat = ((DatabaseInfo) informationFactory).getConnectStatistics();
        }
        System.out.println("conStat = " + conStat);
    }
    
    @Test
    public void testGetInfoAbout() {
    }
    
    @Test
    public void testTestToString() {
        informationFactory.setClassOption("do0001");
        String toStr = informationFactory.toString();
        Assert.assertTrue(toStr.contains("DatabaseUserSearcher{"));
        Assert.assertTrue(toStr.contains("do0001"));
    }
    
    private void checkFactory(@NotNull InformationFactory factory) {
        String getInfoToString = factory.toString();
        String factoryInfo = factory.getInfo();
        System.out.println("getInfoToString = " + getInfoToString);
        System.out.println("factoryInfo = " + factoryInfo);
    }
}