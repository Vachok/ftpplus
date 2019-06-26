// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.sshactions;


import org.springframework.core.task.TaskRejectedException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TestConfigure;


/**
 @since 09.06.2019 (21:32) */
public class TraceroutingTest {
    
    
    private final TestConfigure testConfigure = new TestConfigure(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigure.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigure.afterClass();
    }
    
    
    @Test
    public void testCall() {
        Tracerouting tracerouting = new Tracerouting();
        try {
            String call = tracerouting.call();
            Assert.assertNotNull(call);
            Assert.assertTrue(call.contains("<br><a href=\"/makeok\">"));
        }
        catch (TaskRejectedException ignore) {
            //26.06.2019 (1:49)
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
}