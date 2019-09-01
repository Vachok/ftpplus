package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.data.enums.ModelAttributeNames;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.net.scanner.NetListsTest;


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
    public void testWriteToDB() {
        String toDBStr = UserInfo.writeUsersToDBFromSET();
        Assert.assertTrue(toDBStr.contains("(insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow))"));
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
        UserInfo kudr = UserInfo.getInstance("kuhar");
        String info = kudr.getInfo();
        String toStrInfo = kudr.toString() + "\ninfo = " + info;
        Assert.assertTrue(toStrInfo.contains("10.200.213.56"), toStrInfo);
        Assert.assertTrue(toStrInfo.contains("ResolveUserInDataBase["), toStrInfo);
    
        UserInfo adUser = UserInfo.getInstance(ModelAttributeNames.ADUSER);
        String adInfo = adUser.getInfo();
        String adUserNotSet = adUser.toString() + "\nadInfo = " + adInfo;
        Assert.assertTrue(adUserNotSet.contains("Unknown user"), adUserNotSet);
        adUser.setOption("kudr");
        adInfo = adUser.getInfo();
        Assert.assertTrue(adUser.toString().contains("LocalUserResolver["), adUser.toString());
        Assert.assertEquals(adInfo, "do0213");
        
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.USER);
        String ifToStr = informationFactory.toString();
        Assert.assertTrue(ifToStr.contains("LocalUserResolver["), ifToStr);
    }
}