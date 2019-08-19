// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;


/**
 @see IntoApplication */
public class IntoApplicationTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        IntoApplication.closeContext();
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
        IntoApplication.closeContext();
    }
    
    @Test(enabled = false)
    public void testGetConfigurableApplicationContext() {
        try {
            Assert.assertFalse(IntoApplication.reloadConfigurableApplicationContext().isEmpty());
        }
        catch (BeanCreationException e) {
            Assert.assertNull(e, e.getBeanName() + " " + e.getResourceDescription() + " " + e.getResourceDescription());
        }
    }
    
    @Test
    public void testReloadConfigurableApplicationContext() {
        IntoApplication.main(new String[]{"-test, -notray"});
        String reloadAppContext = IntoApplication.reloadConfigurableApplicationContext();
        Assert.assertEquals(reloadAppContext, "application");
        try (ConfigurableApplicationContext context = IntoApplication.getConfigurableApplicationContext()) {
            context.close();
            IntoApplication.closeContext();
            Assert.assertFalse(context.isActive());
            Assert.assertFalse(context.isRunning());
        }
        catch (RuntimeException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testMain() {
        try {
            IntoApplication.main(new String[]{"test"});
        }
        catch (RejectedExecutionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        try (ConfigurableApplicationContext context = IntoApplication.getConfigurableApplicationContext()) {
            context.close();
            IntoApplication.closeContext();
            Assert.assertFalse(context.isActive());
            Assert.assertFalse(context.isRunning());
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    
    @Test
    public void testBeforeSt() {
        IntoApplication.beforeSt();
        Assert.assertTrue(new File("system").lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10)));
    }
}