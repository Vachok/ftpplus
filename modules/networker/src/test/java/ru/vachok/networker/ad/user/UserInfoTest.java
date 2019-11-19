package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.props.InitProperties;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


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
        Assert.assertTrue(adInfo.contains("pavlova : Unknown user "), adInfo);
        
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.USER);
        String ifToStr = informationFactory.toString();
        Assert.assertTrue(ifToStr.contains("LocalUserResolver["), ifToStr);
    
    }
    
    @Test
    public void testUniqueUsersTableRecord() {
        InformationFactory userInfo = InformationFactory.getInstance(InformationFactory.USER);
        String manDBStr = UserInfo.uniqueUsersTableRecord("pc1", "user1");
        Assert.assertEquals(manDBStr, "user1 already exists in database velkom.pcuser on pc1");
    }
    
    private @NotNull String getCreate() {
        return FileSystemWorker.readRawFile(getClass().getResource("/create.pcuser.txt").getFile());
    }
    
    @Test
    public void testRenewCounter() {
        boolean isOffline = new Random().nextBoolean();
        String pcName = "test";
        boolean wasOff = wasOffline(pcName);
        String sql;
        String sqlOn = String.format("UPDATE `pcuser` SET `lastOnLine`='%s', `On`= `On`+1, `Total`= `On`+`Off` WHERE `pcName` like ?", Timestamp
            .valueOf(LocalDateTime.now()));
        String sqlOff = "UPDATE `pcuser` SET `Off`= `Off`+1, `Total`= `On`+`Off` WHERE `pcName` like ?";
        if (isOffline) {
            sql = sqlOff;
        }
        else {
            sql = sqlOn;
        
            if (wasOff) {
                sql = String
                    .format("UPDATE `pcuser` SET `lastOnLine`='%s', `timeon`='%s', `On`= `On`+1, `Total`= `On`+`Off` WHERE `pcName` like ?", Timestamp
                        .valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));
            }
        
        }
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.H2DB)
            .getDefaultConnection(ConstantsFor.DB_VELKOMPCUSER.replace(DataConnectTo.DBNAME_VELKOM_POINT, ""))) {
            createTable();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, String.format("%s%%", pcName));
                preparedStatement.executeUpdate();
            }
        }
        catch (SQLException | RuntimeException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testGetPCLogins() {
        UserInfo instanceNull = UserInfo.getInstance(null);
        for (String nullPCLogin : instanceNull.getLogins("a123", 1)) {
            boolean contPC = nullPCLogin.split(" : ")[0].contains("a123");
            Assert.assertTrue(contPC, nullPCLogin.split(" : ")[0]);
            boolean contName = Stream.of("i.k.romanovskii", "e.v.vinokur", "a.f.zarickii").anyMatch(s->nullPCLogin.split(" : ")[1].contains(s));
            Assert.assertTrue(contName, nullPCLogin.split(" : ")[1]);
        }
        UserInfo instanceDO0045 = UserInfo.getInstance("do0125");
        List<String> logins = instanceDO0045.getLogins("do0125", 1);
        Assert.assertEquals(logins.size(), 1);
        Assert.assertTrue(logins.get(0).contains("do0125"), AbstractForms.fromArray(logins));
        Assert.assertTrue(logins.get(0).contains("vashaplova"), AbstractForms.fromArray(logins));
    
        UserInfo kudrInst = UserInfo.getInstance("ashapl");
        for (String kudrInstPCLogin : kudrInst.getLogins("ashapl", 1)) {
            boolean do0213Expect = kudrInstPCLogin.contains("do0125") || kudrInstPCLogin.contains("no0029");
            Assert.assertTrue(do0213Expect, kudrInstPCLogin);
        }
    }
    
    @Test
    public void testRenewOffCounter() {
        UserInfo.renewOffCounter("test", new Random().nextBoolean());
    }
    
    @Test
    public void testGetLogins() {
        List<String> loginsDO213 = UserInfo.getInstance("do0213").getLogins("do0213", 20);
        String loginsStr = AbstractForms.fromArray(loginsDO213);
        Assert.assertTrue(loginsStr.contains("ikudryashov"));
    }
    
    @Test
    public void testGetInfoAbout() {
        UserInfo userInfo = UserInfo.getInstance(InformationFactory.USER);
        String infoInfoAbout = userInfo.getInfoAbout("pavlova");
        String[] arr = infoInfoAbout.split(" : ");
        Assert.assertTrue(arr[0].contains("do0214"));
        Assert.assertEquals(arr[1], "s.m.pavlova");
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
        String expected = "do0125";
        Assert.assertTrue(vashaplovaUserName.contains(expected), vashaplovaUserName);
        String vashaplovaDo0125 = UserInfo.resolvePCUserOverDB("do0125");
        Assert.assertTrue(vashaplovaDo0125.contains(expected), vashaplovaDo0125);
    }
    
    private void createTable() {
        String dbName = "pcuser";
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.H2DB).getDefaultConnection(dbName)) {
            Assert.assertEquals(connection.getMetaData().getURL(), "jdbc:h2:mem:pcuser");
            try (PreparedStatement create = connection.prepareStatement(getCreate())) {
                System.out.println("create = " + create.toString());
                create.executeUpdate();
            }
        }
        catch (SQLException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    private boolean wasOffline(String pcName) {
        final String sql = String.format("SELECT lastonline FROM pcuser WHERE pcname LIKE '%s%%'", pcName);
        boolean retBool = false;
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I)
                .getDefaultConnection(ConstantsFor.DB_VELKOMPCUSER)) {
            createTable();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Timestamp timestamp = resultSet.getTimestamp("lastonline");
                        System.out.println("timestamp = " + timestamp.toString());
                        retBool = timestamp.getTime() < InitProperties.getUserPref().getLong(PropertiesNames.LASTSCAN, System.currentTimeMillis()) - TimeUnit.MINUTES
                                .toMillis(ConstantsFor.DELAY * 3);
                    }
                }
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        return retBool;
    }
    
    @Test
    public void testToString() {
        UserInfo userInfo = UserInfo.getInstance("kudr");
        String toStr = userInfo.toString();
        Assert.assertTrue(toStr.contains("ResolveUserInDataBase["), toStr);
    }
}