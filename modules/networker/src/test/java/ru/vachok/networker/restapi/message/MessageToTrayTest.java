package ru.vachok.networker.restapi.message;


import org.springframework.context.ConfigurableApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
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
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        testConfigureThreadsLogMaker.before();
    }

    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
        applicationContext.close();
        Assert.assertFalse(applicationContext.isRunning());
        Assert.assertFalse(applicationContext.isActive());
    }

    @Test
    @Ignore
    public void testErrorAlert() {
        messageToUser.errorAlert(this.getClass().getSimpleName(), "testErrorAlert", "test");
    }

    @Test
    @Ignore
    public void testInfo() {
        messageToUser.info(this.getClass().getSimpleName(), "testInfo", "test");

    }

    @Test
    @Ignore
    public void testWarn() {
        messageToUser.warn(this.getClass().getSimpleName(), "testWarn", "test");
    }

    @Test
    @Ignore
    public void testConfirm() {
        messageToUser.confirm(this.getClass().getSimpleName(), "testWarn", "test");
    }

    @Test
    @Ignore
    public void testTestToString() {
        String toStr = messageToUser.toString();

        Assert.assertTrue(toStr.contains("MessageToTray{"), toStr);
        Assert.assertTrue(toStr.contains("MessageLocal{"), toStr);
    }
}