// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.server;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.util.concurrent.*;


/**
 @see TelnetServer
 @since 05.08.2019 (23:07) */
public class TelnetServerTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private TelnetServer telnetServer = new TelnetServer(11111);
    
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
    public void testRunSocket() {
        Runnable runnable = ()->telnetServer.runSocket();
        Future<?> submit = Executors.newSingleThreadExecutor().submit(runnable);
        try {
            Assert.assertNull(submit.get(5, TimeUnit.SECONDS));
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testTestToString() {
        String toStr = telnetServer.toString();
        Assert.assertTrue(toStr.contains("TelnetServer{"), toStr);
        Assert.assertTrue(toStr.contains("11111"), toStr);
    }
}