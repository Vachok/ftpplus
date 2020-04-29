// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.jetbrains.annotations.Contract;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;


/**
 @see InvokeIllegalException
 @since 23.06.2019 (0:28) */
public class InvokeIllegalExceptionTest {


    @Test
    public void getMyThrow() {
        throwMyThrowable();
    }

    @Contract(" -> fail")
    private void throwMyThrowable() {
        throw new TODOException("29.04.2020 (12:07)");
    }
}