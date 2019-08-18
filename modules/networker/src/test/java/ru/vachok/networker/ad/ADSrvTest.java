// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.OtherKnownDevices;

import java.util.ArrayList;
import java.util.List;


/**
 @see ADSrv
 @since 15.06.2019 (17:17) */
@SuppressWarnings("ALL") public class ADSrvTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private ADSrv adSrv = new ADSrv();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testGetDetails() {
        ADSrv adSrv = new ADSrv();
        String do0213String = adSrv.getInternetUsage(OtherKnownDevices.DO0213_KUDR);
        Assert.assertTrue(do0213String.contains("ikudryashov"), do0213String);
    }
    
    @Test
    public void testShowADPCList() {
        ADSrv adSrv = new ADSrv();
        ADComputer adComputer = new ADComputer();
        adComputer.setEnabled(String.valueOf(true));
        adComputer.setName(OtherKnownDevices.DO0213_KUDR);
        adComputer.setDnsHostName(OtherKnownDevices.DO0213_KUDR);
        adComputer.setSamAccountName("DO0213");
        List<ADComputer> adComputers = new ArrayList<>();
        adComputers.add(adComputer);
        String pcSString = adSrv.showADPCList(adComputers, false);
        Assert.assertTrue(pcSString.contains("name='do0213"), pcSString);
    }
    
    @Test
    public void testRun() {
        ADSrv adSrv = new ADSrv();
        adSrv.run();
        Assert.assertTrue(adSrv instanceof ADSrv);
    }
    
    @Test
    public void testGetAdComputer() {
        ADComputer adComputer = adSrv.getAdComputer();
        Assert.assertNull(adComputer);
    }
    
    @Test
    public void testGetUserInputRaw() {
        String inputRaw = adSrv.getUserInputRaw();
        Assert.assertNull(inputRaw);
    }
    
    @Test
    public void testSetUserInputRaw() {
        adSrv.setUserInputRaw("test");
        Assert.assertEquals(adSrv.getUserInputRaw(), "test");
    }
    
    @Test
    public void testGetAdUser() {
        ADUser user = adSrv.getAdUser();
        Assert.assertNull(user);
    }
    
    @Test
    public void testTestToString() {
        String toStr = adSrv.toString();
        Assert.assertTrue(toStr.contains("ADSrv{"));
    }
}