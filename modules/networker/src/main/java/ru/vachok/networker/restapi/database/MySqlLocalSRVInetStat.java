package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ConstantsNet;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;


/**
 @see MySqlLocalSRVInetStatTest
 @since 04.09.2019 (16:42) */
class MySqlLocalSRVInetStat implements DataConnectTo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, MySqlLocalSRVInetStat.class.getSimpleName());
    
    private Connection defCon;
    
    private String dbName = ConstantsFor.U46_VELKOMPC;
    
    private Collection collection;
    
    MySqlLocalSRVInetStat() {
        try {
            defCon = getDataSource().getConnection();
        }
        catch (SQLException e) {
            messageToUser.error(this.getClass().getSimpleName() + " CONSTRUCT", e.getMessage(), String.format("Trying getDefaultConnection(%s)", dbName));
        }
    }
    
    @Override
    public Connection getDefaultConnection(String dbName) {
        this.dbName = dbName;
        MysqlDataSource defDataSource = new MysqlDataSource();
        
        defDataSource.setServerName(ConstantsNet.SRV_INETSTAT);
        defDataSource.setPort(3306);
        defDataSource.setPassword("1qaz@WSX");
        defDataSource.setUser("it");
        defDataSource.setEncoding("UTF-8");
        defDataSource.setCharacterEncoding("UTF-8");
        defDataSource.setDatabaseName(dbName);
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
            messageToUser.error(MessageFormat.format("MySqlLocalSRVInetStat.getDefaultConnection {0} - {1}", e.getClass().getTypeName(), e.getMessage()));
            return defCon;
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MySqlLocalSRVInetStat{");
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public MysqlDataSource getDataSource() {
        MysqlDataSource retSource = new MysqlDataSource();
        retSource.setServerName(ConstantsNet.SRV_INETSTAT);
        retSource.setPassword("1qaz@WSX");
        retSource.setUser("it");
        retSource.setCharacterEncoding("UTF-8");
        retSource.setEncoding("UTF-8");
        retSource.setDatabaseName(dbName);
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
    
    private void dropTable(String tableName) {
        MysqlDataSource source = getDataSource();
        try (Connection connection = source.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(String.format(ConstantsFor.SQL_DROPTABLE, tableName))) {
            preparedStatement.executeUpdate();
        }
        catch (MySQLSyntaxErrorException e) {
            messageToUser.error(this.getClass().getSimpleName(), "MySQLSyntaxErrorException", e.getMessage() + " see line: 151");
            messageToUser.error("Table: " + tableName + " was not dropped!");
            int i = uploadCollection(this.collection, this.dbName);
            
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 153");
        }
    }
    
    private int createTable() {
        int createInt = 0;
        final String[] createTable = getCreateQuery(dbName);
        for (String query : createTable) {
            try (PreparedStatement preparedStatementTable = defCon.prepareStatement(query)) {
                int executeUpdate = preparedStatementTable.executeUpdate();
                System.out.println(query + " = " + executeUpdate);
                createInt += executeUpdate;
            }
            catch (SQLException e) {
                messageToUser.error(e.getMessage() + " see line: 133");
                messageToUser.error("Dropping table " + dbName);
                dropTable(dbName);
            }
        }
        int upFile = uploadCollection(collection, dbName);
        return upFile + createInt;
    }
    
    private String[] getCreateQuery(String dbPointTableName) {
        if (!dbPointTableName.contains(".")) {
            dbPointTableName = DBNAME_VELKOM_POINT + dbPointTableName;
        }
        String[] dbTable = dbPointTableName.split("\\Q.\\E");
        if (dbTable[1].startsWith(String.valueOf(Pattern.compile("\\d")))) {
            throw new IllegalArgumentException(dbTable[1]);
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
            .append("  `upstring` varchar(260) NOT NULL COMMENT ''\n")
            .append(") ENGINE=MyIsam DEFAULT CHARSET=utf8;\n");
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
    
    @Override
    public int uploadCollection(Collection strings, @NotNull String dbPointTableName) {
        this.collection = strings;
        this.dbName = dbPointTableName;
        
        int resultsUpload = 0;
        if (!dbPointTableName.contains(".")) {
            dbPointTableName = DBNAME_VELKOM_POINT + dbPointTableName;
        }
        String dbName = dbPointTableName.split("\\Q.\\E")[0];
        String tableName = dbPointTableName.split("\\Q.\\E")[1];
        final String insertTo = String.format("INSERT INTO `%s`.`%s` (`upstring`, `stamp`) VALUES (?,?);", dbName, tableName);
        MysqlDataSource source = getDataSource();
        source.setDatabaseName(dbName);
        source.setContinueBatchOnError(true);
        
        List<String> colList = new ArrayList<>(strings);
        try (Connection connection = source.getConnection()) {
            
            try (PreparedStatement preparedStatementInsert = connection.prepareStatement(insertTo)) {
                for (String s : colList) {
                    if (s.length() > 259) {
                        s = s.substring(s.length() - (s.length() - 261));
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
                return createTable();
            }
        }
        return resultsUpload;
    }
}
