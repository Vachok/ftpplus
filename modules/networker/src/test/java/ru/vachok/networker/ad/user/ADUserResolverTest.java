package ru.vachok.networker.ad.user;


import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.util.Set;


/**
 * @see ADUserResolver
 * @since 22.08.2019 (14:14)
 */
public class ADUserResolverTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(ADUserResolver.class.getSimpleName(), System.nanoTime());
    
    private ADUserResolver adUserResolver = new ADUserResolver();
    
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
    public void testGetPossibleVariantsOfPC() {
        Set<String> variantsOfPC = adUserResolver.getPossibleVariantsOfPC("kudr", 10);
        System.out.println("variantsOfPC = " + variantsOfPC.size());
    }
    
    @Test
    public void testGetInfoAbout() {
        throw new InvokeEmptyMethodException("testGetInfoAbout created 22.08.2019 (14:13)");
    }
    
    @Test
    public void testSetClassOption() {
        throw new InvokeEmptyMethodException("testSetClassOption created 22.08.2019 (14:13)");
    }
    
    @Test
    public void testGetInfo() {
        throw new InvokeEmptyMethodException("testGetInfo created 22.08.2019 (14:13)");
    }
}