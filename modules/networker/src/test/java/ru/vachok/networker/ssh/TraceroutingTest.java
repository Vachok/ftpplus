// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ssh;


import org.springframework.core.task.TaskRejectedException;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @since 09.06.2019 (21:32) */
public class TraceroutingTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
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
    public void testCall() {
        Tracerouting tracerouting = new Tracerouting();
        try {
            String call = tracerouting.call();
            Assert.assertNotNull(call);
            Assert.assertTrue(call.contains("<br><a href=\"/makeok\">"));
        }
        catch (TaskRejectedException | InterruptedException ignore) {
            //26.06.2019 (1:49)
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
}