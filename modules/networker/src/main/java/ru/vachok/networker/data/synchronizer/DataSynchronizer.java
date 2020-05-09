package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;
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

    private final String columnName = ConstantsFor.DBCOL_IDREC;

    private final Map<Integer, String> colNames = new ConcurrentHashMap<>();

    private String dbToSync = ConstantsFor.DB_VELKOMVELKOMPC;

    private DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);

    private int columnsNum = 0;

    private File dbObj = new File(dbToSync);

    private int totalRows = 0;

    private int dbsTotal = 0;

    DataSynchronizer() {
    }

    @Override
    public int hashCode() {
        int result = (int) (startStamp ^ (startStamp >>> 32));
        result = 31 * result + dbToSync.hashCode();
        result = 31 * result + columnName.hashCode();
        result = 31 * result + dataConnectTo.hashCode();
        result = 31 * result + colNames.hashCode();
        result = 31 * result + columnsNum;
        result = 31 * result + dbObj.hashCode();
        result = 31 * result + totalRows;
        result = 31 * result + dbsTotal;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataSynchronizer)) {
            return false;
        }

        DataSynchronizer that = (DataSynchronizer) o;

        if (startStamp != that.startStamp) {
            return false;
        }
        if (columnsNum != that.columnsNum) {
            return false;
        }
        if (totalRows != that.totalRows) {
            return false;
        }
        if (dbsTotal != that.dbsTotal) {
            return false;
        }
        if (!dbToSync.equals(that.dbToSync)) {
            return false;
        }
        if (!columnName.equals(that.columnName)) {
            return false;
        }
        if (!dataConnectTo.equals(that.dataConnectTo)) {
            return false;
        }
        if (!colNames.equals(that.colNames)) {
            return false;
        }
        return dbObj.equals(that.dbObj);
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

    @Override
    public String getDbToSync() {
        return dbToSync;
    }

    @Override
    public void setDbToSync(String dbToSync) {
        this.dbToSync = dbToSync;
    }

    @NotNull
    private List<String> getDbNames() {
        List<String> dbNames = new ArrayList<>();
        try (Connection connection = dataConnectTo.getDefaultConnection(dbToSync)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("show databases")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    preparedStatement.setQueryTimeout((int) TimeUnit.MINUTES.toSeconds(7));
                    while (resultSet.next()) {
                        String dbName = resultSet.getString(1);
                        if (Stream.of("_schema", "mysql", "log", "lan", "archive", ModelAttributeNames.COMMON).anyMatch(dbName::contains)) {
                            messageToUser.warn(getClass().getSimpleName(), "NO SYNC:", dbName);
                        }
                        else {
                            dbNames.add(dbName);
                        }
                        Thread.currentThread().setName(dbName);
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error("DataSynchronizer.getDbNames", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            dbNames.add(AbstractForms.networkerTrace(e));
        }
        return dbNames;
    }

    @Override
    public void setOption(Object option) {
        if (option instanceof DataConnectTo) {
            this.dataConnectTo = (DataConnectTo) option;
        }
        else {
            throw new IllegalArgumentException("Set " + DataConnectTo.class.getSimpleName());
        }
    }

    @Override
    public void superRun() {
        Thread.currentThread().checkAccess();
        Thread.currentThread().setPriority(4);
        List<String> dbNames = getDbNames();
        for (String dbName : dbNames) {
            List<String> tblNames = getTblNames(dbName);
            for (String tblName : tblNames) {
                this.dbsTotal += 1;
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
        String syncArch = new OneServerSync().syncData();
        messageToUser.warn(this.getClass().getSimpleName(), "superRun", MessageFormat.format("Total {0} rows affected", totalRows));
        MessageToUser.getInstance(MessageToUser.DB, this.getClass().getSimpleName())
            .warn(this.getClass().getSimpleName(), "DBs synced: ", String.valueOf(dbsTotal) + " and " + syncArch);
        MessageToUser.getInstance(MessageToUser.EMAIL, this.getClass().getSimpleName())
            .info(String.valueOf(totalRows), this.getClass().getSimpleName(), createMailText(syncArch));
    }

    @NotNull
    private String createMailText(String syncArch) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.getClass().getSimpleName())
            .append("\nsuperRun")
            .append(MessageFormat.format("Total {0} rows affected\nTime spend: {1} sec. DBs = {2}", totalRows, TimeUnit.MILLISECONDS
                .toSeconds(System.currentTimeMillis() - startStamp), dbsTotal))
            .append(" and ")
            .append(syncArch).append("\n\n\n");
        stringBuilder.append(AbstractForms.fromArray(getDbNames()));
        return stringBuilder.toString();
    }

    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        int retInt = 0;
        LinkedList<Object> jsonObjects = new LinkedList<Object>(stringsCollection);
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.TESTING).getDefaultConnection(tableName)) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setSavepoint();
            for (Object jsonObject : jsonObjects) {
                retInt += workWithObject(jsonObject, connection);
            }
            connection.commit();
        }
        catch (NumberFormatException | SQLException e) {
            if (e.getMessage().contains(ConstantsFor.ERROR_NOEXIST)) {
                DataConnectTo.getInstance(DataConnectTo.TESTING).createTable(tableName, Collections.EMPTY_LIST);
                retInt = 0;
            }
            else {
                messageToUser.warn(DataSynchronizer.class.getSimpleName(), e.getMessage(), " see line: 158 ***");
                retInt = -666;
            }

        }
        return retInt;
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
        if (!ConstantsFor.noRunOn(ConstantsFor.REGRUHOSTING_PC, "home") && (!dbToSync.contains(".inetstats") && !dbToSync
            .contains(ConstantsFor.DB_PCUSERAUTO_FULL))) {
            try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(dbToSync)) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setQueryTimeout((int) TimeUnit.MINUTES.toSeconds(7));
                    String[] columns = getColumns(preparedStatement);
                    this.columnsNum = columns.length;
                    stringBuilder.append(Arrays.toString(columns)).append("\n");
                    FileSystemWorker.writeFile(dbToSync, AbstractForms.fromArray(preparedStatement.getMetaData()).toString());
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
                messageToUser.warn(DataSynchronizer.class.getSimpleName(), e.getMessage(), " see line: 264 ***");
            }
        }
        uploadedCount = uploadCollection(jsonObjects, dbToSync);
        if (uploadedCount != -666) {
            stringBuilder.append(uploadedCount).append(" items uploaded").append("\n");
        }
        else {
            stringBuilder.append("Nothing to upload");
        }
        messageToUser.info(this.getClass().getSimpleName(), "syncData", stringBuilder.toString());
        this.totalRows += uploadedCount;
        return stringBuilder.toString();
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
            preparedStatement.setQueryTimeout((int) TimeUnit.MINUTES.toSeconds(7));
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

    private void addComment() {
        String sql = String.format("ALTER TABLE %s COMMENT='Automatically created by %s, at %s';", dbToSync, this.getClass().getTypeName(), new Date());
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.TESTING).getDefaultConnection(dbToSync);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setQueryTimeout((int) TimeUnit.MINUTES.toSeconds(7));
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

    @NotNull
    private String[] getColumns(@NotNull PreparedStatement preparedStatement) throws SQLException {
        ResultSetMetaData metaData = preparedStatement.getMetaData();
        int countCol = metaData.getColumnCount();
        String[] retArr = new String[countCol];
        for (int i = 0; i < countCol; i++) {
            retArr[i] = metaData.getColumnName(i + 1) + "," + metaData.getColumnTypeName(i + 1);
        }
        return retArr;
    }

    @NotNull
    private List<String> getTblNames(String dbName) {
        List<String> tblNames = new ArrayList<>();
        try (Connection connection = dataConnectTo.getDefaultConnection(dbToSync)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SHOW TABLE STATUS FROM `" + dbName + "`")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    preparedStatement.setQueryTimeout((int) TimeUnit.MINUTES.toSeconds(7));
                    while (resultSet.next()) {
                        String tableName = resultSet.getString("Name");
                        tblNames.add(tableName);

                    }
                }
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
            preparedStatement.setQueryTimeout((int) TimeUnit.MINUTES.toSeconds(7));
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

    @NotNull
    private String genSQL(@NotNull JsonObject jsonObject) {
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