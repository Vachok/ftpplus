// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.context.ConfigurableApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;


public class IntoApplicationTest {
    
    
    @Test(enabled = false)
    public void testGetConfigurableApplicationContext() {
        ConfigurableApplicationContext ctx = IntoApplication.getConfigurableApplicationContext();
        Assert.assertNotNull(ctx);
    }
}