package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;


/**
 @see MySqlLocalSRVInetStatTest
 @since 04.09.2019 (16:42) */
class MySqlLocalSRVInetStat implements DataConnectTo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, MySqlLocalSRVInetStat.class.getSimpleName());
    
    private String dbName = ConstantsFor.STR_VELKOM;
    
    private String tableName = ConstantsFor.STR_VELKOM;
    
    @Override
    public MysqlDataSource getDataSource() {
        MysqlDataSource retSource = new MysqlDataSource();
        retSource.setServerName(OtherKnownDevices.SRV_INETSTAT);
        retSource.setPassword("1qaz@WSX");
        retSource.setUser("it");
        retSource.setCharacterEncoding("UTF-8");
        retSource.setEncoding("UTF-8");
        retSource.setDatabaseName(this.dbName);
        retSource.setCreateDatabaseIfNotExist(true);
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
    public int uploadCollection(Collection strings, @NotNull String dbPointTableName) {
        this.dbName = dbPointTableName;
        int resultsUpload = 0;
        
        if (!dbPointTableName.contains(".")) {
            dbPointTableName = DBNAME_VELKOM_POINT + dbPointTableName;
        }
        
        this.dbName = dbPointTableName.split("\\Q.\\E")[0];
        this.tableName = dbPointTableName.split("\\Q.\\E")[1];
        final String insertTo = String.format("INSERT INTO `%s`.`%s` (`upstring`, `json`) VALUES (?, ?)", dbName, tableName);
        MysqlDataSource source = getDataSource();
        source.setDatabaseName(dbName);
        source.setContinueBatchOnError(true);
        List<String> colList = new ArrayList<>(strings);
        
        try (Connection connection = source.getConnection()) {
            try (PreparedStatement preparedStatementInsert = connection.prepareStatement(insertTo)) {
                for (String s : colList) {
                    String s1 = s;
                    if (s1.length() > 190) {
                        preparedStatementInsert.setString(1, String.valueOf(s1.length()));
                        preparedStatementInsert.setString(2, s1);
                    }
                    else {
                        preparedStatementInsert.setString(1, s1);
                        preparedStatementInsert.setString(2, this.toString());
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
                }
            }
        }
        catch (SQLException e) {
            if (e.getMessage().contains(ConstantsFor.ERROR_NOEXIST)) {
                resultsUpload = createTable(dbPointTableName, Collections.EMPTY_LIST);
                if (resultsUpload != -666) {
                    resultsUpload += uploadCollection(strings, dbPointTableName);
                }
            }
            else {
                messageToUser
                        .error(MessageFormat.format("MySqlLocalSRVInetStat.uploadCollection", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace())));
            }
        }
        return resultsUpload;
    }
    
    @Override
    public Connection getDefaultConnection(@NotNull String dbName) {
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
        defDataSource.setAutoReconnect(true);
        defDataSource.setCreateDatabaseIfNotExist(true);
        try {
            connection = defDataSource.getConnection();
        }
        catch (SQLException e) {
            messageToUser.warn("MySqlLocalSRVInetStat", "getDefaultConnection", e.getMessage() + " see line: 189");
        }
        if (connection != null) {
            return connection;
        }
        else {
            throw new InvokeIllegalException(MessageFormat.format("{0} DEFAULT CONNECTION ERROR! NULL!", this.getClass().getSimpleName()));
        }
    }
    
    private void abortConnection(@NotNull Connection connection) {
        try {
            connection.abort(AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor());
        }
        catch (SQLException e) {
            messageToUser.error("MySqlLocalSRVInetStat.abortConnection", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MySqlLocalSRVInetStat{");
        sb.append("\"tableName\":\"").append(tableName).append("\",");
        sb.append("\"dbName\":\"").append(dbName).append("\"");
        sb.append('}');
        return sb.toString();
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
        String engine = ConstantsFor.DBENGINE_MEMORY;
        
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
    
    @Override
    public int createTable(@NotNull String dbPointTable, List<String> additionalColumns) {
        this.dbName = dbPointTable.split("\\Q.\\E")[0];
        this.tableName = dbPointTable.split("\\Q.\\E")[1];
        int createInt = 0;
        final String createTable = getCreateQuery(dbPointTable, additionalColumns);
        try (Connection connection = getDefaultConnection(dbName + "." + tableName);
             PreparedStatement preparedStatementTable = connection.prepareStatement(createTable)) {
            int executeUpdate = preparedStatementTable.executeUpdate();
            createInt += executeUpdate;
        }
        catch (SQLException e) {
            messageToUser.warn(MySqlLocalSRVInetStat.class.getSimpleName(), "createTable", e.getMessage() + " see line: 178");
            createInt = -666;
        }
        return createInt;
    }
    
    @Override
    public boolean dropTable(String dbPointTable) {
        this.dbName = dbPointTable;
        MysqlDataSource source = getDataSource();
        boolean retBool = false;
        try (Connection connection = source.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(String.format(ConstantsFor.SQL_DROPTABLE, dbPointTable))) {
            preparedStatement.executeUpdate();
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
    

    
    
}
