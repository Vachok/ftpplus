package ru.vachok.networker.net;


import org.testng.Assert;
import org.testng.annotations.Test;


@SuppressWarnings("ALL") public class PCUserResolverTest {
    
    
    @Test
    public void testRun() {
        PCUserResolver pcUserResolver = new PCUserResolver("do0213");
        pcUserResolver.setInfo();
        String resolverInfoAbout = pcUserResolver.getInfoAbout();
        Assert.assertFalse(resolverInfoAbout.isEmpty(), resolverInfoAbout);
    }
}