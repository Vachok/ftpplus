package ru.vachok.networker.data.synchronizer;


import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;


/**
 @see TimeOnActualizer
 @since 21.11.2019 (13:49) */
public class TimeOnActualizerTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(TimeOnActualizer.class
            .getSimpleName(), System.nanoTime());
    
    private final String dbToSync = ConstantsFor.DB_VELKOMVELKOMPC;
    
    private TimeOnActualizer timeOnActualizer;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        this.timeOnActualizer = new TimeOnActualizer("do0213.eatmeat.ru");
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testRun() {
        timeOnActualizer.run();
    }
}