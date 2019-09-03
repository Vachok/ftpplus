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
    
    private InternetUse internetUse;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @BeforeMethod
    public void setInetUse() {
        this.internetUse = InternetUse.getInstance("");
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
        internetUse.setClassOption("homya");
        String infoAbout = internetUse.getInfoAbout("homya");
        Assert.assertTrue(infoAbout.contains("n.s.homyakova"), infoAbout);
        Assert.assertTrue(infoAbout.contains("время открытых сессий"), infoAbout);
    }
    
    @Test
    public void testGetInfo() {
        internetUse.setClassOption("do0008");
        String info = internetUse.getInfo();
        Assert.assertTrue(info.contains("TCP_DENIED"), info);
        Assert.assertTrue(info.contains("bytes"), info);
        Assert.assertTrue(info.contains("seconds"), info);
        Assert.assertTrue(info.contains("Посмотреть сайты (BETA)"), info);
    }
}