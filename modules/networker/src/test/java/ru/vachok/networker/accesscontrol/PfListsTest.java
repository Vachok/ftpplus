// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol;


import org.testng.Assert;
import org.testng.annotations.Test;


/**
 @see PfLists
 @since 05.08.2019 (20:50) */
public class PfListsTest {
    
    
    @Test
    public void testTestToString() {
        String toStr = new PfLists().toString();
        Assert.assertTrue(toStr.contains("PfLists{"), toStr);
    }
}