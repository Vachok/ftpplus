package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.util.List;


/**
 * @see UserOnlineResolverDBSender
 * @since 22.08.2019 (14:14)
 */
public class UserOnlineResolverDBSenderTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(UserOnlineResolverDBSender.class.getSimpleName(), System
        .nanoTime());
    
    private UserOnlineResolverDBSender UserOnlineResolverDBSender = new UserOnlineResolverDBSender();
    
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
    public void testGetPossibleVariantsOfPC() {
        List<String> variantsOfPC = UserOnlineResolverDBSender.getUserLogins("kudr", 10);
        Assert.assertTrue(variantsOfPC.size() > 0);
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = UserOnlineResolverDBSender.getInfoAbout("kpivo");
        Assert.assertTrue(infoAbout.contains("do0045"), infoAbout);
    }
    
    @Test
    public void testGetInfo() {
        UserOnlineResolverDBSender.setClassOption("strel");
        String info = UserOnlineResolverDBSender.getInfo();
        Assert.assertTrue(info.contains("do0001"), info);
    }
    
    @Test
    public void testGetPossibleVariantsOfUser() {
        List<String> varUsersList = UserOnlineResolverDBSender.getPCLogins("do0212", 4);
        String varUsers = new TForms().fromArray(varUsersList, true);
        System.out.println("varUsers = " + varUsers);
    }
    
    @Test
    public void testTestToString() {
        UserOnlineResolverDBSender.setClassOption("do0213");
        String toStr = UserOnlineResolverDBSender.toString();
        Assert.assertTrue(toStr.contains("ADUserResolver["), toStr);
        Assert.assertTrue(toStr.contains("classOption = do0213,"), toStr);
    }
}