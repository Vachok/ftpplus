// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.Test;


/**
 @see ADUser
 @since 05.08.2019 (20:53) */
public class ADUserTest {
    
    
    @Test
    public void testTestToString() {
        String toStr = new ADUser().toString();
        Assert.assertTrue(toStr.contains("ADUser{"), toStr);
    }
}