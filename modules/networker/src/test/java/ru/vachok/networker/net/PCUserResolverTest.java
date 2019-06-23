// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.testng.Assert;
import org.testng.annotations.Test;


/**
 @see PCUserResolver */
@SuppressWarnings("ALL") public class PCUserResolverTest {
    
    
    @Test
    public void testRun() {
        PCUserResolver pcUserResolver = new PCUserResolver("do0213.eatmeat.ru");
        pcUserResolver.setInfo();
        String resolverInfoAbout = pcUserResolver.getInfoAbout();
        Assert.assertFalse(resolverInfoAbout.isEmpty(), resolverInfoAbout);
        resolverDO0004();
        
    }
    
    private void resolverDO0004() {
        PCUserResolver pcUserResolver = new PCUserResolver("do0004.eatmeat.ru");
        pcUserResolver.setInfo();
    }
}