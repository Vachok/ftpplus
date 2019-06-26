// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.beans.factory.BeanCreationException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


public class IntoApplicationTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    
    @Test
    public void testGetConfigurableApplicationContext() {
        try {
            IntoApplication.reloadConfigurableApplicationContext();
        }
        catch (BeanCreationException e) {
            System.err.println(e.getBeanName() + " " + e.getResourceDescription() + " " + e.getResourceDescription());
            Assert.assertNotNull(e, e.getMessage());
        }
    }
}