package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsNet;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 @see MySqlLocalSRVInetStatTest
 @since 04.09.2019 (16:42) */
class MySqlLocalSRVInetStat implements DataConnectTo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, MySqlLocalSRVInetStat.class.getSimpleName());
    
    private String name;
    
    private Collection collection;
    
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
        retSource.setCreateDatabaseIfNotExist(true);
        retSource.setContinueBatchOnError(true);
        retSource.setCreateDatabaseIfNotExist(true);
        return retSource;
    }
    
    @Override
    public int uploadFileTo(Collection strings, @NotNull String dbPointTableName) {
        this.collection = strings;
        this.name = dbPointTableName;
        
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
            createTable(source.getConnection(), dbPointTableName);
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
                messageToUser.error(e.getMessage() + " see line: 122");
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("MySqlLocalSRVInetStat.uploadFileTo: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return resultsUpload;
    }
    
    @Override
    public Connection getDefaultConnection(String dbName) {
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
            return defDataSource.getConnection();
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 95");
            FileSystemWorker.error(getClass().getSimpleName() + ".getDefaultConnection", e);
        }
        return DataConnectTo.getInstance(DataConnectTo.LIB_REGRU).getDefaultConnection(dbName);
    }
    
    private void createTable(@NotNull Connection connection, String tableName) {
        final String[] createTable = getCreateQuery(tableName);
        for (String query : createTable) {
            try (PreparedStatement preparedStatementTable = connection.prepareStatement(query)) {
                System.out.println(" = " + preparedStatementTable.executeUpdate());
            }
            catch (SQLException e) {
                messageToUser.error(e.getMessage() + " see line: 133");
                messageToUser.error("Dropping table " + tableName);
                dropTable(tableName);
            }
        }
    }
    
    private void dropTable(String tableName) {
        MysqlDataSource source = getDataSource();
        try (Connection connection = source.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(String.format("drop table %s", tableName))) {
            preparedStatement.executeUpdate();
        }
        catch (MySQLSyntaxErrorException e) {
            messageToUser.error(e.getMessage() + " see line: 151");
            messageToUser.error("Table: " + tableName + " was dropped!");
            uploadFileTo(this.collection, this.name);
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 153");
        }
    }
}
