// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.jetbrains.annotations.Contract;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;


/**
 @see InvokeIllegalException
 @since 23.06.2019 (0:28) */
public class InvokeIllegalExceptionTest {

    @Test
    public void getMyThrow() {
        try {
            throwMyThrowable();
        }
        catch (InvokeIllegalException e) {
            Assert.assertNotNull(e);
            Assert.assertTrue(e.getMessage().contains("THIS IS ME 23.06.2019 (0:34)"), e.getMessage());
        }
    }

    @Contract(" -> fail")
    private void throwMyThrowable() {
        throw new InvokeIllegalException("THIS IS ME 23.06.2019 (0:34)");
    }
}