package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.util.List;


/**
 * @see LocalUserResolver
 * @since 22.08.2019 (14:14)
 */
public class LocalUserResolverTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(LocalUserResolver.class.getSimpleName(), System
        .nanoTime());
    
    private LocalUserResolver userInfo = new LocalUserResolver();
    
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
        List<String> variantsOfPC = userInfo.getPCLogins("do0045", 10);
        Assert.assertTrue(variantsOfPC.size() > 0);
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = userInfo.getInfoAbout("do0008");
        Assert.assertTrue(infoAbout.contains("homya"), infoAbout);
    }
    
    @Test
    public void testGetInfo() {
        userInfo.setOption("do0001");
        String info = userInfo.getInfo();
        Assert.assertTrue(info.contains("strel"), info);
    }
    
    @Test
    public void testGetPossibleVariantsOfUser() {
        List<String> varUsersList = userInfo.getPCLogins("do0212", 20);
        String varUsers = new TForms().fromArray(varUsersList, true);
        System.out.println("varUsers = " + varUsers);
    }
    
    @Test
    public void testTestToString() {
        userInfo.setOption("do0213");
        String toStr = userInfo.toString();
        Assert.assertTrue(toStr.contains("LocalUserResolverDBSender["), toStr);
        Assert.assertTrue(toStr.contains("classOption = do0213,"), toStr);
    }
    
    @Test
    public void testGetPCLogins() {
        String pcName = "do0045";
        Assert.assertTrue(userInfo.getPCLogins(pcName, 1).size() == 1);
        Assert.assertTrue(userInfo.getPCLogins(pcName, 2).size() == 2);
    }
    
    @Test
    public void testBadCredentials() {
        String pcName = "do0";
        String info = userInfo.getInfo();
        Assert.assertTrue(info.contains("Unknown user:"), info);
        Assert.assertTrue(info.contains("LocalUserResolver["), info);
        
        info = userInfo.getInfoAbout(pcName);
        System.out.println("info = " + info);
    }
}