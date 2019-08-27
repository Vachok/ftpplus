package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;

import java.util.List;


/**
 * @see ResolveUserInDataBase
 * @since 22.08.2019 (9:14)
 */
public class ResolveUserInDataBaseTest {
    
    
    private ResolveUserInDataBase resolveUserInDataBase = new ResolveUserInDataBase("do0001.eatmeat.ru");
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(ResolveUserInDataBase.class.getSimpleName(), System
            .nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        resolveUserInDataBase.setClassOption("do0001");
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testToString(){
        Assert.assertTrue(resolveUserInDataBase.toString().contains("ResolveUserInDataBase["), resolveUserInDataBase.toString());
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = resolveUserInDataBase.getInfoAbout("do0001.eatmeat.ru");
        Assert.assertFalse(infoAbout.contains("estrelyaeva"), infoAbout);
        testAbstract("do0001.eatmeat.ru");
    }
    
    private void testAbstract(String s) {
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.USER);
        String infoAbout = informationFactory.getInfoAbout(s);
        Assert.assertTrue(infoAbout.contains("estrelyaeva"), infoAbout);
    }
    
    @Test
    public void testGetBadCred() {
        String infoAbout = resolveUserInDataBase.getInfoAbout("j.doe");
        Assert.assertTrue(infoAbout.contains("Unknown user: j.doe"), infoAbout);
    }
    
    @Test
    public void testGetInfo() {
        resolveUserInDataBase.setClassOption("estrelyaeva");
        String info = resolveUserInDataBase.getInfo();
        Assert.assertEquals(info, "10.200.213.103");
    }
    
    @Test
    public void testGetPossibleVariantsOfUser() {
        List<String> do0001 = ((UserInfo) resolveUserInDataBase).getPCLogins("do0001", 10);
        Assert.assertTrue(do0001.size() > 0);
        String listAsStr = new TForms().fromArray(do0001);
        Assert.assertFalse(listAsStr.isEmpty());
        Assert.assertTrue(listAsStr.contains("do0001.eatmeat.ru : "), listAsStr);
    }
}