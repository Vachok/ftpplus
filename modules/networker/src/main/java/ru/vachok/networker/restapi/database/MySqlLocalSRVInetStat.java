package ru.vachok.networker.restapi.database;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.DBConnectException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


/**
 @see MySqlLocalSRVInetStatTest
 @since 04.09.2019 (16:42) */
@SuppressWarnings("rawtypes")
class MySqlLocalSRVInetStat implements DataConnectTo {


    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, MySqlLocalSRVInetStat.class.getSimpleName());

    private Collection strings;

    private String dbName = ConstantsFor.STR_VELKOM;

    private String tableName = ConstantsFor.STR_VELKOM;

    private void setFields(@NotNull String dbPointTableName) {
        this.dbName = dbPointTableName;
        if (!dbPointTableName.contains(".")) {
            dbPointTableName = MessageFormat.format("{0}{1}", DBNAME_VELKOM_POINT, dbPointTableName);
        }
        this.dbName = dbPointTableName.split("\\Q.\\E")[0];
        this.tableName = dbPointTableName.split("\\Q.\\E")[1];
    }

    public Connection getDefaultConnection(@NotNull String dbName) {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        Connection connection = null;
        if (dbName.matches("^[a-z]+[a-z_0-9]{2,20}\\Q.\\E[a-z_0-9]{2,30}[a-z \\d]$")) {
            this.dbName = dbName.split("\\Q.\\E")[0];
            this.tableName = dbName.split("\\Q.\\E")[1];
        }
        else {
            throw new IllegalArgumentException(dbName);
        }
        MysqlDataSource defDataSource = new MysqlDataSource();
        defDataSource.setServerName(OtherKnownDevices.SRV_INETSTAT);
        defDataSource.setPort(3306);
        defDataSource.setPassword("1qaz@WSX");
        defDataSource.setUser("it");
        defDataSource.setEncoding("UTF-8");
        defDataSource.setCharacterEncoding("UTF-8");
        defDataSource.setDatabaseName(this.dbName);
        defDataSource.setUseSSL(false);
        defDataSource.setVerifyServerCertificate(false);
        defDataSource.setAutoClosePStmtStreams(true);
        defDataSource.setRelaxAutoCommit(true);
        defDataSource.setInteractiveClient(true);
        try {
            defDataSource.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5));
            defDataSource.setSocketTimeout((int) TimeUnit.MINUTES.toMillis(20));
            connection = tryConnect(defDataSource);
        }
        catch (DBConnectException e) {
            messageToUser.error(tableName, e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            connection = DataConnectTo.getInstance(DataConnectTo.REGRUCONNECTION).getDefaultConnection(tableName);
        }
        catch (SQLException e) {
            messageToUser.warn(MySqlLocalSRVInetStat.class.getSimpleName(), e.getMessage(), " see line: 60 ***");
        }
        return connection;
    }

    @Override
    public int hashCode() {
        int result = strings != null ? strings.hashCode() : 0;
        result = 31 * result + (dbName != null ? dbName.hashCode() : 0);
        result = 31 * result + (tableName != null ? tableName.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MySqlLocalSRVInetStat)) {
            return false;
        }

        MySqlLocalSRVInetStat stat = (MySqlLocalSRVInetStat) o;

        return (strings != null ? strings.equals(stat.strings) : stat.strings == null) && (dbName != null ? dbName
            .equals(stat.dbName) : stat.dbName == null) && (tableName != null ? tableName.equals(stat.tableName) : stat.tableName == null);
    }

    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(PropertiesNames.CLASS, getClass().getSimpleName());
        jsonObject.add(PropertiesNames.HASH, this.hashCode());
        jsonObject.add(PropertiesNames.TIMESTAMP, System.currentTimeMillis());
        if (strings != null && strings.size() > 0) {
            jsonObject.add("strings", strings.size());
        }
        jsonObject.add("dbName", dbName);
        jsonObject.add("tableName", tableName);
        return jsonObject.toString();
    }

    /**
     @see MySqlLocalSRVInetStatTest#testUploadCollection()
     */
    @SuppressWarnings("JDBCPrepareStatementWithNonConstantString")
    @Override
    public int uploadCollection(Collection strings, @NotNull String dbPointTableName) {
        this.strings = strings;
        int resultsUpload = 0;
        setFields(dbPointTableName);
        MysqlDataSource source = getDataSource();
        source.setDatabaseName(dbName);
        source.setContinueBatchOnError(true);
        source.setInteractiveClient(true);
        JsonValue toStr = Json.parse(toString());
        try (Connection connection = source.getConnection()) {
            final String insertTo = String.format("INSERT INTO `%s`.`%s` (`upstring`, `json`) VALUES (?, ?)", dbName, tableName);
            try (PreparedStatement preparedStatementInsert = connection.prepareStatement(insertTo)) {
                for (Object o : strings) {
                    String s = (String) o;
                    if (s.length() > 190) {
                        preparedStatementInsert.setString(1, String.valueOf(190));
                        preparedStatementInsert.setString(2, s);
                    }
                    else {
                        String noVal = "No value";
                        preparedStatementInsert.setString(1, noVal);
                        preparedStatementInsert.setString(2, noVal);
                    }
                    resultsUpload += preparedStatementInsert.executeUpdate();
                }
            }
            catch (MySQLIntegrityConstraintViolationException e) {
                if (e.getMessage().contains(ConstantsFor.ERROR_DUPLICATEENTRY)) {
                    resultsUpload += 1;
                }
                else {
                    messageToUser.error("MySqlLocalSRVInetStat.uploadCollection", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
                    toStr.asObject().add(ConstantsFor.STR_ERROR, e.getMessage());
                }
            }
        }
        catch (SQLException e) {
            resultsUpload = sqlExp(dbPointTableName, e.getMessage(), resultsUpload);
            messageToUser.warn(MySqlLocalSRVInetStat.class.getSimpleName(), e.getMessage(), " see line: 218 ***");
            toStr.asObject().add(ConstantsFor.STR_ERROR, e.getMessage());
        }
        finally {
            messageToUser.info(toStr.toString());
        }
        return resultsUpload;
    }

    @Override
    public MysqlDataSource getDataSource() {
        MysqlDataSource retSource = new MysqlDataSource();
        retSource.setServerName(OtherKnownDevices.SRV_INETSTAT);
        retSource.setPassword("1qaz@WSX");
        retSource.setUser("it");
        retSource.setCharacterEncoding("UTF-8");
        retSource.setEncoding("UTF-8");
        retSource.setDatabaseName(this.dbName);
        retSource.setContinueBatchOnError(true);
        retSource.setAutoReconnect(true);
        retSource.setReconnectAtTxEnd(true);
        retSource.setCachePreparedStatements(true);
        retSource.setCacheCallableStatements(true);
        retSource.setInteractiveClient(true);
        retSource.setUseCompression(false);
        retSource.setUseInformationSchema(true);
        try {
            retSource.setLogWriter(new PrintWriter(retSource.getDatabaseName() + ".log"));
            retSource.setDumpQueriesOnException(true);
        }
        catch (SQLException | FileNotFoundException e) {
            messageToUser.error("MySqlLocalSRVInetStat.getDataSource", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        return retSource;
    }

    @Override
    public boolean dropTable(String dbPointTable) {
        this.dbName = dbPointTable;
        MysqlDataSource source = getDataSource();
        boolean retBool = false;
        try (Connection connection = source.getConnection();
             Statement statement = connection.createStatement()) {
            retBool = statement.execute(String.format(ConstantsFor.SQL_DROPTABLE, dbPointTable));
        }
        catch (MySQLSyntaxErrorException e) {
            messageToUser.error(this.getClass().getSimpleName(), "MySQLSyntaxErrorException", e.getMessage() + " see line: 151");
            messageToUser.error(ConstantsFor.TABLE + dbPointTable + " was not dropped!");
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("MySqlLocalSRVInetStat.dropTable: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            retBool = false;
        }
        return retBool;
    }

    @Override
    public int createTable(@NotNull String dbPointTable, List<String> additionalColumns) {
        this.dbName = dbPointTable.split("\\Q.\\E")[0];
        this.tableName = dbPointTable.split("\\Q.\\E")[1];
        int createInt = 0;
        final String createTable = getCreateQuery(dbPointTable, additionalColumns);
        try (Connection connection = getDefaultConnection(dbName + "." + tableName);
             Statement connectionStatement = connection.createStatement()) {
            boolean executeUpdate = connectionStatement.execute(createTable);
            if (executeUpdate) {
                createInt++;
            }
        }
        catch (SQLException e) {
            messageToUser.warn(MySqlLocalSRVInetStat.class.getSimpleName(), "createTable " + e.getErrorCode(), e.getMessage() + " see line: 178");
            createInt = -666;
        }
        return createInt;
    }

    /**
     @param defDataSource new MysqlDataSource
     @return {@link OtherKnownDevices#SRV_INETSTAT} {@link Connection}

     @throws DBConnectException if {@link Connection} is null
     */
    @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
    private Connection tryConnect(MysqlDataSource defDataSource) {
        Connection connection = null;
        try {
            connection = defDataSource.getConnection();
            connection.clearWarnings();
        }
        catch (SQLException e) {
            messageToUser.warn(MySqlLocalSRVInetStat.class.getSimpleName(), e.getMessage(), " see line: 78 ***");
        }
        if (connection != null) {
            return connection;
        }
        else {
            throw new DBConnectException(dbName + "." + tableName);
        }
    }

    @Contract("_, _ -> new")
    private @NotNull String getCreateQuery(@NotNull String dbPointTableName, List<String> additionalColumns) {
        if (!dbPointTableName.contains(".")) {
            dbPointTableName = DBNAME_VELKOM_POINT + dbPointTableName;
        }
        String[] dbTable = dbPointTableName.split("\\Q.\\E");
        if (dbTable[1].startsWith(String.valueOf(Pattern.compile("\\d")))) {
            throw new IllegalArgumentException(dbTable[1]);
        }
        String engine;

        if (dbTable[0].equals(ConstantsFor.DB_SEARCH)) {
            engine = "MyISAM";
        }
        else {
            engine = "InnoDB";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CREATE TABLE IF NOT EXISTS ")
            .append(dbTable[0])
            .append(".")
            .append(dbTable[1])
            .append("(\n")
            .append("\t`idrec` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,\n")
            .append("\t`tstamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n")
            .append("\t`upstring` VARCHAR(190) NOT NULL DEFAULT 'not set',\n")
            .append("\t`json` TEXT NULL,\n");
        if (!additionalColumns.isEmpty()) {
            additionalColumns.forEach(stringBuilder::append);
        }
        stringBuilder.append("\tPRIMARY KEY (`idrec`),\n" +
            "\tUNIQUE INDEX `upstring` (`upstring`)");
        stringBuilder.append(") ENGINE=").append(engine).append(" MAX_ROWS=100000;\n");
        return stringBuilder.toString();
    }

    private int sqlExp(@NotNull String dbPointTableName, @NotNull String errMessage, int resultsUpload) {
        int i = resultsUpload;
        if (errMessage.contains(ConstantsFor.ERROR_NOEXIST)) {
            i = createTable(dbPointTableName, Collections.EMPTY_LIST);
            if (i != -666) {
                i += uploadCollection(strings, dbPointTableName);
            }
        }
        else {
            messageToUser.warn(MySqlLocalSRVInetStat.class.getSimpleName(), errMessage, " see line: 235 ***");
            i--;
        }
        return i;
    }


}
