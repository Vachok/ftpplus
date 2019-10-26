package ru.vachok.networker.ad.inet;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLInfo;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.stats.Stats;

import java.util.stream.Stream;


/**
 @see AccessLogHTMLMaker
 @since 27.08.2019 (11:28) */
public class AccessLogHTMLMakerTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(AccessLogHTMLMaker.class
            .getSimpleName(), System.nanoTime());
    
    private AccessLogHTMLMaker accessLog;
    
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
    public void initMaker() {
        this.accessLog = new AccessLogHTMLMaker();
    }
    
    @Test
    public void testToString() {
        Assert.assertTrue(accessLog.toString().contains("AccessLogHTMLMaker{"), accessLog.toString());
    }
    
    @Test
    public void testFillWebModel() {
        String fillWebModelStr = accessLog.fillWebModel();
        Assert.assertTrue(fillWebModelStr.contains("Посмотреть сайты (BETA)"));
        accessLog.setClassOption("do0011");
        fillWebModelStr = accessLog.fillWebModel();
        Assert.assertTrue(fillWebModelStr.contains("ALLOWED SITES"), fillWebModelStr);
        if (!Stats.isSunday()) {
            Assert.assertTrue(fillWebModelStr.contains("a href"), fillWebModelStr);
        }
    }
    
    @Test
    public void testFillAttribute() {
        String fillAttributeStr = accessLog.fillAttribute("do0056");
        Assert.assertTrue(fillAttributeStr.contains("время открытых сессий"), fillAttributeStr);
    }
    
    @Test
    public void testSetClassOption() {
        this.accessLog.setClassOption("do0213");
        String toStr = accessLog.toString();
        Assert.assertTrue(toStr.contains("aboutWhat='do0213'"), toStr);
    }
    
    @Test
    public void testTestEquals() {
        accessLog.setClassOption("do0001");
        Assert.assertFalse(this.accessLog.equals(new AccessLogHTMLMaker()));
    }
    
    @Test
    public void testTestHashCode() {
        this.accessLog = new AccessLogHTMLMaker();
        HTMLInfo info = (HTMLInfo) HTMLGeneration.getInstance(InformationFactory.ACCESS_LOG_HTMLMAKER);
        info.setClassOption("do0001");
        Assert.assertFalse(this.accessLog.hashCode() == info.hashCode(), info.toString() + "\n" + accessLog.toString());
        
    }
    
    @Test
    public void testGetInfoAbout() {
        String do0086 = accessLog.getInfoAbout("do0086");
        boolean isUser = Stream.of("msc", "d.yu.podbuckii", "a.v.nikolaev").anyMatch(do0086::contains);
        Assert.assertTrue(isUser, do0086);
    }
    
    @Test
    public void testGetInfo() {
        this.accessLog = new AccessLogHTMLMaker();
        String info = accessLog.getInfo();
        Assert.assertTrue(info.contains("Set classOption! AccessLogHTMLMaker{"), info);
        accessLog.setClassOption("do0008");
        info = accessLog.getInfo();
        Assert.assertTrue(info.contains("ALLOWED SITES"), info);
        if (!Stats.isSunday()) {
            Assert.assertTrue(info.contains("TCP_DENIED"), info);
        }
    }
}