package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.data.enums.ModelAttributeNames;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;


/**
 * @see UserInfo
 */
public class UserInfoTest$$ABS {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(UserInfo.class.getSimpleName(), System.nanoTime());
    
    private InformationFactory userInfo = InformationFactory.getInstance(InformationFactory.USER);
    
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
        InformationFactory informationFactory = UserInfo.getInstance(ModelAttributeNames.ADUSER);
        String typeName = informationFactory.getClass().getTypeName();
        Assert.assertEquals(typeName, LocalUserResolver.class.getTypeName());
    }
    
    @Test
    public void testTestToString() {
        String toStr = userInfo.toString();
        Assert.assertTrue(toStr.contains("ResolveUserInDataBase["), toStr);
    }
}