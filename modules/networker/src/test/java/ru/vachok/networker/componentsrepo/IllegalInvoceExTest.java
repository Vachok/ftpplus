// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.testng.Assert;
import org.testng.annotations.Test;


/**
 @see IllegalInvoceEx
 @since 23.06.2019 (0:28) */
public class IllegalInvoceExTest {
    
    
    @Test
    public void getMyThrow() {
        try {
            throwMyThrowable();
        }
        catch (IllegalInvoceEx e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(e.getMessage(), "THIS IS ME 23.06.2019 (0:34)");
        }
    }
    
    private void throwMyThrowable() {
        throw new IllegalInvoceEx("THIS IS ME 23.06.2019 (0:34)");
    }
}