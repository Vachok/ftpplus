// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.scanner.NetListsTest;


/**
 @see DBPCInfo
 @since 18.08.2019 (23:41) */
public class DBPCInfoTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    private DBPCInfo dbpcInfo = new DBPCInfo("do0213");
    
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
    public void testTestToString() {
        String do0213 = dbpcInfo.toString();
        Assert.assertTrue(do0213.contains("DBPCInfo{"), do0213);
    }
    
    @Test
    public void testGetPCbyUser() {
        String pcByUser = dbpcInfo.resolvePCNameByUserName("kudr");
        Assert.assertEquals(pcByUser, "do0213.eatmeat.ru : ikudryashov\n");
    }
    
    @Test
    public void testDefaultInformation() {
        this.dbpcInfo.setPcName("do0045");
        String dbpcInfoInfo = dbpcInfo.defaultInformation();
        Assert.assertTrue(dbpcInfoInfo.contains("kpivovarov"), dbpcInfoInfo + "\n" + new TForms().fromArray(Thread.currentThread().getStackTrace()));
    
    }
    
    @Test
    public void test$$PACKAGE$$LastOnlineHTML() {
        String lastOnline = dbpcInfo.lastOnline();
        String countOnOff = dbpcInfo.countOnOff();
        String byUserName = dbpcInfo.resolvePCNameByUserName("kudr");
    
        Assert.assertTrue(lastOnline.contains("ikudryashov"), lastOnline);
        Assert.assertTrue(countOnOff.contains("offline times and"), countOnOff);
        Assert.assertTrue(byUserName.contains("do0213.eatmeat.ru"), byUserName);
    }
}