// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;


public class IntoApplicationTest {
    
    
    @Test()
    public void testGetConfigurableApplicationContext() {
        ConfigurableApplicationContext ctx = IntoApplication.reloadConfigurableApplicationContext();
        Assert.assertNotNull(ctx);
        ctx.close();
        new IntoApplication().setConfigurableApplicationContext(SpringApplication.run(IntoApplication.class));
    }
}