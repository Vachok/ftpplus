package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;


/**
 @see BackupDBTest
 @since 17.10.2019 (21:16) */
class BackupDB extends SyncData {
    
    
    private String dbToSync;
    
    private Object option;
    
    public BackupDB() {
        this.dbToSync = ConstantsFor.DB_VELKOMVELKOMPC;
        this.option = 0;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", BackupDB.class.getSimpleName() + "[\n", "\n]")
            .add("dbToSync = '" + dbToSync + "'")
            .add("option = " + option)
            .toString();
    }
    
    @Override
    String getDbToSync() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.BackupDB.getDbToSync( String ) at 17.10.2019 - (21:16)");
    }
    
    @Override
    public void setDbToSync(String dbToSync) {
        this.dbToSync = dbToSync;
    }
    
    @Override
    public void setOption(Object option) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.BackupDB.setOption( void ) at 17.10.2019 - (21:16)");
    }
    
    @Override
    public String syncData() {
        throw new TODOException("17.10.2019 (21:41)");
    }
    
    @Override
    public void superRun() {
        int inetStatID = getLastRemoteID(dbToSync);
        int localVWID = getLastLocalID(dbToSync);
        messageToUser.info(dbToSync, MessageFormat.format("Remote ID = {0} ; local ID = {1}", inetStatID, localVWID), MessageFormat
            .format("Diff = {0}", inetStatID - localVWID));
        List<JsonObject> mapForUpload = trySync(DataConnectTo.getInstance(DataConnectTo.DEFAULT_I), String
            .format("select * from %s where idRec > %d;", dbToSync, localVWID));
        uploadCollection(mapForUpload, dbToSync);
    }
    
    private @NotNull List<JsonObject> trySync(@NotNull DataConnectTo dataConnectTo, @NotNull String sql) {
        List<JsonObject> retList = new ArrayList<>();
        try (Connection connection = dataConnectTo.getDefaultConnection(dbToSync)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                if (sql.contains("select")) {
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            JsonObject putObject = downloadData(resultSet);
                            retList.add(putObject);
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error("BackupDB.trySync", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace()));
        }
        return retList;
    }
    
    @Override
    public int uploadCollection(@NotNull Collection stringsCollection, String tableName) {
        MysqlDataSource source = DataConnectTo.getInstance(DataConnectTo.TESTING).getDataSource();
        try (Connection connection = source.getConnection()) {
            int updated = 0;
            for (Object o : stringsCollection) {
                JsonObject jsonObject = Json.parse(String.valueOf(o)).asObject();
                List<String> colNames = jsonObject.names();
                StringBuilder sqlB = new StringBuilder();
                sqlB.append(ConstantsFor.SQL_INSERTINTO).append(tableName).append(" (");
                colNames.forEach(colName->sqlB.append(colName).append(", "));
                sqlB.replace(sqlB.length() - 2, sqlB.length(), "");
                sqlB.append(ConstantsFor.VALUES);
                for (int i = 0; i < colNames.size(); i++) {
                    sqlB.append(jsonObject.get(colNames.get(i))).append(", ");
                }
                sqlB.replace(sqlB.length() - 2, sqlB.length(), "");
                sqlB.append(");");
                try (PreparedStatement preparedStatement = connection.prepareStatement(sqlB.toString())) {
                    updated += preparedStatement.executeUpdate();
                }
            }
            return updated;
        }
        catch (SQLException e) {
            messageToUser.error("BackupDB.uploadMap", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace()));
            return -666;
        }
    }
    
    private @NotNull JsonObject downloadData(@NotNull ResultSet resultSet) throws SQLException {
        JsonObject jsonObject = new JsonObject();
        for (int i = 1; i < resultSet.getMetaData().getColumnCount(); i++) {
            String column = resultSet.getMetaData().getColumnName(i);
            jsonObject.add(column, resultSet.getString(column));
        }
        return jsonObject;
    }
    
    @Override
    Map<String, String> makeColumns() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.BackupDB.makeColumns( Map<String, String> ) at 17.10.2019 - (21:16)");
    }
}