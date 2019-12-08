package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


/**
 @see DataSynchronizerTest
 @since 26.11.2019 (21:39) */
public class DataSynchronizer extends SyncData {


    private final long startStamp = System.currentTimeMillis();

    private String dbToSync = ConstantsFor.DB_VELKOMVELKOMPC;

    private String columnName = ConstantsFor.DBCOL_IDREC;

    private DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);

    private Map<Integer, String> colNames = new ConcurrentHashMap<>();

    private int columnsNum = 0;

    private File dbObj = new File(dbToSync);

    private int totalRows = 0;

    DataSynchronizer() {
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
    public String toString() {
        return new StringJoiner(",\n", DataSynchronizer.class.getSimpleName() + "[\n", "\n]")
            .add("dbToSync = '" + dbToSync + "'")
            .add("columnName = '" + columnName + "'")
            .add("dataConnectTo = " + dataConnectTo)
            .add("colNames = " + colNames)
            .add("columnsNum = " + columnsNum)
            .toString();
    }

    private @NotNull String[] getColumns(@NotNull PreparedStatement preparedStatement) throws SQLException {
        ResultSetMetaData metaData = preparedStatement.getMetaData();
        int countCol = metaData.getColumnCount();
        String[] retArr = new String[countCol];
        for (int i = 0; i < countCol; i++) {
            retArr[i] = metaData.getColumnName(i + 1) + "," + metaData.getColumnTypeName(i + 1);
        }
        return retArr;
    }

    @Override
    public int createTable(String dbPointTable, @NotNull List<String> additionalColumns) {
        int retInt;
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("CREATE TABLE ")
            .append(dbPointTable)
            .append(" (`idrec` INT NOT NULL AUTO_INCREMENT, ")
            .append("`tstamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(), ");
        if (additionalColumns.size() > 0) {
            additionalColumns.forEach(sqlBuilder::append);
        }
        sqlBuilder.append("PRIMARY KEY (`idrec`))");
        final String sql = sqlBuilder.toString();
        try (Connection connection = dataConnectTo.getDefaultConnection(dbPointTable);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            retInt = preparedStatement.executeUpdate();
            addComment();
        }
        catch (SQLException e) {
            if (e.getMessage().contains("already exists")) {
                retInt = -1;
            }
            else {
                messageToUser.error("DataSynchronizer.createTable", e.getMessage(), AbstractForms.networkerTrace(e));
                retInt = -666;
            }
        }
        this.dbToSync = dbPointTable;
        return retInt;
    }

    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        int retInt = 0;
        Queue<Object> jsonObjects = new LinkedList<Object>(stringsCollection);
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.H2DB).getDefaultConnection(tableName)) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setSavepoint();
            for (Object jsonObject : jsonObjects) {
                retInt += workWithObject(jsonObject, connection);
            }
            connection.commit();
        }
        catch (NumberFormatException | SQLException e) {
            messageToUser.warn(DataSynchronizer.class.getSimpleName(), e.getMessage(), " see line: 158 ***");
            retInt = -666;
        }
        return retInt;
    }

    @Override
    public void superRun() {
        Thread.currentThread().checkAccess();
        Thread.currentThread().setPriority(4);
        List<String> dbNames = getDbNames();
        for (String dbName : dbNames) {
            List<String> tblNames = getTblNames(dbName);
            for (String tblName : tblNames) {
                this.dbToSync = dbName + "." + tblName;
                this.dbObj = new File(dbToSync);
                try {
                    syncData();
                }
                catch (RuntimeException ignore) {
                    //27.11.2019 (0:06)
                }
                finally {
                    dbObj.deleteOnExit();
                }
            }
        }
        messageToUser.warn(this.getClass().getSimpleName(), "superRun", MessageFormat.format("Total {0} rows affected", totalRows));
        MessageToUser.getInstance(MessageToUser.SWING, this.getClass().getSimpleName())
            .infoTimer(20, this.getClass().getSimpleName() + "\nsuperRun" + MessageFormat
                .format("Total {0} rows affected\nTime spend: {1} sec.", totalRows, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startStamp)));
    }

    @Override
    public void setOption(Object option) {
        if (option instanceof DataConnectTo) {
            this.dataConnectTo = (DataConnectTo) option;
        }
        else {
            throw new InvokeIllegalException("Set " + DataConnectTo.class.getSimpleName());
        }
    }

    /**
     @return results

     @see DataSynchronizerTest#testSyncData()
     */
    @Override
    public String syncData() {
        final String sql = String.format("SELECT * FROM %s WHERE %s > %d", dbToSync, columnName, getLastLocalID(dbToSync));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(sql).append("\n");
        int uploadedCount;
        Queue<JsonObject> jsonObjects = new LinkedList<>();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(dbToSync)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                String[] columns = getColumns(preparedStatement);
                this.columnsNum = columns.length;
                stringBuilder.append(Arrays.toString(columns)).append("\n");
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    Files.deleteIfExists(dbObj.toPath());
                    while (resultSet.next()) {
                        JsonObject jsonObject = new JsonObject();
                        for (int i = 0; i < columns.length; i++) {
                            jsonObject.add(columns[i].split(",")[0], resultSet.getString(i + 1));
                        }
                        jsonObjects.add(jsonObject);
                    }
                }
            }
        }
        catch (SQLException | IOException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        uploadedCount = uploadCollection(jsonObjects, dbToSync);
        if (uploadedCount != -666) {
            stringBuilder.append(uploadedCount).append(" items uploaded").append("\n");
        }
        else {
            throw new InvokeIllegalException(stringBuilder.toString());
        }
        messageToUser.info(this.getClass().getSimpleName(), "syncData", stringBuilder.toString());
        this.totalRows += uploadedCount;
        return stringBuilder.toString();
    }

    private void addComment() {
        String sql = String.format("ALTER TABLE %s COMMENT='Automatically created by %s, at %s';", dbToSync, this.getClass().getTypeName(), new Date());
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.TESTING).getDefaultConnection(dbToSync);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            messageToUser.warn(DataSynchronizer.class.getSimpleName(), "addComment", e.getMessage() + Thread.currentThread().getState().name());
        }
    }

    @Override
    public Map<String, String> makeColumns() {
        throw new UnsupportedOperationException("27.11.2019 (21:03)");
    }

    private @NotNull List<String> getDbNames() {
        List<String> dbNames = new ArrayList<>();
        try (Connection connection = dataConnectTo.getDefaultConnection(dbToSync);
             PreparedStatement preparedStatement = connection.prepareStatement("show databases");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String dbName = resultSet.getString(1);
                if (Stream.of("_schema", "mysql", "log", "lan").anyMatch(dbName::contains)) {
                    System.out.println("dbName = " + dbName);
                }
                else {
                    dbNames.add(dbName);
                }
                Thread.currentThread().setName(dbName);
            }
        }
        catch (SQLException e) {
            messageToUser.error("DataSynchronizer.getDbNames", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            dbNames.add(AbstractForms.networkerTrace(e));
        }
        return dbNames;
    }

    private @NotNull List<String> getTblNames(String dbName) {
        List<String> tblNames = new ArrayList<>();
        try (Connection connection = dataConnectTo.getDefaultConnection(dbToSync);
             PreparedStatement preparedStatement = connection.prepareStatement("SHOW TABLE STATUS FROM `" + dbName + "`");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                tblNames.add(resultSet.getString("Name"));
            }
        }
        catch (SQLException e) {
            messageToUser.error("DataSynchronizer.getTblNames", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            tblNames.add(AbstractForms.networkerTrace(e));
        }
        return tblNames;
    }

    private int workWithObject(@NotNull Object object, Connection connection) throws SQLException {
        int retInt = 0;
        JsonObject jsonObject = Json.parse(object.toString()).asObject();
        String sql = "null";
        try {
            sql = genSQL(jsonObject);
        }
        catch (RuntimeException ignore) {
            //28.11.2019 (12:59)
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, Integer.parseInt(colNames.get(1)));
            for (int j = 2; j < columnsNum + 1; j++) {
                preparedStatement.setString(j, colNames.get(j));
            }
            retInt += preparedStatement.executeUpdate();
            FileSystemWorker.appendObjectToFile(dbObj, jsonObject);
        }
        catch (SQLException e) {
            retInt -= retInt;
            connection.rollback();
        }
        finally {
            colNames.clear();
        }
        return retInt;
    }

    private @NotNull String genSQL(@NotNull JsonObject jsonObject) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ").append(dbToSync).append(" (");
        Iterator<JsonObject.Member> iterator = jsonObject.iterator();
        int ind = 1;
        while (iterator.hasNext()) {
            JsonObject.Member nextMember = iterator.next();
            colNames.put(ind++, nextMember.getValue().asString());
            sqlBuilder.append(nextMember.getName()).append(", ");
        }
        sqlBuilder.replace(sqlBuilder.length() - 2, sqlBuilder.length(), "");
        sqlBuilder.append(") VALUES (");
        for (int j = 0; j < colNames.size(); j++) {
            sqlBuilder.append("?, ");
        }
        sqlBuilder.replace(sqlBuilder.length() - 2, sqlBuilder.length(), "");
        sqlBuilder.append(")");
        return sqlBuilder.toString();
    }


}