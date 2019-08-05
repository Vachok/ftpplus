// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.enums.OtherKnownDevices;


/**
 @see PCUserResolver */
@SuppressWarnings("ALL") public class PCUserResolverTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 3));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    
    @Test
    public void testRun() {
        PCUserResolver pcUserResolver = new PCUserResolver(OtherKnownDevices.DO0213_KUDR);
        pcUserResolver.setInfo();
        String resolverInfoAbout = pcUserResolver.getInfoAbout();
        Assert.assertFalse(resolverInfoAbout.isEmpty(), resolverInfoAbout);
        resolverDO0045();
        
    }
    
    private void resolverDO0045() {
        PCUserResolver pcUserResolver = new PCUserResolver(OtherKnownDevices.DO0045_KIRILL);
        pcUserResolver.setInfo();
    }
}