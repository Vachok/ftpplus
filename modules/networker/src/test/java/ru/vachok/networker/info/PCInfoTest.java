// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see PCInfo
 @since 16.08.2019 (10:43) */
public class PCInfoTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(PCInfoTest.class.getSimpleName(), System
            .nanoTime());
    
    private InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.SEARCH_PC_IN_DB);
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
        throw new InvokeEmptyMethodException("17.08.2019 (12:59)");
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
        throw new InvokeEmptyMethodException("17.08.2019 (12:59)");
    }
    
    @Test
    public void testGetInfoInstance() {
        throw new InvokeEmptyMethodException("17.08.2019 (13:00)");
    }
    
    @Test
    public void testCleanTrash() {
        int trashRows = ((PCInfo) informationFactory).cleanTrash();
        System.out.println("trashRows = " + trashRows);
        Assert.assertTrue(trashRows > 0);
    }
    
    @Test
    public void testGetInfoAbout() {
        throw new InvokeEmptyMethodException("17.08.2019 (13:00)");
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