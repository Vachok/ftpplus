// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.util.concurrent.*;


/**
 @since 16.06.2019 (9:00)
 @see SSHFactory
 */
public class SSHFactoryTest {
    
    
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
    
    
    @Test
    public void testDirectCall() {
        SSHFactory sshFactory = new SSHFactory.Builder("192.168.13.42", "ls", getClass().getSimpleName()).build();
        try {
            Future<String> submit = Executors.newSingleThreadExecutor().submit((Callable<String>) sshFactory);
            try {
                String sshCall = submit.get(ConstantsFor.DELAY, TimeUnit.SECONDS);
                Assert.assertTrue(sshCall.contains("!_passwords.xlsx"), sshCall);
                testConfigureThreadsLogMaker.getPrintStream().println(sshCall);
            }
            catch (InterruptedException | ExecutionException | TimeoutException e) {
        
            }
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    
    }
    
    @Test
    public void testOverABSFactory() {
        AbstractNetworkerFactory networkerFactory = AbstractNetworkerFactory.getInstance(SSHFactory.class.getTypeName());
        Callable<String> sshFactory = networkerFactory.getSSHFactory("192.168.13.42", "sudo ls", this.getClass().getSimpleName());
        try {
            Assert.assertTrue(sshFactory.call().contains(".git<br>"));
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}