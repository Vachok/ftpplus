package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;

import java.io.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;


/**
 @see DBRemoteDownloaderTest
 @since 08.09.2019 (17:36) */
class DBRemoteDownloader extends SyncData {
    
    
    private int lastLocalId;
    
    private String dbToSync;
    
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
    
        this.lastLocalId = getLastLocalID(dbToSync);
    
        stringBuilder.append(getDbToSync()).append(", ");
        stringBuilder.append(CONNECT_TO_REGRU.toString()).append(" dataConnectTo");
        stringBuilder.append(sqlConnect());
        
        return stringBuilder.toString();
    }
    
    @Override
    public void setOption(Object option) {
        this.setDbToSync((String) option);
    }
    
    @Override
    public int uploadFileTo(Collection stringsCollection, String tableName) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.DBRemoteDownloader.uploadFileTo( int ) at 14.09.2019 - (9:10)");
    }
    
    String writeJSON() {
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(dbToSync + FileNames.EXT_TABLE))) {
            bufferedOutputStream.write(sqlConnect().getBytes());
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 107");
        }
        return getDbToSync() + FileNames.EXT_TABLE;
    }
    
    private @NotNull String sqlConnect() {
        String jsonStr = "null";
        try (Connection connection = CONNECT_TO_REGRU.getDataSource().getConnection()) {
            String dbReg = ConstantsFor.DBBASENAME_U0466446_VELKOM + "." + dbToSync.split("\\Q.\\E")[1];
            final String sql = String.format("SELECT * FROM %s WHERE idrec > %s", dbReg, getLastLocalID(dbToSync));
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
            messageToUser.error(MessageFormat.format("{0} see line: 91 *** {1}", e.getMessage(), CONNECT_TO_REGRU.getDataSource().getURL()));
        }
        return jsonStr;
    }
    
    @Override
    String getDbToSync() {
        return dbToSync;
    }
    
    @Override
    public void setDbToSync(String dbToSync) {
        this.dbToSync = dbToSync;
    }
    
    private @NotNull String makeJSONStrings(@NotNull ResultSet resultSet) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        if (resultSet.first())
        while (resultSet.next()) {
            JsonObject jsonObject = new JsonObject();
            if (resultSet.getInt(1) > lastLocalId) {
                int columnsIndexCount = resultSet.getMetaData().getColumnCount() + 1;
                for (int i = 1; i < columnsIndexCount; i++) {
                    jsonObject.add(resultSet.getMetaData().getColumnName(i), resultSet.getString(i));
                }
            }
            stringBuilder.append(jsonObject).append("\n");
        }
        return stringBuilder.toString();
    }
    
    @Override
    Map<String, String> makeColumns() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.DBRemoteDownloader.makeCollumns( Map<String, String> ) at 14.09.2019 - (11:51)");
    }
    
    @Override
    public void superRun() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.DBRemoteDownloader.superRun( void ) at 15.09.2019 - (10:19)");
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