// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.inet;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;


/**
 @see InternetUse
 @since 13.08.2019 (8:46) */
public class InternetUseTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(InternetUse.class.getSimpleName(), System.nanoTime());
    
    private InternetUse internetUse = InternetUse.getInstance("");
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        internetUse.getInfoAbout("do0001");
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testToString() {
        String toStr = internetUse.toString();
        Assert.assertTrue(toStr.contains("AccessLogHTMLMaker{"), toStr);
    }
    
    public void testCleanTrash() {
        int cleanedRows = InternetUse.getCleanedRows();
        Assert.assertTrue(cleanedRows == 0, cleanedRows + " cleanedRows");
    }
    
    @Test
    public void testGetInstance() {
        InternetUse instanceEmpt = InternetUse.getInstance("");
        String toStr = instanceEmpt.toString();
        Assert.assertTrue(toStr.contains("AccessLogHTMLMaker{"), toStr);
        InternetUse instanceAccessLog = InternetUse.getInstance(InformationFactory.ACCESS_LOG);
        toStr = instanceAccessLog.toString();
        Assert.assertTrue(toStr.contains("AccessLogUSER{"), toStr);
        
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.INET_USAGE);
        toStr = informationFactory.toString();
        Assert.assertTrue(toStr.contains("AccessLogHTMLMaker{"), toStr);
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = internetUse.getInfoAbout("kudr");
        Assert.assertTrue(infoAbout.contains("Unknown user"));
    }
    
    @Test
    public void testGetInfo() {
        internetUse.setOption("do0008");
        String info = internetUse.getInfo();
        Assert.assertTrue(info.contains("TCP_DENIED"), info);
        Assert.assertTrue(info.contains("bytes"), info);
        Assert.assertTrue(info.contains("seconds"), info);
        Assert.assertTrue(info.contains("Посмотреть сайты (BETA)"), info);
    }
}