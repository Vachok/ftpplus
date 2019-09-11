package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.data.enums.FileNames;

import java.io.*;
import java.sql.*;


/**
 @see DBRemoteDownloaderTest
 @since 08.09.2019 (17:36) */
class DBRemoteDownloader extends SyncData {
    
    
    private int lastLocalId;
    
    DBRemoteDownloader(int lastLocalID) {
        this.lastLocalId = lastLocalID;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBRemoteDownloader{");
        sb.append("lastId=").append(lastLocalId);
        sb.append(", idColName='").append(getIdColName()).append('\'');
        sb.append(", dbToSync='").append(getDbToSync()).append('\'');
        sb.append(", get from db='").append(CONNECT_TO_REGRU.getDataSource().getURL()).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String syncData() {
        StringBuilder stringBuilder = new StringBuilder();
    
        this.lastLocalId = getLastLocalID();
    
        stringBuilder.append(getDbToSync()).append(", ");
        stringBuilder.append(CONNECT_TO_REGRU.toString()).append(" dataConnectTo");
        stringBuilder.append(sqlConnect());
        
        return stringBuilder.toString();
    }
    
    @Override
    public void setOption(Object option) {
        this.setDbToSync((String) option);
    }
    
    private @NotNull String sqlConnect() {
        String jsonStr = "null";
        try (Connection connection = CONNECT_TO_REGRU.getDataSource().getConnection()) {
            final String sql = String.format("SELECT * FROM %s WHERE idrec > %s", getDbToSync(), getLastLocalID());
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    jsonStr = makeJSONStrings(resultSet);
                }
                catch (ParseException ignore) {
                    //11.09.2019 (15:10)
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 69");
        }
        return jsonStr;
    }
    
    private @NotNull String makeJSONStrings(@NotNull ResultSet resultSet) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\n \"");
        stringBuilder.append(getDbToSync());
        stringBuilder.append("\" :[\n");
        if (resultSet.first()) {
            stringBuilder.append("{");
        }
        while (resultSet.next()) {
            if (resultSet.getInt(1) > lastLocalId) {
                for (int i = 1; i < resultSet.getMetaData().getColumnCount(); i++) {
                    stringBuilder.append("\"");
                    stringBuilder.append(resultSet.getMetaData().getColumnName(i));
                    stringBuilder.append("\":\"");
                    stringBuilder.append(resultSet.getString(i));
                    stringBuilder.append("\",");
                }
                stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");
                stringBuilder.append("},\n{");
            }
        }
        if (resultSet.last()) {
            int length = stringBuilder.length();
            stringBuilder.replace((length - 4), length, "}\n]\n}");
        }
        return stringBuilder.toString();
    }
    
    String writeJSON() {
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(getDbToSync() + FileNames.EXT_TABLE))) {
            bufferedOutputStream.write(sqlConnect().getBytes());
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 107");
        }
        return getDbToSync() + FileNames.EXT_TABLE;
    }
    
    @Contract(pure = true)
    private @NotNull JsonObject makeJsonObject(@NotNull ResultSet resultSet) throws SQLException {
        JsonObject jsonObject = new JsonObject();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        for (int i = 1; i < columnCount; i++) {
            jsonObject.set(resultSetMetaData.getColumnName(i), resultSet.getString(i));
        }
        return jsonObject;
    }
}