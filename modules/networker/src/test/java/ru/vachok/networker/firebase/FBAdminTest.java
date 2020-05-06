package ru.vachok.networker.firebase;


import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.restapi.props.InitProperties;


/**
 @see FBAdmin */
public class FBAdminTest {


    private final FBAdmin fbAdmin = new FBAdmin();

    @Test
    public void testInitSDK() {
        try {
            String appName = FirebaseApp.getInstance().getName();
            Assert.assertEquals(appName, "[DEFAULT]");
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

    @Test
    public void appVerInFB() {
        try {
            String filePath = InitProperties.getInstance(InitProperties.FIREBASE).getProps().getProperty("file");
            System.out.println("filePath = " + filePath);
        }
        catch (RuntimeException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }

    }
}