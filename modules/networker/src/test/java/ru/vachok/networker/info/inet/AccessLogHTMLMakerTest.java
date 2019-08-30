package ru.vachok.networker.info.inet;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLInfo;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;


/**
 @see AccessLogHTMLMaker
 @since 27.08.2019 (11:28) */
public class AccessLogHTMLMakerTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(AccessLogHTMLMaker.class
            .getSimpleName(), System.nanoTime());
    
    private AccessLogHTMLMaker accessLog = new AccessLogHTMLMaker();
    
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
        Assert.assertTrue(fillWebModelStr.contains("TCP_MISS"), fillWebModelStr);
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
        HTMLInfo info = (HTMLInfo) HTMLGeneration.getInstance(InformationFactory.ACCESS_LOG);
        info.setClassOption("do0001");
        Assert.assertFalse(this.accessLog.hashCode() == info.hashCode());
        
    }
    
    @Test
    public void testGetInfoAbout() {
        String do0001 = accessLog.getInfoAbout("do0001");
        Assert.assertTrue(do0001.contains("estrelyaeva"), do0001);
    }
    
    @Test
    public void testGetInfo() {
        this.accessLog = new AccessLogHTMLMaker();
        String info = accessLog.getInfo();
        Assert.assertTrue(info.contains("Set classOption! AccessLogHTMLMaker{"), info);
        accessLog.setClassOption("do0008");
        info = accessLog.getInfo();
        Assert.assertTrue(info.contains("ALLOWED SITES"), info);
        Assert.assertTrue(info.contains("TCP_MISS"), info);
    }
}