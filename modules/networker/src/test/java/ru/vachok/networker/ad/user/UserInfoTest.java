package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
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
        userInfo = UserInfo.getInstance(UserInfo.ADUSER);
        toStr = userInfo.toString();
        Assert.assertTrue(toStr.contains("LocalUserResolver["), toStr);
        userInfo = UserInfo.getInstance(null);
        toStr = userInfo.toString();
        Assert.assertTrue(toStr.contains("UnknownUser["), toStr);
    }
    
    @Test
    public void testWriteToDB() {
        String toDBStr = UserInfo.writeToDB();
        Assert.assertTrue(toDBStr.contains("(insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow))"));
    }
    
    @Test
    public void testAutoResolvedUsersRecord() {
        UserInfo.autoResolvedUsersRecord("test", "test");
    }
    
    @Test
    public void testManualUsersTableRecord() {
        String manDBStr = UserInfo.manualUsersTableRecord("test", "test");
        Assert.assertEquals(manDBStr, "test executeUpdate 0");
    }
}