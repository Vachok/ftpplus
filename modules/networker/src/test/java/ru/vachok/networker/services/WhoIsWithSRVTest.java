// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.Test;


/**
 @since 15.06.2019 (17:12) */
public class WhoIsWithSRVTest {
    
    
    @Test
    public void testWhoIs() {
        WhoIsWithSRV whoIsWithSRV = new WhoIsWithSRV();
        String whoIsString = whoIsWithSRV.whoIs("ya.ru");
        Assert.assertTrue(whoIsString.contains("This is the RIPE Database query service"), whoIsString);
    }
}