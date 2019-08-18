// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.scanner.NetListsTest;


/**
 * @see PCLast20UserSearcher
 * @since 18.08.2019 (19:07)
 */
public class PCLast20UserSearcherTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.SEARCH_PC_IN_DB);
    
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
        String toStr = informationFactory.toString();
        Assert.assertTrue(toStr.contains("PCLast20UserSearcher{"), toStr);
    }
    
    @Test
    public void testGetInfo() {
        try {
            String gettedInfo = informationFactory.getInfo();
            System.out.println("gettedInfo = " + gettedInfo);
        }
        catch (TODOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetUserPCFromDB() {
        String kudrPC = informationFactory.getInfoAbout("do0213");
        Assert.assertFalse(kudrPC.contains("EXCEPTION in SQL"));
        Assert.assertTrue(kudrPC.contains("ikudryashov"));
    }
    
    @Test
    public void testGetInfoAbout() {
        try {
            String infoAbout = informationFactory.getInfoAbout("kpivo");
            Assert.assertFalse(infoAbout.contains("EXCEPTION in SQL"));
            Assert.assertTrue(infoAbout.contains("do0045"));
        }
        catch (TODOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}