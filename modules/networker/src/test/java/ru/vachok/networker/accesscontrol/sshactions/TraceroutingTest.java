// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.sshactions;


import org.testng.Assert;
import org.testng.annotations.Test;


/**
 @since 09.06.2019 (21:32) */
public class TraceroutingTest {
    
    
    @Test
    public void testCall() {
        Tracerouting tracerouting = new Tracerouting();
        try {
            String call = tracerouting.call();
            System.out.println("call = " + call);
            Assert.assertNotNull(call);
            Assert.assertTrue(call.contains("<br><a href=\"/makeok\">"));
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
}