package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.sql.*;
import java.util.Date;
import java.util.List;


/**
 @see UserInfo
 @since 28.08.2019 (20:57) */
public class UserInfoTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(UserInfoTest.class.getSimpleName(), System.nanoTime());
    
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
        String sql = "DELETE FROM `pcuserauto` WHERE `userName` LIKE 'estrelyaevatest'";
        UserInfo.autoResolvedUsersRecord("test", "1561612688516 \\\\do0001.eatmeat.ru\\c$\\Users\\estrelyaevatest " + new Date() + " " + System.currentTimeMillis());
        checkDB(sql);
    }
    
    @Test
    public void testGetInfo() {
        UserInfo pcUserName = UserInfo.getInstance("mdc");
        String info = pcUserName.getInfo();
        String toStrInfo = pcUserName.toString() + "\ninfo = " + info;
        Assert.assertTrue(toStrInfo.contains("10.200.212.72"), toStrInfo);
        Assert.assertTrue(toStrInfo.contains("ResolveUserInDataBase["), toStrInfo);
    
        UserInfo adUser = UserInfo.getInstance(ModelAttributeNames.ADUSER);
        String adInfo = adUser.getInfo();
        String adUserNotSet = adUser.toString() + "\nadInfo = " + adInfo;
        Assert.assertTrue(adUserNotSet.contains(ConstantsFor.UNKNOWN_USER), adUserNotSet);
        adUser.setClassOption("pavlova");
        adInfo = adUser.getInfo();
        Assert.assertTrue(adUser.toString().contains("LocalUserResolver["), adUser.toString());
        Assert.assertEquals(adInfo, "do0214 : s.m.pavlova");
        
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.USER);
        String ifToStr = informationFactory.toString();
        Assert.assertTrue(ifToStr.contains("LocalUserResolver["), ifToStr);
    }
    
    @Test
    public void testManualUsersTableRecord() {
        InformationFactory userInfo = InformationFactory.getInstance(InformationFactory.USER);
        String manDBStr = UserInfo.manualUsersTableRecord("test", "test");
        Assert.assertEquals(manDBStr, "test executeUpdate 0");
    }
    
    private static void checkDB(final String sql) {
        
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_VELKOMINETSTATS)) {
            Thread.sleep(1000);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                int updRows = preparedStatement.executeUpdate();
                Assert.assertTrue(updRows > 0);
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        catch (InterruptedException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testWriteUsersToDBFromSET() {
        String infoPc = PCInfo.getInstance("do0006.eatmeat.ru").getInfo();
        NetKeeper.getPcNamesForSendToDatabase().add("do0213:10.200.213.85 online test<br>");
        Assert.assertTrue(UserInfo.writeUsersToDBFromSET(), AbstractForms.fromArray(NetKeeper.getPcNamesForSendToDatabase()));
        
        checkDB("DELETE FROM `velkompc` WHERE `AddressPP` LIKE '10.200.213.85 online test'");
    }
    
    @Test
    public void testResolvePCUserOverDB() {
        String vashaplovaUserName = UserInfo.resolvePCUserOverDB("vashaplova");
        String expected = "do0125 : vashaplova";
        Assert.assertEquals(vashaplovaUserName.toLowerCase(), expected);
        String vashaplovaDo0125 = UserInfo.resolvePCUserOverDB("do0125");
        Assert.assertEquals(vashaplovaDo0125, expected);
    }
    
    @Test
    public void testGetPCLogins() {
        UserInfo instanceNull = UserInfo.getInstance(null);
        for (String nullPCLogin : instanceNull.getLogins("a123", 1)) {
            System.out.println("nullPCLogin = " + nullPCLogin);
            Assert.assertTrue(nullPCLogin.equalsIgnoreCase("Unknown user: \n ru.vachok.networker.ad.user.UnknownUser"));
        }
        UserInfo instanceDO0045 = UserInfo.getInstance("do0125");
        List<String> logins = instanceDO0045.getLogins("do0125", 1);
        Assert.assertEquals(logins.size(), 1);
        Assert.assertEquals(logins.get(0), "do0125 : vashaplova", new TForms().fromArray(logins));
        
        UserInfo kudrInst = UserInfo.getInstance("kudr");
        for (String kudrInstPCLogin : kudrInst.getLogins("kudr", 1)) {
            System.out.println("kudrInstPCLogin = " + kudrInstPCLogin);
        }
    }
    
    @Test
    public void testGetInfoAbout() {
        UserInfo userInfo = UserInfo.getInstance(InformationFactory.USER);
        String infoInfoAbout = userInfo.getInfoAbout("pavlova");
        Assert.assertTrue(infoInfoAbout.equalsIgnoreCase("do0214 : s.m.pavlova"));
    }
    
    @Test
    public void testToString() {
        UserInfo userInfo = UserInfo.getInstance("kudr");
        String toStr = userInfo.toString();
        Assert.assertTrue(toStr.contains("ResolveUserInDataBase["), toStr);
    }
}