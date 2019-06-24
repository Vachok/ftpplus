// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.testng.Assert;
import org.testng.annotations.Test;


/**
 @see IllegalInvokeEx
 @since 23.06.2019 (0:28) */
public class IllegalInvokeExTest {
    
    
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