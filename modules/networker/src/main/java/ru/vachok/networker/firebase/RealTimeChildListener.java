package ru.vachok.networker.firebase;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 Class ru.vachok.networker.firebase.NewPCListener
 <p>

 @since 23.02.2020 (16:06) */
public class RealTimeChildListener implements ChildEventListener {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.EMAIL, RealTimeChildListener.class.getSimpleName());

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
        if (snapshot.getKey().equals("RemoteConfigInformer")) {
            sendValueToSQLDatabase(String.valueOf(snapshot.getValue()));
        }
    }

    protected boolean sendValueToSQLDatabase(String value) {
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("firebase.visits");
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `firebase`.`visits` (`deviceid`) VALUES (?)")) {
            preparedStatement.setString(1, String.valueOf(value));
            int i = preparedStatement.executeUpdate();
            return i > 0;
        }
        catch (SQLException e) {
            messageToUser.error("RealTimeChildListener.onChildChanged", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            return false;
        }
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
        messageToUser.warn(RealTimeChildListener.class.getSimpleName(), error.toException().getMessage(), " see line: 49 ***");
    }
}