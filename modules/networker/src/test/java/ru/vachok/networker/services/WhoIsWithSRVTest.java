// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TestConfigure;


/**
 @since 15.06.2019 (17:12) */
public class WhoIsWithSRVTest {
    
    
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
    
    
    @Test(timeOut = 6000)
    public void testWhoIs() {
        WhoIsWithSRV whoIsWithSRV = new WhoIsWithSRV();
        String whoIsString = whoIsWithSRV.whoIs("ya.ru");
        Assert.assertTrue(whoIsString.contains("This is the RIPE Database query service"), whoIsString);
    }
}