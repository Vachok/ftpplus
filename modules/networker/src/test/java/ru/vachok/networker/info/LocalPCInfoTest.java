// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.scanner.NetListsTest;


/**
 @see LocalPCInfo
 @since 18.08.2019 (23:41) */
public class LocalPCInfoTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    private InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.LOCAL);
    
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
    public void getInfoAbout() {
        String infoAbout = informationFactory.getInfoAbout("do0001");
        Assert.assertFalse(infoAbout.contains("null"), infoAbout);
        System.out.println("infoAbout = " + infoAbout);
    }
    
    @Test
    public void testGetInfo() {
        String info = informationFactory.getInfo();
        System.out.println("info = " + info);
    }
    
    @Test
    public void testGetUserByPCNameFromDB() {
        throw new InvokeEmptyMethodException("18.08.2019 (23:42)");
    }
}