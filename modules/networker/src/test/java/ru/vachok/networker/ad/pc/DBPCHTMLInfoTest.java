// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.data.NetListsTest;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see DBPCHTMLInfo
 @since 18.08.2019 (23:41) */
public class DBPCHTMLInfoTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    private DBPCHTMLInfo dbpchtmlInfo = new DBPCHTMLInfo();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        dbpchtmlInfo.setClassOption("do0213");
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testTestToString() {
        String do0213 = dbpchtmlInfo.toString();
        Assert.assertTrue(do0213.contains("DBPCInfo{"), do0213);
    }
    
    @Test
    public void testFillWebModel() {
        String fillWebModel = dbpchtmlInfo.fillWebModel();
        Assert.assertTrue(fillWebModel.contains("<a href="), fillWebModel);
        Assert.assertTrue(fillWebModel.contains("Last online"), fillWebModel);
    }
    
    @Test
    public void testFillAttribute() {
        String fillAttributeStr = dbpchtmlInfo.fillAttribute("do0008");
        Assert.assertTrue(fillAttributeStr.contains("Online = "), fillAttributeStr);
        Assert.assertTrue(fillAttributeStr.contains("Offline = "), fillAttributeStr);
        Assert.assertTrue(fillAttributeStr.contains("<br>"), fillAttributeStr);
    }
    
    @Test
    public void testSetClassOption() {
        dbpchtmlInfo.setClassOption("do0213");
        Assert.assertTrue(dbpchtmlInfo.toString().contains("DBPCInfo{pcName='do0213'"), dbpchtmlInfo.toString());
    }
    
    @Test
    public void testLastOnline() {
        String firstOnline = dbpchtmlInfo.firstOnline();
        Assert.assertTrue(firstOnline.contains("2019-06-03"), firstOnline);
    }
    
    @Test
    public void testCountOnOff() {
        String countOnOff = dbpchtmlInfo.fillAttribute("do0213");
        Assert.assertTrue(countOnOff.contains("Online"), countOnOff);
        Assert.assertTrue(countOnOff.contains("Offline"), countOnOff);
        Assert.assertTrue(countOnOff.contains("TOTAL"), countOnOff);
    }
    
    @Test
    public void testFirstOnline() {
        String firstOnline = dbpchtmlInfo.firstOnline();
        Assert.assertTrue(firstOnline.contains("Since:"));
    }
}