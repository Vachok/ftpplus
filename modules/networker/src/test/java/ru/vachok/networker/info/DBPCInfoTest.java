// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.scanner.NetListsTest;


/**
 @see DBPCInfo
 @since 18.08.2019 (23:41) */
public class DBPCInfoTest {
    
    
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
    public void testTestToString() {
        String do0213 = new DBPCInfo("do0213").toString();
        Assert.assertTrue(do0213.contains("DBPCInfo["), toString());
    }
    
    @Test
    public void testGetPCbyUser() {
        throw new InvokeEmptyMethodException("testGetPCbyUser created 22.08.2019 (9:55)");
    }
    
    @Test
    public void testSetClassOption() {
        throw new InvokeEmptyMethodException("testSetClassOption created 22.08.2019 (9:55)");
    }
    
    @Test
    public void testGetInfoAbout() {
        throw new InvokeEmptyMethodException("testGetInfoAbout created 22.08.2019 (9:55)");
    }
    
    @Test
    public void testGetInfo() {
        throw new InvokeEmptyMethodException("testGetInfo created 22.08.2019 (9:55)");
    }
    
    @Test
    public void testLastOnline() {
        throw new InvokeEmptyMethodException("testLastOnline created 22.08.2019 (9:55)");
    }
}