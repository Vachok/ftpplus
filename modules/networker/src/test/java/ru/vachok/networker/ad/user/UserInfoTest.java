package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.data.NetKeeper;
import ru.vachok.networker.componentsrepo.data.NetListsTest;
import ru.vachok.networker.componentsrepo.data.enums.ModelAttributeNames;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;


/**
 @see UserInfo
 @since 28.08.2019 (20:57) */
public class UserInfoTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
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
    public void testGetInstance() {
        UserInfo userInfo = UserInfo.getInstance("");
        String toStr = userInfo.toString();
        Assert.assertTrue(toStr.contains("ResolveUserInDataBase["), toStr);
        userInfo = UserInfo.getInstance(ModelAttributeNames.ADUSER);
        toStr = userInfo.toString();
        Assert.assertTrue(toStr.contains("LocalUserResolver["), toStr);
        userInfo = UserInfo.getInstance(null);
        toStr = userInfo.toString();
        Assert.assertTrue(toStr.contains("UnknownUser["), toStr);
    }
    
    @Test
    public void testAutoResolvedUsersRecord() {
        String autoRec = UserInfo.autoResolvedUsersRecord("test", "test");
        System.out.println("autoRec = " + autoRec);
    }
    
    @Test
    public void testManualUsersTableRecord() {
        InformationFactory userInfo = InformationFactory.getInstance(InformationFactory.USER);
        String manDBStr = UserInfo.manualUsersTableRecord("test", "test");
        Assert.assertEquals(manDBStr, "test executeUpdate 0");
    }
    
    @Test
    public void testGetInfo() {
        UserInfo kuhar = UserInfo.getInstance("kuhar");
        String info = kuhar.getInfo();
        String toStrInfo = kuhar.toString() + "\ninfo = " + info;
        Assert.assertTrue(toStrInfo.contains("10.200.213.56"), toStrInfo);
        Assert.assertTrue(toStrInfo.contains("ResolveUserInDataBase["), toStrInfo);
    
        UserInfo adUser = UserInfo.getInstance(ModelAttributeNames.ADUSER);
        String adInfo = adUser.getInfo();
        String adUserNotSet = adUser.toString() + "\nadInfo = " + adInfo;
        Assert.assertTrue(adUserNotSet.contains("Unknown user"), adUserNotSet);
        adUser.setClassOption("kudr");
        adInfo = adUser.getInfo();
        Assert.assertTrue(adUser.toString().contains("LocalUserResolver["), adUser.toString());
        Assert.assertEquals(adInfo, "do0213");
        
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.USER);
        String ifToStr = informationFactory.toString();
        Assert.assertTrue(ifToStr.contains("LocalUserResolver["), ifToStr);
    }
    
    @Test
    public void testWriteUsersToDBFromSET() {
        NetKeeper.getPcNamesForSendToDatabase().add("do0213:10.200.213.85 online false<br>");
        String writeBigDB = UserInfo.writeUsersToDBFromSET();
        Assert.assertTrue(writeBigDB.contains("Update = 1"), writeBigDB);
    }
    
    @Test
    public void testResolvePCByUserNameOverDB() {
        String kudr = UserInfo.resolvePCUserOverDB("kudr");
        Assert.assertEquals(kudr.toLowerCase(), "do0213");
        String do0213 = UserInfo.resolvePCUserOverDB("do0213");
        Assert.assertEquals(do0213, "ikudryashov");
    }
}