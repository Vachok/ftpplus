// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TestConfigure;


/**
 @see IllegalInvokeEx
 @since 23.06.2019 (0:28) */
public class IllegalInvokeExTest {
    
    
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
    public void getMyThrow() {
        try {
            throwMyThrowable();
        }
        catch (IllegalInvokeEx e) {
            Assert.assertNotNull(e);
            Assert.assertTrue(e.getMessage().contains("THIS IS ME 23.06.2019 (0:34)"), e.getMessage());
        }
    }
    
    private void throwMyThrowable() {
        throw new IllegalInvokeEx("THIS IS ME 23.06.2019 (0:34)");
    }
}