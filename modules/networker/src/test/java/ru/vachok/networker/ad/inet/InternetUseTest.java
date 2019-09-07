// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.inet;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
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
        Assert.assertTrue(toStr.contains("AccessLogUSER{"), toStr);
    }
    
    @Test
    @Ignore
    public void testCleanTrash() {
        int cleanedRows = InternetUse.getCleanedRows();
        Assert.assertTrue(cleanedRows == 0, cleanedRows + " cleanedRows");
    }
    
    @Test
    public void testGetInstance() {
        InternetUse instanceEmpt = InternetUse.getInstance("");
        String toStr = instanceEmpt.toString();
        Assert.assertTrue(toStr.contains("AccessLogUSER{"), toStr);
        InternetUse instanceAccessLog = InternetUse.getInstance(InformationFactory.ACCESS_LOG_HTMLMAKER);
        toStr = instanceAccessLog.toString();
        Assert.assertTrue(toStr.contains("AccessLogHTMLMaker{"), toStr);
        
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.INET_USAGE);
        toStr = informationFactory.toString();
        Assert.assertTrue(toStr.contains("AccessLogHTMLMaker{"), toStr);
    }
    
    @Test(invocationCount = 3)
    public void testGetInfoAbout() {
        internetUse.setClassOption("homya");
        String infoAbout = internetUse.getInfoAbout("homya");
        if (!UsefulUtilities.thisPC().toLowerCase().contains("home")) {
            Assert.assertTrue(infoAbout.contains("n.s.homyakova"), infoAbout);
        }
        Assert.assertTrue(infoAbout.contains("время открытых сессий"), infoAbout);
    }
    
    @Test
    public void testGetInfo() {
        internetUse.setClassOption("do0056");
        String info = internetUse.getInfo();
        Assert.assertTrue(info.contains("мегабайт трафика"));
        Assert.assertTrue(info.contains("время открытых сессий"), info);
    }
}