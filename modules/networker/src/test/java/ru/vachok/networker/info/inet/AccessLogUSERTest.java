// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info.inet;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.scanner.NetListsTest;

import java.io.File;
import java.util.UnknownFormatConversionException;


/**
 @see AccessLogUSER
 @since 17.08.2019 (15:34) */
public class AccessLogUSERTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    private AccessLogUSER informationFactory = new AccessLogUSER();
    
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
    public void testGetInfoAbout() {
        String infoAbout = informationFactory.getInfoAbout("e.v.vinokur");
        Assert.assertTrue(infoAbout.contains("время открытых сессий"), infoAbout);
    }
    
    @Test
    public void testByIP() {
        String infoAbout = informationFactory.getInfoAbout("10.200.213.85");
        Assert.assertTrue(infoAbout.contains("время открытых сессий"), infoAbout);
    }
    
    @Test
    public void testByPcName() {
        String infoAbout = informationFactory.getInfoAbout("do0213");
        Assert.assertTrue(infoAbout.contains("время открытых сессий"));
        infoAbout = informationFactory.getInfoAbout("do0213.eatmeat.ru");
        Assert.assertTrue(infoAbout.contains("время открытых сессий"), infoAbout);
    }
    
    @Test
    public void testBadCredentials() {
        try {
            String infoAbout = informationFactory.getInfoAbout("john doe");
        }
        catch (UnknownFormatConversionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testTestToString() {
        String toStr = informationFactory.toString();
        Assert.assertTrue(toStr.contains("AccessLogUSER{"), toStr);
    }
    
    @Test
    public void testGetInfo() {
        this.informationFactory = new AccessLogUSER();
        String factoryInfo = informationFactory.getInfo();
        Assert.assertTrue(factoryInfo.contains("Identification is not set! "), factoryInfo);
        informationFactory.setOption("do0001");
        factoryInfo = informationFactory.getInfo();
        Assert.assertTrue(factoryInfo.contains("estrelyaeva"), factoryInfo);
    }
    
    @Test
    public void testWriteLog() {
        String writeLogstr = informationFactory.writeLog("test", "test");
        Assert.assertTrue(writeLogstr.contains("AccessLogUSER_"), writeLogstr);
        File fileLog = new File(writeLogstr);
        Assert.assertTrue(fileLog.exists());
        Assert.assertTrue(fileLog.delete());
    }
}