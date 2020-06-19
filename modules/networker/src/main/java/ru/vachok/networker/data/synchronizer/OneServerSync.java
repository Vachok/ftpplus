package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ConstantsNet;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.*;
import java.sql.*;
import java.util.*;


/**
 @see OneServerSyncTest
 @since 26.12.2019 (19:33) */
public class OneServerSync extends SyncData {


    public static final String SYNCED = "synced";

    private final File file;

    private String dbToSync;

    private DataConnectTo dataConnectTo;

    public OneServerSync() {
        this.dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        this.dbToSync = ConstantsFor.DB_VELKOMVELKOMPC;
        this.file = new File(dbToSync);
    }

    @Override
    public void run() {
        superRun();
    }

    @Override
    public Object getRawResult() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.OneServerSync.getRawResult( Object ) at 17.05.2020 - (13:10)");
    }

    @Override
    protected void setRawResult(Object value) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.OneServerSync.setRawResult( void ) at 17.05.2020 - (13:10)");
    }

    @Override
    public String getDbToSync() {
        return dbToSync;
    }

    @Override
    public void setDbToSync(String dbToSync) {
        this.dbToSync = dbToSync;
    }

    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        this.dbToSync = tableName;
        return 0;
    }

    @Override
    public void setOption(Object option) {
        if (option instanceof DataConnectTo) {
            this.dataConnectTo = (DataConnectTo) option;
        }
        else {
            this.dbToSync = option.toString();
        }
    }

    @Override
    public String syncData() {
        int indexFrom = getLastRemoteID(ConstantsFor.DB_ARCHIVEVELKOMPC);

        try (Connection connection = dataConnectTo.getDefaultConnection(dbToSync)) {
            try (OutputStream outputStream = new FileOutputStream(dbToSync)) {
                try (PrintWriter printWriter = new PrintWriter(outputStream, true)) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + dbToSync + " WHERE idrec > " + indexFrom)) {
                        ResultSetMetaData mData = preparedStatement.getMetaData();
                        List<String> colNames = new ArrayList<>();
                        for (int i = 1; i < mData.getColumnCount(); i++) {
                            colNames.add(mData.getColumnName(i));
                        }
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            while (resultSet.next()) {
                                JsonObject jsonObject = new JsonObject();
                                colNames.forEach(colName->{
                                    try {
                                        jsonObject.set(colName, resultSet.getString(colName));
                                    }
                                    catch (SQLException e) {
                                        messageToUser.warn(OneServerSync.class.getSimpleName(), e.getMessage(), " see line: 78 ***");
                                    }
                                });
                                printWriter.println(jsonObject);
                            }
                        }
                    }
                }
            }
        }
        catch (SQLException | IOException e) {
            messageToUser.error(e.getMessage());
        }
        finally {
            superRun();
        }
        return file.getAbsolutePath();
    }

    @Override
    public void superRun() {
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_ARCHIVEVELKOMPC)) {
            try (PreparedStatement preparedStatement = connection
                .prepareStatement("INSERT INTO archive.velkompc (idrec, NamePP, AddressPP, SegmentPP, instr, OnlineNow, userName) values (?,?,?,?,?,?,?)")) {
                try (InputStream inputStream = new FileInputStream(file)) {
                    preparedStatement.setQueryTimeout(100);
                    try (Scanner scanner = new Scanner(inputStream)) {
                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            JsonObject parsedObj = (JsonObject) Json.parse(line);
                            preparedStatement.setInt(1, Integer.parseInt(parsedObj.getString(ConstantsFor.DBCOL_IDREC, "0")));
                            preparedStatement.setString(2, parsedObj.getString(ConstantsFor.DBCOL_NAMEPP, "0"));
                            preparedStatement.setString(3, parsedObj.getString(ConstantsFor.DBCOL_ADDRPP, "0"));
                            preparedStatement.setString(4, parsedObj.getString(ConstantsFor.DBCOL_SEGPP, "0"));
                            preparedStatement.setString(5, parsedObj.getString("instr", "0"));
                            preparedStatement.setString(6, parsedObj.getString(ConstantsNet.ONLINE_NOW, "0"));
                            preparedStatement.setString(7, parsedObj.getString(ConstantsFor.DBFIELD_USERNAME, "0"));
                            preparedStatement.addBatch();
                        }
                    }
                }
                preparedStatement.executeBatch();
            }
        }
        catch (SQLException | IOException e) {
            messageToUser.error(e.getMessage());
        }
        finally {
            messageToUser.info(getClass().getSimpleName(), SYNCED, dbToSync);
        }
    }

    @Override
    public int createTable(String dbPointTable, List<String> additionalColumns) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.OneServerSync.createTable( int ) at 26.12.2019 - (19:34)");
    }

    @Override
    public Map<String, String> makeColumns() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.OneServerSync.makeColumns( Map<String, String> ) at 26.12.2019 - (19:34)");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OneServerSync)) {
            return false;
        }

        OneServerSync sync = (OneServerSync) o;

        if (file != null ? !file.equals(sync.file) : sync.file != null) {
            return false;
        }
        if (dbToSync != null ? !dbToSync.equals(sync.dbToSync) : sync.dbToSync != null) {
            return false;
        }
        return dataConnectTo != null ? dataConnectTo.equals(sync.dataConnectTo) : sync.dataConnectTo == null;
    }

    @Override
    public int hashCode() {
        int result = file != null ? file.hashCode() : 0;
        result = 31 * result + (dbToSync != null ? dbToSync.hashCode() : 0);
        result = 31 * result + (dataConnectTo != null ? dataConnectTo.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(PropertiesNames.CLASS, getClass().getSimpleName());
        jsonObject.add(PropertiesNames.HASH, this.hashCode());
        jsonObject.add(PropertiesNames.TIMESTAMP, System.currentTimeMillis());
        jsonObject.add("file", file.getAbsolutePath());
        jsonObject.add("dbToSync", dbToSync);
        jsonObject.add("dataConnectTo", dataConnectTo.toString());
        return jsonObject.toString();
    }
}