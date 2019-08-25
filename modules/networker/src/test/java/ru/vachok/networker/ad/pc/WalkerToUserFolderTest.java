package ru.vachok.networker.ad.pc;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.net.scanner.NetListsTest;


/**
 @see WalkerToUserFolder */
public class WalkerToUserFolderTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    private WalkerToUserFolder walkerToUserFolder = new WalkerToUserFolder("do0045");
    
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
    public void testCall() {
        String call = walkerToUserFolder.call();
        Assert.assertTrue(call.contains("\\"), call);
    }
    
    @Test
    public void testTestToString() {
        String toStr = walkerToUserFolder.toString();
        Assert.assertTrue(toStr.contains("WalkerToUserFolder{"), toStr);
    }
}