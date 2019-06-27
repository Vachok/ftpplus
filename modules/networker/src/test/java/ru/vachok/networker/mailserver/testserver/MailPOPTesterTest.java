package ru.vachok.networker.mailserver.testserver;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @since 27.06.2019 (10:42) */
public class MailPOPTesterTest {
    
    
    private TestConfigure testConfigure = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
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
    public void testTestInput() {
        throw new InvokeEmptyMethodException(getClass().getTypeName(), "testTestInput");
    }
    
    @Test
    public void testTestOutput() {
        throw new InvokeEmptyMethodException(getClass().getTypeName(), "testTestOutput");
    }
    
    @Test
    public void testTestComplex() {
        throw new InvokeEmptyMethodException(getClass().getTypeName(), "testTestComplex");
    }
}