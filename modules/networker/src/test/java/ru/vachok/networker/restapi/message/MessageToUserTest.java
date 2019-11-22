package ru.vachok.networker.restapi.message;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see MessageToUser
 @since 21.08.2019 (11:03) */
public class MessageToUserTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(MessageToUser.class.getSimpleName(), System
            .nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testGetInstance() {
        MessageToUser testInst = MessageToUser.getInstance(null, "TEST");
        try {
            testInst.infoTimer(10, "TEST");
        }
        catch (InvokeIllegalException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        testInst = MessageToUser.getInstance(null, "null");
        testInst.info(testInst.toString());
        testInst = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, "null");
        testInst.info(testInst.toString());
    }
}