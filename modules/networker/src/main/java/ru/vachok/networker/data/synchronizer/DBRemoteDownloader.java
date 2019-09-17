package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;

import java.io.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;


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
        sb.append("lastLocalId=").append(lastLocalId);
        sb.append(", dbToSync='").append(dbToSync).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String syncData() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(writeJSON());
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
    
    private String writeJSON() {
        String fileName = dbToSync + FileNames.EXT_TABLE;
        List<String> jsonFromDB = sqlConnect();
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fileName))) {
            bufferedOutputStream.write(new TForms().fromArray(jsonFromDB).getBytes());
            return fileName;
        }
        catch (IOException e) {
            return e.getMessage() + " see line: 72";
        }
    }
    
    private @NotNull List<String> sqlConnect() {
        List<String> jsonStrings = new ArrayList<>();
        try (Connection connection = CONNECT_TO_REGRU.getDataSource().getConnection()) {
            String dbReg = ConstantsFor.DBBASENAME_U0466446_VELKOM + "." + dbToSync.split("\\Q.\\E")[1];
            final String sql = String.format("SELECT * FROM %s WHERE idrec > %s", dbReg, lastLocalId);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    jsonStrings.addAll(makeJSONStrings(resultSet));
                }
                catch (ParseException ignore) {
                    //11.09.2019 (15:10)
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("{0} see line: 91 *** {1}", e.getMessage(), CONNECT_TO_REGRU.getDataSource().getURL()));
        }
        return jsonStrings;
    }
    
    @Override
    String getDbToSync() {
        return dbToSync;
    }
    
    private @NotNull List<String> makeJSONStrings(@NotNull ResultSet resultSet) throws SQLException {
        List<String> jsonStrings = new ArrayList<>();
        if (resultSet.first())
        while (resultSet.next()) {
            JsonObject jsonObject = new JsonObject();
            if (resultSet.getInt(1) > lastLocalId) {
                int columnsIndexCount = resultSet.getMetaData().getColumnCount() + 1;
                for (int i = 1; i < columnsIndexCount; i++) {
                    jsonObject.add(resultSet.getMetaData().getColumnName(i), resultSet.getString(i));
                }
                jsonStrings.add(jsonObject.toString());
            }
        }
        return jsonStrings;
    }
    
    @Override
    public void setDbToSync(String dbToSync) {
        if (dbToSync.contains(".")) {
            this.dbToSync = ConstantsFor.DBBASENAME_U0466446_VELKOM + "." + dbToSync.split("\\Q.\\E")[1];
        }
        else {
            this.dbToSync = ConstantsFor.DBBASENAME_U0466446_VELKOM + "." + dbToSync;
        }
    }
    
    @Override
    Map<String, String> makeColumns() {
        Map<String, String> colMap = new HashMap<>();
        colMap.put("Not ready", "17.09.2019 (10:09)");
        return colMap;
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