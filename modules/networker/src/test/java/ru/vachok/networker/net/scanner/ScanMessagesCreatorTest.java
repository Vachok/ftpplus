package ru.vachok.networker.net.scanner;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see ScanMessagesCreator
 @since 16.11.2019 (11:33) */
public class ScanMessagesCreatorTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(ScanMessagesCreatorTest.class.getSimpleName(), System
        .nanoTime());

    private ScanMessagesCreator scanMessagesCreator = new ScanMessagesCreator();

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
    public void testGetMsg() {
        String msg = scanMessagesCreator.getMsg();
        Assert.assertTrue(msg.contains("seconds elapsed"), msg);
        Assert.assertTrue(msg.contains("from"), msg);
        Assert.assertTrue(msg.contains("to"), msg);
    }

    @Test
    public void testTestToString() {
        String s = scanMessagesCreator.toString();
        Assert.assertEquals(s, "ScanMessagesCreator[\n" +
            "numOfTrains = 6\n" +
            "]");
    }

    @Test
    public void testGetTitle() {
        String title = scanMessagesCreator.getTitle(1);
        Assert.assertTrue(title.contains("1/"));
    }

    @Test
    public void testFillUserPCForWEBModel() {
        String s = scanMessagesCreator.fillUserPCForWEBModel();
        Assert.assertTrue(s.contains("<p>"), s);
    }
}