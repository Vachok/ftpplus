// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.testng.Assert;
import org.testng.annotations.Test;


@SuppressWarnings("ALL") public class PCUserResolverTest {
    
    
    @Test
    public void testRun() {
        PCUserResolver pcUserResolver = new PCUserResolver("do0213");
        pcUserResolver.setInfo(); //todo 22.06.2019 (22:57) Проверка на доступность хоста.
        String resolverInfoAbout = pcUserResolver.getInfoAbout();
        Assert.assertFalse(resolverInfoAbout.isEmpty(), resolverInfoAbout);
    }
}