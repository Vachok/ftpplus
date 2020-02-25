package ru.vachok.networker.firebase;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 @see RealTimeChildListenerTest
 @since 23.02.2020 (16:06) */
public class RealTimeChildListener implements ChildEventListener {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, RealTimeChildListener.class.getSimpleName());

    @Override
    public void onChildAdded(@NotNull DataSnapshot snapshot, String previousChildName) {
        if (snapshot.getKey().toLowerCase().contains(ModelAttributeNames.NEWPC)) {
            String value = snapshot.getValue(String.class);
            MessageToUser.getInstance(MessageToUser.EMAIL, this.getClass().getSimpleName())
                .info(getClass().getSimpleName() + " db send: " + sendValueToSQLDatabase(snapshot.getKey(), snapshot.getValue()
                    .toString()), previousChildName, value);

        }
    }

    @Override
    public void onChildChanged(@NotNull DataSnapshot snapshot, String previousChildName) {
        messageToUser.info(String.valueOf(snapshot.getValue()));
        messageToUser.info(previousChildName);
    }

    protected boolean sendValueToSQLDatabase(String key, String value) {
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("velkom.newpc");
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `velkom`.`newpc` (`ip`,`name`) VALUES (?,?)")) {
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, value);
            int i = preparedStatement.executeUpdate();
            return i > 0;
        }
        catch (SQLException e) {
            messageToUser.error("RealTimeChildListener.onChildChanged", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            return false;
        }
    }

    @Override
    public void onChildRemoved(@NotNull DataSnapshot snapshot) {
        System.out.println("snapshot = " + snapshot.getValue());
    }

    @Override
    public void onChildMoved(@NotNull DataSnapshot snapshot, String previousChildName) {
        System.out.println(previousChildName + " = " + snapshot.getValue());
    }

    @Override
    public void onCancelled(@NotNull DatabaseError error) {
        messageToUser.warn(RealTimeChildListener.class.getSimpleName(), error.toException().getMessage(), " see line: 49 ***");
    }
}