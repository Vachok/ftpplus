// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;


/**
 @see UsersKeeper
 @since 28.07.2019 (17:53) */
public class UsersKeeperTest {
    
    
    @Test
    public void testGet24hrsTempInetList() {
        Map<String, String> inetTMP24Hrs = UsersKeeper.get24hrsTempInetList();
        Assert.assertNotNull(inetTMP24Hrs);
    }
    
    @Test
    public void testGetInetUniqMap() {
        Map<String, String> inetUniq = UsersKeeper.getInetUniqMap();
        Assert.assertNotNull(inetUniq);
    }
}