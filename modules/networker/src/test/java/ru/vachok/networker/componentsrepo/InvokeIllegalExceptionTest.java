// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see InvokeIllegalException
 @since 23.06.2019 (0:28) */
public class InvokeIllegalExceptionTest {
    
    
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
    
    private void throwMyThrowable() {
        throw new InvokeIllegalException("THIS IS ME 23.06.2019 (0:34)");
    }
}