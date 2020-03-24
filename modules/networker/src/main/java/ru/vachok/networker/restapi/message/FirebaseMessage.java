package ru.vachok.networker.restapi.message;


import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.data.enums.PropertiesNames;

import java.text.MessageFormat;


/**
 Class ru.vachok.networker.restapi.message.FirebaseMessage
 <p>

 @since 22.02.2020 (16:39) */
public class FirebaseMessage implements MessageToUser {


    FirebaseMessage() {
        new AppComponents().getFirebaseApp();
    }

    @Override
    public void errorAlert(String s, String s1, String s2) {
        FirebaseDatabase.getInstance().getReference(s).setValue(s1 + "\n" + s2, new Compl());
    }

    @Override
    public void info(String s, String s1, String s2) {
        try {
            FirebaseDatabase.getInstance().getReference(s.split(".")[0]).setValue(s1 + "\n" + s2, new Compl());
        }
        catch (ArrayIndexOutOfBoundsException e) {
            FirebaseDatabase.getInstance().getReference("info").setValue(MessageFormat.format("{0}_{1}\n{2}", s, s1, s2), new Compl());
        }
    }

    @Override
    public void infoNoTitles(String s) {
        FirebaseDatabase.getInstance().getReference(s).setValue(s, new Compl());
    }

    @Override
    public void info(String s) {
        try {
            FirebaseDatabase.getInstance().getReference(s).setValue(s, new Compl());
        }
        catch (DatabaseException e) {
            FirebaseDatabase.getInstance().getReference(getClass().getSimpleName()).setValue(s, new Compl());
        }
    }

    @Override
    public void error(String s) {
        FirebaseDatabase.getInstance().getReference(s).setValue(s, new Compl());
    }

    @Override
    public void error(String s, String s1, String s2) {
        try {
            FirebaseDatabase.getInstance().getReference(s.split(".")[0]).setValue(s1 + "\n" + s2, new Compl());
        }
        catch (ArrayIndexOutOfBoundsException e) {
            FirebaseDatabase.getInstance().getReference(PropertiesNames.ERROR).setValue(s1 + "\n" + s2, new Compl());
        }
    }

    @Override
    public void warn(String s, String s1, String s2) {
        FirebaseDatabase.getInstance().getReference(s).setValue(s1 + "\n" + s2, new Compl());
    }

    @Override
    public void warn(String s) {
        FirebaseDatabase.getInstance().getReference(s).setValue(s, new Compl());
    }

    @Override
    public void warning(String s, String s1, String s2) {
        FirebaseDatabase.getInstance().getReference(s).setValue(s1 + "\n" + s2, new Compl());
    }

    @Override
    public void warning(String s) {
        FirebaseDatabase.getInstance().getReference(s).setValue(s, new Compl());
    }

    @Override
    public void setHeaderMsg(String headerMsg) {
        FirebaseDatabase.getInstance().getReference(headerMsg).setValue(headerMsg, new Compl());
    }

    private class Compl implements DatabaseReference.CompletionListener {


        @Override
        public void onComplete(DatabaseError error, DatabaseReference ref) {
            AbstractForms.networkerTrace(error.toException().getStackTrace());
        }
    }
}