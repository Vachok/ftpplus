package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.*;
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
    
    private ResolveUserInDataBase resolveUserInDataBase = new ResolveUserInDataBase("do0001.eatmeat.ru");
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 4));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testToString() {
        Assert.assertTrue(resolveUserInDataBase.toString().contains("ResolveUserInDataBase["), resolveUserInDataBase.toString());
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = resolveUserInDataBase.getInfoAbout("do0045.eatmeat.ru");
        Assert.assertTrue(infoAbout.contains("kpivovarov"), infoAbout);
        testAbstract();
    }
    
    private void testAbstract() {
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.USER);
        String infoAbout = informationFactory.getInfoAbout("do0045.eatmeat.ru");
        Assert.assertTrue(infoAbout.contains("kpivovarov"), infoAbout);
    }
    
    @Test
    public void testGetBadCred() {
        String infoAbout = resolveUserInDataBase.getInfoAbout("j.doe");
        Assert.assertTrue(infoAbout.contains("Unknown user: j.doe"), infoAbout);
    }
    
    @Test
    public void testGetInfo() {
        this.resolveUserInDataBase.setClassOption("homya");
        String info = resolveUserInDataBase.getInfo();
        Assert.assertEquals(info, "10.200.217.83");
    }
    
    @Test
    public void testGetPossibleVariantsOfUser() {
        List<String> do0001 = ((UserInfo) resolveUserInDataBase).getPCLogins("do0001", 10);
        Assert.assertTrue(do0001.size() > 0);
        String listAsStr = new TForms().fromArray(do0001);
        Assert.assertFalse(listAsStr.isEmpty());
        Assert.assertTrue(listAsStr.contains("do0001 : "), listAsStr);
    }
}