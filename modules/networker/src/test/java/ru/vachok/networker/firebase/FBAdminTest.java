package ru.vachok.networker.firebase;


import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;

import java.io.File;
import java.nio.file.Paths;


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
            File[] rootFiles = Paths.get(".").toFile().listFiles();
            for (File file : rootFiles) {
                if (file.getName().contains("jar")) {
                    System.out.println("file = " + file);
                }
            }
        }
        catch (RuntimeException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }

    }
}