package ru.vachok.networker.firebase;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;


/**
 @see FBAdmin */
public class FBAdminTest {
    
    
    private FBAdmin fbAdmin = new FBAdmin();
    
    @Test
    public void testInitSDK() {
        try {
            fbAdmin.initSDK();
        }
        catch (Exception e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }
    
    @Test
    public void testTestToString() {
        String s = fbAdmin.toString();
        Assert.assertTrue(s.contains("\"className\":\"FBAdmin\""));
    }
}