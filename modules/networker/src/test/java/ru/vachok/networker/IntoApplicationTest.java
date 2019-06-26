// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.springframework.beans.factory.BeanCreationException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class IntoApplicationTest {
    
    
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