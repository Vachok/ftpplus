package ru.vachok.networker.exe.runnabletasks;


import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;


/**
 @see OnStartTasksLoader
 @since 14.11.2019 (10:19) */
public class OnStartTasksLoaderTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private final MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    AppConfigurationLocal appConfigurationLocal = new OnStartTasksLoader();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testRun() {
        appConfigurationLocal.run();
    }
}