package ru.vachok.networker.info.inet;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
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
    
    private HTMLInfo accessLog = (HTMLInfo) HTMLGeneration.getInstance(HTMLGeneration.ACCESS_LOG);
    
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
        System.out.println("fillWebModelStr = " + fillWebModelStr);
    }
    
    @Test
    public void testFillAttribute() {
        String fillAttributeStr = accessLog.fillAttribute("do0056");
        System.out.println("fillAttributeStr = " + fillAttributeStr);
    }
    
    @Test
    public void testSetClassOption() {
        this.accessLog.setClassOption("do0213");
        String toStr = accessLog.toString();
        Assert.assertTrue(toStr.contains("aboutWhat='do0213'"), toStr);
    }
    
    @Test
    public void testTestEquals() {
        HTMLInfo info = (HTMLInfo) HTMLGeneration.getInstance(HTMLGeneration.ACCESS_LOG);
        info.setClassOption("do0001");
        Assert.assertFalse(this.accessLog.equals(info));
    }
    
    @Test
    public void testTestHashCode() {
        HTMLInfo info = (HTMLInfo) HTMLGeneration.getInstance(HTMLGeneration.ACCESS_LOG);
        info.setClassOption("do0001");
        Assert.assertFalse(this.accessLog.hashCode() == info.hashCode());
        
    }
    
    @Test
    public void testGetInfoAbout() {
        ((InformationFactory) accessLog).getInfoAbout("do0001");
    }
    
    @Test
    public void testGetInfo() {
        throw new InvokeEmptyMethodException("testGetInfo created 27.08.2019 (12:04)");
    }
}