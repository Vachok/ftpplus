// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLInfo;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.scanner.NetListsTest;


/**
 @see DBPCHTMLInfo
 @since 18.08.2019 (23:41) */
public class DBPCHTMLInfoTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    private HTMLInfo dbpchtmlInfo = new DBPCHTMLInfo();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        dbpchtmlInfo.setClassOption("do0213");
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testTestToString() {
        String do0213 = dbpchtmlInfo.toString();
        Assert.assertTrue(do0213.contains("DBPCInfo{"), do0213);
    }
    
    @Test
    public void testFillWebModel() {
        throw new InvokeEmptyMethodException("testFillWebModel created 27.08.2019 (12:46)");
    }
    
    @Test
    public void testFillAttribute() {
        throw new InvokeEmptyMethodException("testFillAttribute created 27.08.2019 (12:46)");
    }
    
    @Test
    public void testSetClassOption() {
        throw new InvokeEmptyMethodException("testSetClassOption created 27.08.2019 (12:46)");
    }
    
    @Test
    public void testLastOnline() {
        throw new InvokeEmptyMethodException("testLastOnline created 27.08.2019 (12:46)");
    }
    
    @Test
    public void testCountOnOff() {
        throw new InvokeEmptyMethodException("testCountOnOff created 27.08.2019 (12:46)");
    }
}