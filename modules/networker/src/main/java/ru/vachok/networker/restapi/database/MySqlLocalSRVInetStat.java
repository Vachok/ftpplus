package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
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
    
    private String tableName;
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MySqlLocalSRVInetStat{");
        sb.append("tableName='").append(tableName).append('\'');
        sb.append(", dbName='").append(dbName).append('\'');
        sb.append('}');
        return sb.toString();
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
        final String insertTo = String.format("INSERT INTO `%s`.`%s` (`upstring`, `stamp`) VALUES (?, ?)", dbName, tableName);
        MysqlDataSource source = getDataSource();
        source.setDatabaseName(dbName);
        source.setContinueBatchOnError(true);
        List<String> colList = new ArrayList<>(strings);
        
        try (Connection connection = source.getConnection()) {
            try (PreparedStatement preparedStatementInsert = connection.prepareStatement(insertTo)) {
                for (String s : colList) {
                    if (s.length() > 259) {
                        s = s.substring(0, 259);
                    }
                    preparedStatementInsert.setString(1, s);
                    preparedStatementInsert.setLong(2, System.currentTimeMillis());
                    resultsUpload += preparedStatementInsert.executeUpdate();
                }
            }
            catch (MySQLIntegrityConstraintViolationException e) {
                if (e.getMessage().contains(ConstantsFor.ERROR_DUPLICATEENTRY)) {
                    resultsUpload += 1;
                }
                else {
                    messageToUser.error("MySqlLocalSRVInetStat.uploadCollection", e.getMessage(), new TForms().exceptionNetworker(e.getStackTrace()));
                }
            }
        }
        catch (SQLException e) {
            if (e.getMessage().contains(ConstantsFor.ERROR_NOEXIST)) {
                resultsUpload = createTable(dbPointTableName, Collections.EMPTY_LIST);
                resultsUpload += uploadCollection(strings, dbPointTableName);
            }
            else {
                messageToUser.error(MessageFormat.format("MySqlLocalSRVInetStat.uploadCollection", e.getMessage(), new TForms().exceptionNetworker(e.getStackTrace())));
            }
        }
        return resultsUpload;
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
            messageToUser.error("Table: " + dbPointTable + " was not dropped!");
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
        final String[] createTable = getCreateQuery(dbPointTable, additionalColumns);
        for (String query : createTable) {
            try (PreparedStatement preparedStatementTable = getDefaultConnection(dbName + "." + tableName).prepareStatement(query)) {
                int executeUpdate = preparedStatementTable.executeUpdate();
                createInt += executeUpdate;
            }
            catch (SQLException e) {
                createInt = -666;
            }
        }
        return createInt;
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
            Thread.currentThread().setName(retSource.getDatabaseName());
        }
        catch (SQLException | FileNotFoundException e) {
            messageToUser.error("MySqlLocalSRVInetStat.getDataSource", e.getMessage(), new TForms().exceptionNetworker(e.getStackTrace()));
        }
        return retSource;
    }
    
    @Override
    public Connection getDefaultConnection(@NotNull String dbName) {
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
            Connection connection = defDataSource.getConnection();
            Thread.currentThread().setName(defDataSource.getDatabaseName());
            return connection;
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("MySqlLocalSRVInetStat.getDefaultConnection", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
            return null;
        }
    }
    
    @Contract("_ -> new")
    private @NotNull String[] getCreateQuery(@NotNull String dbPointTableName, List<String> additionalColumns) {
        if (!dbPointTableName.contains(".")) {
            dbPointTableName = DBNAME_VELKOM_POINT + dbPointTableName;
        }
        String[] dbTable = dbPointTableName.split("\\Q.\\E");
        if (dbTable[1].startsWith(String.valueOf(Pattern.compile("\\d")))) {
            throw new IllegalArgumentException(dbTable[1]);
        }
        String engine = ConstantsFor.DBENGINE_MEMORY;
    
        if (!dbTable[0].equals(ConstantsFor.DB_SEARCH)) {
            engine = "MyISAM" ;
        }
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilder1 = new StringBuilder();
        StringBuilder stringBuilder2 = new StringBuilder();
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder.append("CREATE TABLE IF NOT EXISTS ")
                .append(dbTable[0])
                .append(".")
                .append(dbTable[1])
                .append("(\n")
                .append("  `idrec` mediumint(11) unsigned NOT NULL COMMENT '',\n")
                .append("  `stamp` bigint(13) unsigned NOT NULL COMMENT '',\n")
                .append("  `upstring` varchar(260) NOT NULL COMMENT '',\n");
        if (!additionalColumns.isEmpty()) {
            additionalColumns.forEach(stringBuilder::append);
        }
        else {
            stringBuilder.replace(stringBuilder.length() - 2, stringBuilder.length(), "");
        }
    
        stringBuilder.append(") ENGINE=").append(engine).append(" MAX_ROWS=100000;\n");
        
        stringBuilder2.append(ConstantsFor.SQL_ALTERTABLE)
                .append(dbTable[0])
                .append(".")
                .append(dbTable[1])
                .append("\n")
                .append("  ADD PRIMARY KEY (`idrec`);\n");
        stringBuilder1.append(ConstantsFor.SQL_ALTERTABLE).append(dbPointTableName).append("\n")
                .append("  MODIFY `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '';");
        
        stringBuilder3.append("ALTER TABLE ").append(dbTable[1]).append(" ADD UNIQUE (`upstring`);");
        return new String[]{stringBuilder.toString(), stringBuilder2.toString(), stringBuilder1.toString(), stringBuilder3.toString()};
    }
}
