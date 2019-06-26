package ru.vachok.networker.accesscontrol;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import static org.testng.Assert.assertTrue;


/**
 @see MatrixSRV
 @since 21.06.2019 (12:40) */
@SuppressWarnings("ALL") public class MatrixSRVTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.afterClass();
    }
    
    
    /**
     @see MatrixSRV#searchAccessPrincipals(String)
     */
    @Test
    public void testSearchAccessPrincipals() {
        MatrixSRV matrixSRV = new MatrixSRV();
        String accessPrincipals = matrixSRV.searchAccessPrincipals("адми");
        assertTrue(accessPrincipals.contains("администр"));
    }
}