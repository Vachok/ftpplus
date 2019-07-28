package ru.vachok.networker.accesscontrol.common;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @since 17.06.2019 (10:51)
 @see ArchivesAutoCleaner
 */
@SuppressWarnings("ALL") public class ArchivesAutoCleanerTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    
    @Test(enabled = false)
    public void testRun() {
        ArchivesAutoCleaner autoCleaner = new ArchivesAutoCleaner();
        autoCleaner.toString();
        autoCleaner.run();
    }
}