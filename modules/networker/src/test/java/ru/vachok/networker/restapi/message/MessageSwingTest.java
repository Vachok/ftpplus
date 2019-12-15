package ru.vachok.networker.restapi.message;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see MessageSwing
 @since 31.08.2019 (11:03) */
public class MessageSwingTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(MessageSwingTest.class.getSimpleName(), System.nanoTime());

    private ru.vachok.messenger.@NotNull MessageSwing messageSwing = AppComponents.getMessageSwing(this.getClass().getSimpleName());

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
    public void testInfoTimer() {
        messageSwing.infoTimer(10, "test10");
    }

    @Test
    public void testTestToString() {
        String toStr = messageSwing.toString();
        Assert.assertTrue(toStr.contains("MessageSwing{"), toStr);
    }

    @Test
    public void testOverFace() {
        MessageToUser.getInstance(MessageToUser.SWING, this.getClass().getSimpleName()).infoTimer(4, "test");
    }
}