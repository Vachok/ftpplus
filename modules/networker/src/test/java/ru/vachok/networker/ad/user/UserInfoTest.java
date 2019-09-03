package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.NetKeeper;
import ru.vachok.networker.componentsrepo.data.NetListsTest;
import ru.vachok.networker.componentsrepo.data.enums.ModelAttributeNames;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.info.InformationFactory;

import java.util.List;


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
        UserInfo.autoResolvedUsersRecord("test", "test");
        throw new TODOException("03.09.2019 (22:10)");
    }
    
    @Test
    public void testManualUsersTableRecord() {
        InformationFactory userInfo = InformationFactory.getInstance(InformationFactory.USER);
        String manDBStr = UserInfo.manualUsersTableRecord("test", "test");
        Assert.assertEquals(manDBStr, "test executeUpdate 0");
    }
    
    @Test
    public void testGetInfo() {
        UserInfo kuhar = UserInfo.getInstance("vinok");
        String info = kuhar.getInfo();
        String toStrInfo = kuhar.toString() + "\ninfo = " + info;
        Assert.assertTrue(toStrInfo.contains("10.200.217.79"), toStrInfo);
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
        UserInfo.writeUsersToDBFromSET();
        throw new TODOException("03.09.2019 (22:10)");
    }
    
    @Test
    public void testResolvePCUserOverDB() {
        String kudr = UserInfo.resolvePCUserOverDB("kudr");
        Assert.assertEquals(kudr.toLowerCase(), "do0213");
        String do0213 = UserInfo.resolvePCUserOverDB("do0213");
        Assert.assertEquals(do0213, "ikudryashov");
    }
    
    @Test
    public void testGetPCLogins() {
        UserInfo instanceNull = UserInfo.getInstance(null);
        for (String nullPCLogin : instanceNull.getLogins("a123", 1)) {
            System.out.println("nullPCLogin = " + nullPCLogin);
            Assert.assertTrue(nullPCLogin.equalsIgnoreCase("Unknown user: \n ru.vachok.networker.ad.user.UnknownUser"));
        }
        UserInfo instanceDO0045 = UserInfo.getInstance("do0045");
        List<String> logins = instanceDO0045.getLogins("do0045", 1);
        Assert.assertEquals(logins.size(), 1);
        Assert.assertEquals(logins.get(0), "do0045 : kpivovarov", new TForms().fromArray(logins));
        
        UserInfo kudrInst = UserInfo.getInstance("kudr");
        for (String kudrInstPCLogin : kudrInst.getLogins("kudr", 1)) {
            System.out.println("kudrInstPCLogin = " + kudrInstPCLogin);
        }
    }
    
    @Test
    public void testGetInfoAbout() {
        throw new InvokeEmptyMethodException("GetInfoAbout created 04.09.2019 at 0:03");
    }
    
    @Test
    public void testSetClassOption() {
        throw new InvokeEmptyMethodException("SetClassOption created 04.09.2019 at 0:03");
    }
    
    @Test
    public void testTestToString() {
        throw new InvokeEmptyMethodException("TestToString created 04.09.2019 at 0:03");
    }
}