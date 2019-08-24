package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.util.List;


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
        List<String> variantsOfPC = adUserResolver.getPossibleVariantsOfPC("kudr", 10);
        Assert.assertTrue(variantsOfPC.size() > 0);
    }
    
    @Test
    public void testGetInfoAbout() {
        String infoAbout = adUserResolver.getInfoAbout("kpivo");
        Assert.assertTrue(infoAbout.contains("do0045"), infoAbout);
    }
    
    @Test
    public void testGetInfo() {
        adUserResolver.setClassOption("strel");
        String info = adUserResolver.getInfo();
        Assert.assertTrue(info.contains("do0001"), info);
    }
}