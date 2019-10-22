package ru.vachok.networker.ad;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import javax.security.auth.kerberos.KerberosPrincipal;


public class KerberosAuthTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 4));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    @Ignore
    public void testAuth() {
        KerberosPrincipal kerberosPrincipal = new KerberosPrincipal("ikudryashov");
        String kerberosStr = kerberosPrincipal.toString();
        Assert.assertEquals(kerberosPrincipal.toString(), "ikudryashov@EATMEAT.RU");
    }
    
}
