package ru.vachok.networker.ad.user;


import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 * @see ResolveUserInDataBase
 * @since 22.08.2019 (9:14)
 */
public class ResolveUserInDataBaseTest {
    
    private ResolveUserInDataBase resolveUserInDataBase = new ResolveUserInDataBase();
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(ResolveUserInDataBase.class.getSimpleName(), System
            .nanoTime());
    
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
    public void testToString(){
    
    }
}