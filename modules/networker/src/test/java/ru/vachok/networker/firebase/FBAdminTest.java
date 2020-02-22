package ru.vachok.networker.firebase;


import com.google.firebase.database.*;
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
            String s = fbAdmin.initSDK();
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

    @Test
    public void funcTest() {
        new FBAdmin().initSDK();
        DatabaseReference networker = FirebaseDatabase.getInstance().getReference("networker");
        networker.setValue("test", (error, ref)->ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Assert.assertNotNull(snapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Assert.assertNull(error.toException(), error.toException().getMessage() + "\n" + AbstractForms.fromArray(error.toException()));
            }
        }));
    }
}