package ru.vachok.networker.restapi.database;


import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;


/**
 Class ru.vachok.networker.restapi.database.FirebaseRealTime
 <p>

 @since 22.02.2020 (21:56) */
public class FirebaseRealTime implements DataConnectTo {


    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, getClass().getSimpleName());

    @Override
    public MysqlDataSource getDataSource() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        FirebaseDatabase.getInstance().getReference(tableName).setValue(stringsCollection, new Compl());
        return 1;
    }

    @Override
    public boolean dropTable(String dbPointTable) {
        FirebaseDatabase.getInstance().getReference(dbPointTable).removeValue(new Compl());
        return true;
    }

    @Override
    public int createTable(String dbPointTable, List<String> additionalColumns) {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    private class Compl implements DatabaseReference.CompletionListener {


        @Override
        public void onComplete(DatabaseError error, DatabaseReference ref) {
            messageToUser.error("Compl.onComplete", error.toException().getMessage(), AbstractForms.networkerTrace(error.toException().getStackTrace()));
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(",\n", FirebaseRealTime.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}