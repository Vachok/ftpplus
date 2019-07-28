// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.beans.factory.BeanCreationException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.util.concurrent.RejectedExecutionException;


/**
 @see IntoApplication */
public class IntoApplicationTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
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
    public void runMainApp() {
        IntoApplication intoApplication = new IntoApplication();
        try {
            intoApplication.start(new String[]{"test"});
        }
        catch (RejectedExecutionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            Assert.assertTrue(e.getMessage().contains("KEY"));
        }
        
    }
}