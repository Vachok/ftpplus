package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;

import java.util.List;


/**
 @see ResolveUserInDataBase
 @since 22.08.2019 (9:14) */
public class ResolveUserInDataBaseTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(ResolveUserInDataBase.class.getSimpleName(), System
        .nanoTime());
    
    private ResolveUserInDataBase resolveUserInDataBase;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 4));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @BeforeMethod
    public void setResolveUserInDataBase() {
        this.resolveUserInDataBase = new ResolveUserInDataBase();
    }
    
    @Test
    public void testToString() {
        Assert.assertTrue(resolveUserInDataBase.toString().contains("ResolveUserInDataBase["), resolveUserInDataBase.toString());
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = resolveUserInDataBase.getInfoAbout("no0015.eatmeat.ru");
        boolean strOk = infoAbout.contains("msc") || infoAbout.contains("d.yu.podbuckii");
        Assert.assertTrue(strOk, infoAbout);
        testAbstract();
    }
    
    @Test
    public void testGetLogins() {
        List<String> loginsPC = resolveUserInDataBase.getLogins("do0133", 1);
        String logStr = new TForms().fromArray(loginsPC);
        Assert.assertTrue(logStr.contains("do0133"), logStr);
        Assert.assertTrue(logStr.contains("buh_sch2"), logStr);
        List<String> kudrLogins = resolveUserInDataBase.getLogins("buh_sch2", 1);
        String logStrKudr = new TForms().fromArray(kudrLogins);
        Assert.assertEquals(logStr, logStr);
        
    }
    
    private void testAbstract() {
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.USER);
        String infoAbout = informationFactory.getInfoAbout("a323.eatmeat.ru");
        Assert.assertTrue(infoAbout.contains(": tbabicheva"), infoAbout);
    }
    
    @Test
    public void testGetBadCred() {
        String infoAbout = resolveUserInDataBase.getInfoAbout("j.doe");
        Assert.assertTrue(infoAbout.contains("Unknown"), infoAbout);
        Assert.assertTrue(infoAbout.contains("j.doe"), infoAbout);
    }
    
    @Test
    public void testGetInfo() {
        this.resolveUserInDataBase.setClassOption("homya");
        String info = resolveUserInDataBase.getInfo();
        Assert.assertEquals(info, "10.200.217.83");
    }
    
    @Test
    public void testGetPossibleVariantsOfUser() {
        List<String> do0001 = ((UserInfo) resolveUserInDataBase).getLogins("do0001", 10);
        Assert.assertTrue(do0001.size() > 0);
        String listAsStr = AbstractForms.fromArray(do0001);
        Assert.assertFalse(listAsStr.isEmpty());
        Assert.assertTrue(listAsStr.contains("do0001"), listAsStr);
    }
}