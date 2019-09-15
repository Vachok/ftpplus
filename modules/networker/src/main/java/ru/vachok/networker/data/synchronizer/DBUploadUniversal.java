package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @see DBUploadUniversalTest
 @since 15.09.2019 (13:07) */
class DBUploadUniversal extends SyncData {
    
    
    private Collection toUploadCollection;
    
    private String dbToSync;
    
    DBUploadUniversal(Collection toUploadCollection, String dbToSync) {
        this.toUploadCollection = toUploadCollection;
        this.dbToSync = dbToSync;
    }
    
    DBUploadUniversal() {
    }
    
    @Override
    public String syncData() {
        if (toUploadCollection.size() <= 0 && dbToSync.isEmpty()) {
            throw new IllegalArgumentException(this.dbToSync + "\n" + this.toUploadCollection.size());
        }
        
        return MessageFormat.format("{0} rows uploaded to {1}", uploadFileTo(toUploadCollection, dbToSync), dbToSync);
    }
    
    @Override
    public int uploadFileTo(Collection stringsCollection, @NotNull String tableName) {
        DataConnectTo dataConnectTo;
        this.dbToSync = tableName;
        this.toUploadCollection = stringsCollection;
    
        if (tableName.contains(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            dataConnectTo = CONNECT_TO_REGRU;
        }
    
        else {
            dataConnectTo = CONNECT_TO_LOCAL;
        }
        List<String> colList = new ArrayList<>();
        String onlyTableName = dbToSync.split("\\Q.\\E")[1];
        String onlyDBName = dbToSync.split("\\Q.\\E")[0];
        
        try (Connection connection = dataConnectTo.getDefaultConnection(onlyDBName)) {
            try (ResultSet columns = connection.getMetaData().getColumns(onlyDBName, onlyTableName, onlyTableName, "%")) {
                while (columns.next()) {
                    colList.add(columns.getString(4));
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
        }
        uploadReal(colList, dataConnectTo);
        return colList.size();
    }
    
    private void uploadReal(List<String> list, DataConnectTo dataConnectTo) {
        Deque<String> colDeq = new ConcurrentLinkedDeque<>(toUploadCollection);
        String sql;
        try (Connection connection = dataConnectTo.getDefaultConnection(dbToSync.split("\\Q.\\E")[0])) {
            while (!colDeq.isEmpty()) {
                sql = getSQL(colDeq.removeFirst(), list);
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.executeUpdate();
                }
                catch (SQLException e) {
                    messageToUser.warn(sql);
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    private @NotNull String getSQL(String first, @NotNull List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] values = new String[list.size()];
        try {
            JsonObject jsonObject = Json.parse(first).asObject();
            for (int i = 0; i < list.size(); i++) {
                values[i] = jsonObject.getString(list.get(i), list.get(i));
            }
            
        }
        catch (ParseException e) {
            messageToUser.error(e.getMessage() + " see line: 104 ***");
        }
        stringBuilder.append("insert into ").append(dbToSync.split("\\Q.\\E")[1]);
        stringBuilder.append(" ");
        for (String s : list) {
            stringBuilder.append(s).append(", ");
        }
        stringBuilder.append(" values (");
        for (int i = 0; i < list.size(); i++) {
            stringBuilder.append(values[i]).append(", ");
        }
        stringBuilder.replace(stringBuilder.length() - 2, stringBuilder.length(), "");
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
    
    @Override
    public void setOption(Object option) {
        if (option instanceof Collection) {
            this.toUploadCollection = (Collection) option;
        }
        else {
            throw new IllegalArgumentException(option.toString());
        }
    }
    
    @Override
    public void superRun() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.DBUploadUniversal.superRun( void ) at 15.09.2019 - (13:08)");
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", DBUploadUniversal.class.getSimpleName() + "[\n", "\n]")
            .add("toUploadCollection = " + toUploadCollection)
            .add("dbToSync = '" + dbToSync + "'")
            .toString();
    }
    
    @Override
    String getDbToSync() {
        return dbToSync;
    }
    
    @Override
    public void setDbToSync(String dbToSync) {
        this.dbToSync = dbToSync;
    }
    
    @Override
    Map<String, String> makeColumns() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.DBUploadUniversal.makeColumns( Map<String, String> ) at 15.09.2019 - (13:08)");
    }
}