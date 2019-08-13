package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see AnketaKonfeta
 @since 13.08.2019 (9:27) */
public class AnketaKonfetaTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(AnketaKonfeta.class.getSimpleName(), System
        .nanoTime());
    
    private AnketaKonfeta anketaKonfeta = new AnketaKonfeta();
    
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
    public void testSendKonfeta() {
        anketaKonfeta.sendKonfeta("test");
    }
    
    @Test
    public void testTestToString() {
        String toStr = anketaKonfeta.toString();
        Assert.assertTrue(toStr.contains("AnketaKonfeta{"), toStr);
    }
}