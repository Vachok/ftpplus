// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.inet;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;

import java.io.File;
import java.util.Random;
import java.util.UnknownFormatConversionException;
import java.util.concurrent.TimeUnit;


/**
 @see AccessLogUSER
 @since 17.08.2019 (15:34) */
public class AccessLogUSERTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(AccessLogUSERTest.class.getSimpleName(), System.nanoTime());

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
    
    @BeforeMethod
    public void beforeMethod() {
        this.informationFactory = new AccessLogUSER();
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout;
        boolean asTrue = false;
        try {
            infoAbout = informationFactory.getInfoAbout("e.v.vinokur");
            asTrue = !infoAbout.contains("время открытых сессий") || UsefulUtilities.thisPC().toLowerCase().contains("home");
            Assert.assertTrue(infoAbout.contains("время открытых сессий"), infoAbout);
        }
        catch (IndexOutOfBoundsException e) {
            if (asTrue) {
                Assert.assertTrue(asTrue);
            }
        }
    }
    
    @Test
    public void testByIP() {
        String infoAbout = informationFactory.getInfoAbout("10.200.213.85");
        Assert.assertTrue(infoAbout.contains("время открытых сессий"), infoAbout);
    }
    
    @Test
    public void testByPcName() {
        String infoAbout = informationFactory.getInfoAbout("do0214");
        Assert.assertTrue(infoAbout.contains("время открытых сессий"));
        infoAbout = informationFactory.getInfoAbout("do0214.eatmeat.ru");
        Assert.assertTrue(infoAbout.contains("время открытых сессий"), infoAbout);
    }
    
    @Test
    public void testBadCredentials() {
        try {
            String infoAbout = informationFactory.getInfoAbout("john doe");
            Assert.assertTrue(infoAbout.contains(ConstantsFor.UNKNOWN_USER), infoAbout);
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
        String factoryInfo = informationFactory.getInfo();
        Assert.assertTrue(factoryInfo.contains("Identification is not set! "), factoryInfo);
    }
    
    @Test
    public void testWriteLog() {
        String writeLogstr = informationFactory.writeObj("test", "test");
        Assert.assertTrue(writeLogstr.contains("AccessLogUSER_"), writeLogstr);
        File fileLog = new File(writeLogstr);
        Assert.assertTrue(fileLog.exists());
        Assert.assertTrue(fileLog.delete());
    }
    
    @Test
    public void testGetStatistics() {
        StringBuilder stringBuilder = new StringBuilder();
        UserInfo userInfo = UserInfo.getInstance(ModelAttributeNames.ADUSER);
        String userResolved = userInfo.getInfoAbout("do0001");
        stringBuilder.append(userResolved).append(" : ");
        long minutesResponse;
        long mbTraffic;
        float hoursResp;
        minutesResponse = TimeUnit.MILLISECONDS.toMinutes(new Random().nextLong());
        stringBuilder.append(minutesResponse);
        
        hoursResp = (float) minutesResponse / (float) 60;
        stringBuilder.append(" мин. (").append(String.format("%.02f", hoursResp));
        stringBuilder.append(" ч.) время открытых сессий, ");
        
        mbTraffic = new Random().nextLong() / ConstantsFor.MBYTE;
        stringBuilder.append(mbTraffic);
        stringBuilder.append(" мегабайт трафика.");
        System.out.println("stringBuilder = " + stringBuilder.toString());
    }
}