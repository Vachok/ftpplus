package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;

import java.util.List;


/**
 * @see ResolveUserInDataBase
 * @since 22.08.2019 (9:14)
 */
public class ResolveUserInDataBaseTest {
    
    
    private InformationFactory resolveUserInDataBase = InformationFactory.getInstance(InformationFactory.USER);
    
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
    public void testGetUsage() {
        try {
            List<String> do0001 = ((UserInfo) resolveUserInDataBase).getPossibleVariantsOfPC("do0001", 10);
            
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = resolveUserInDataBase.getInfoAbout("do0001.eatmeat.ru");
        Assert.assertEquals(infoAbout, "10.200.213.103");
    }
    
    @Test
    public void testGetBadCred() {
        String infoAbout = resolveUserInDataBase.getInfoAbout("j.doe");
        Assert.assertEquals(infoAbout, "j.doe is not valid user");
    }
    
    @Test
    public void testGetInfo() {
        String info = resolveUserInDataBase.getInfo();
        Assert.assertEquals(info, "10.200.213.103");
    }
    
    @Test
    public void testGetPossibleVariantsOfPC() {
        List<String> kudrList = ((UserInfo) resolveUserInDataBase).getPossibleVariantsOfPC("kudr", 10);
        Assert.assertTrue(kudrList.size() > 0);
        String listAsStr = new TForms().fromArray(kudrList);
        Assert.assertFalse(listAsStr.isEmpty());
        Assert.assertTrue(listAsStr.contains(".eatmeat.ru : \\"), listAsStr);
    }
    
    @Test
    public void testGetPossibleVariantsOfUser() {
        List<String> do0001 = ((UserInfo) resolveUserInDataBase).getPossibleVariantsOfUser("do0001", 10);
        Assert.assertTrue(do0001.size() > 0);
        String listAsStr = new TForms().fromArray(do0001);
        Assert.assertFalse(listAsStr.isEmpty());
        Assert.assertTrue(listAsStr.contains(".eatmeat.ru : \\"), listAsStr);
    }
}