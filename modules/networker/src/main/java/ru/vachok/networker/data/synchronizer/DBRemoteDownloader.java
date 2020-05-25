package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 @see DBRemoteDownloaderTest
 @since 08.09.2019 (17:36) */
class DBRemoteDownloader extends SyncData {


    private final int lastLocalId;

    private String dbToSync;

    private final List<String> jsonFromDB = new ArrayList<>();

    @Override
    public void run() {
        superRun();
    }

    @Override
    public Object getRawResult() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.DBRemoteDownloader.getRawResult( Object ) at 17.05.2020 - (13:10)");
    }

    @Override
    protected void setRawResult(Object value) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.DBRemoteDownloader.setRawResult( void ) at 17.05.2020 - (13:10)");
    }

    DBRemoteDownloader(int lastLocalID) {
        this.lastLocalId = lastLocalID;
    }

    @Override
    public int hashCode() {
        int result = lastLocalId;
        result = 31 * result + (dbToSync != null ? dbToSync.hashCode() : 0);
        result = 31 * result + jsonFromDB.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DBRemoteDownloader)) {
            return false;
        }

        DBRemoteDownloader that = (DBRemoteDownloader) o;

        if (lastLocalId != that.lastLocalId) {
            return false;
        }
        if (dbToSync != null ? !dbToSync.equals(that.dbToSync) : that.dbToSync != null) {
            return false;
        }
        return jsonFromDB.equals(that.jsonFromDB);
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
    public String getDbToSync() {
        return dbToSync;
    }

    @Override
    public void setDbToSync(@NotNull String dbToSync) {
        if (dbToSync.contains(".") & !dbToSync.matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
            this.dbToSync = dbToSync;
        }
        else {
            this.dbToSync = ConstantsFor.DBBASENAME_U0466446_VELKOM + "." + dbToSync;
        }
    }

    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        return new DBUploadUniversal(stringsCollection, tableName).uploadCollection(stringsCollection, tableName);
    }

    @Override
    public void setOption(Object option) {
        this.setDbToSync((String) option);
    }

    @Override
    public String syncData() {
        StringBuilder stringBuilder = new StringBuilder();
        fillListFromSQL();
        stringBuilder.append(writeJSON());
        return stringBuilder.toString();
    }

    @Override
    public void superRun() {
        try (Connection connection = CONNECT_TO_LOCAL.getDefaultConnection(dbToSync)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM %s", dbToSync));
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                makeJSONStrings(resultSet);
            }
        }
        catch (SQLException e) {
            messageToUser.error("DBRemoteDownloader.superRun " + dbToSync, e.getMessage(), new TForms().networkerTrace(e.getStackTrace()));
            messageToUser.error(CONNECT_TO_LOCAL.getDataSource().getURL());
        }
    }

    @Override
    public int createTable(String dbPointTable, List<String> additionalColumns) {
        throw new TODOException("0");

    }

    @Override
    public Map<String, String> makeColumns() {
        Map<String, String> colMap = new HashMap<>();
        colMap.put("Not ready", "17.09.2019 (10:09)");
        return colMap;
    }

    private void fillListFromSQL() {
        try (Connection connection = CONNECT_TO_REGRU.getDataSource().getConnection()) {
            String dbReg = ConstantsFor.DBBASENAME_U0466446_VELKOM + "." + dbToSync.split("\\Q.\\E")[1];
            final String sql = String.format("SELECT * FROM %s WHERE idrec > %s", dbReg, lastLocalId);
            connection.setNetworkTimeout(Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()), (int) TimeUnit.MINUTES.toMillis(1));
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    jsonFromDB.addAll(makeJSONStrings(resultSet));
                }
                catch (ParseException ignore) {
                    //11.09.2019 (15:10)
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("{0} see line: 91 *** {1}", e.getMessage(), CONNECT_TO_REGRU.getDataSource().getURL()));
        }
    }

    private String writeJSON() {
        String fileName = dbToSync + FileNames.EXT_TABLE;
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fileName))) {
            bufferedOutputStream.write(new TForms().fromArray(jsonFromDB).getBytes());
            return fileName;
        }
        catch (IOException e) {
            return e.getMessage() + " see line: 72";
        }
    }

    @NotNull
    private List<String> makeJSONStrings(@NotNull ResultSet resultSet) throws SQLException {
        List<String> jsonStrings = new ArrayList<>();
        if (resultSet.first()) {
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
        }
        return jsonStrings;
    }

    @Contract(pure = true)
    @NotNull
    private JsonObject makeJsonObject(@NotNull ResultSet resultSet) throws SQLException {
        JsonObject jsonObject = new JsonObject();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        for (int i = 1; i < columnCount; i++) {
            jsonObject.set(resultSetMetaData.getColumnName(i), resultSet.getString(i));
        }
        return jsonObject;
    }
}