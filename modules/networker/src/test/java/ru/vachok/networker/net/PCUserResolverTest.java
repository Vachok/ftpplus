// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TestConfigure;
import ru.vachok.networker.net.enums.OtherKnownDevices;


/**
 @see PCUserResolver */
@SuppressWarnings("ALL") public class PCUserResolverTest {
    
    
    private final TestConfigure testConfigure = new TestConfigure(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigure.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigure.afterClass();
    }
    
    
    @Test
    public void testRun() {
        PCUserResolver pcUserResolver = new PCUserResolver(OtherKnownDevices.DO0213_KUDR);
        pcUserResolver.setInfo();
        String resolverInfoAbout = pcUserResolver.getInfoAbout();
        Assert.assertFalse(resolverInfoAbout.isEmpty(), resolverInfoAbout);
        resolverDO0004();
        
    }
    
    private void resolverDO0004() {
        PCUserResolver pcUserResolver = new PCUserResolver(OtherKnownDevices.DO0045_KIRILL);
        pcUserResolver.setInfo();
    }
}