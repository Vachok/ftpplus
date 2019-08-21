package ru.vachok.networker.restapi.message;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.MessageToUser;


/**
 @see MessageLocal
 @since 13.08.2019 (9:17) */
public class MessageLocalTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(MessageLocal.class
        .getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
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
    public void testIgExc() {
        try {
            ((MessageLocal) messageToUser).igExc(new InvokeEmptyMethodException("test"));
        }
        catch (InvokeEmptyMethodException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testInfoTimer() {
        try {
            messageToUser.infoTimer(10, "test");
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testInfo() {
        messageToUser.info("test");
        messageToUser.info("test", "test", "test");
    }
    
    @Test
    public void testError() {
        messageToUser.errorAlert("test", "test", "test");
        messageToUser.error("test");
        messageToUser.error("test", "test", "test");
    }
    
    @Test
    public void testWarn() {
        messageToUser.warn("test");
        messageToUser.warning("test");
        
        messageToUser.warning("test", "test", "test");
        messageToUser.warn("test", "test", "test");
    }
    
    @Test
    public void testLoggerFine() {
        ((MessageLocal) messageToUser).loggerFine("test");
    }
    
    @Test
    public void testTestToString() {
        String toString = messageToUser.toString();
        Assert.assertTrue(toString.contains("MessageLocal{"), toString);
    }
}