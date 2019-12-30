package ru.vachok.networker.net;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see UniqPCInformator
 @since 28.12.2019 (18:25) */
public class UniqPCInformatorTest {


    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(UniqPCInformatorTest.class.getSimpleName(), System
        .nanoTime());

    private UniqPCInformator uniqPCInformator;

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
    public void testGetInfo() {
        this.uniqPCInformator = new UniqPCInformator();
        String info = uniqPCInformator.getInfo();
        Assert.assertTrue(info.contains("do0213.eatmeat.ru : 10.200.213.85"), info);
    }

    @Test
    public void testGetInfoAbout() {
        this.uniqPCInformator = new UniqPCInformator();
        String infoA = uniqPCInformator.getInfoAbout("");
        Assert.assertTrue(infoA.contains("\"ip\":\"10.200.213.85\",\"pcname\":\"do0213.eatmeat.ru\""), infoA);
    }

    @Test
    public void testTestToString() {
        this.uniqPCInformator = new UniqPCInformator();
        String toStr = uniqPCInformator.toString();
        Assert.assertTrue(toStr.contains("UniqPCInformator["), toStr);
    }
}