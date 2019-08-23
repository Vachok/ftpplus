package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.info.InformationFactory;


/**
 * @see UserInfo
 */
public class UserInfoTest$$ABS {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(UserInfo.class.getSimpleName(), System.nanoTime());
    
    private InformationFactory userInfo = InformationFactory.getInstance(InformationFactory.USER);
    
    private InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.USER);
    
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
    public void testGetI() {
        
        try {
            informationFactory = ((UserInfo) this.userInfo).getI(null);
        }
        catch (RuntimeException e) {
            Assert.assertNotNull(e);
            Assert.assertTrue(e instanceof InvokeIllegalException, e.getMessage());
        }
        informationFactory = UserInfo.getI(ModelAttributeNames.ADUSER);
        String typeName = informationFactory.getClass().getTypeName();
        Assert.assertEquals(typeName, ADUserResolver.class.getTypeName());
    }
    
    @Test
    public void testGetInfoAbout() {
        String strel = userInfo.getInfoAbout("strel");
        Assert.assertEquals(strel, "10.200.213.103");
    }
    
    @Test
    public void testGetInfo() {
        String info = userInfo.getInfo();
        Assert.assertTrue(info.contains("Identificator"), info);
        userInfo.setClassOption("strel");
        info = userInfo.getInfo();
        Assert.assertEquals(info, "10.200.213.103");
    }
}