package ru.vachok.networker.firebase;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.restapi.message.MessageToUser;


/**
 Class ru.vachok.networker.firebase.NewPCListener
 <p>

 @since 23.02.2020 (16:06) */
public class NewPCListener implements ChildEventListener {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.EMAIL, NewPCListener.class.getSimpleName());

    public void listenNewPC() {
        FirebaseDatabase.getInstance().getReference().addChildEventListener(this);
    }

    @Override
    public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
        if (snapshot.getKey().toLowerCase().contains(ModelAttributeNames.NEWPC)) {
            String value = snapshot.getValue(String.class);
            messageToUser.info(getClass().getSimpleName(), previousChildName, value);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
        System.out.println(previousChildName + " = " + snapshot.getValue());
    }

    @Override
    public void onChildRemoved(DataSnapshot snapshot) {
        System.out.println("snapshot = " + snapshot.getValue());
    }

    @Override
    public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
        System.out.println(previousChildName + " = " + snapshot.getValue());
    }

    @Override
    public void onCancelled(DatabaseError error) {
        messageToUser.warn(NewPCListener.class.getSimpleName(), error.toException().getMessage(), " see line: 49 ***");
    }
}