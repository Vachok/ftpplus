package ru.vachok.networker.restapi.message;


import org.springframework.context.ConfigurableApplicationContext;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see MessageToTray
 @since 06.08.2019 (10:15) */
public class MessageToTrayTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser;
    
    private ConfigurableApplicationContext applicationContext;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
        try (ConfigurableApplicationContext applicationContext = IntoApplication.getConfigurableApplicationContext()) {
            this.applicationContext = applicationContext;
            this.messageToUser = MessageToUser.getInstance(MessageToUser.TRAY, this.getClass().getSimpleName());
        }
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
        applicationContext.close();
        Assert.assertFalse(applicationContext.isRunning());
        Assert.assertFalse(applicationContext.isActive());
    }
    
    @Test
    public void testErrorAlert() {
        messageToUser.errorAlert(this.getClass().getSimpleName(), "testErrorAlert", "test");
    }
    
    @Test
    public void testInfo() {
        messageToUser.info(this.getClass().getSimpleName(), "testInfo", "test");
        
    }
    
    @Test
    public void testWarn() {
        messageToUser.warn(this.getClass().getSimpleName(), "testWarn", "test");
    }
    
    @Test
    public void testConfirm() {
        try {
            messageToUser.confirm(this.getClass().getSimpleName(), "testWarn", "test");
        }
        catch (InvokeIllegalException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testTestToString() {
        String toStr = messageToUser.toString();
        
        Assert.assertTrue(toStr.contains("MessageToTray{"), toStr);
        Assert.assertTrue(toStr.contains("MessageLocal{"), toStr);
    }
}