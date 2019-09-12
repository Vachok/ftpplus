package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ConstantsNet;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.nio.file.Path;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Queue;


/**
 @see MySqlLocalSRVInetStatTest
 @since 04.09.2019 (16:42) */
class MySqlLocalSRVInetStat implements DataConnectTo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, MySqlLocalSRVInetStat.class.getSimpleName());
    
    private static final String DBNAME_VELKOM_POINT = "velkom.";
    
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
        try {
            return defDataSource.getConnection();
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 95");
            FileSystemWorker.error(getClass().getSimpleName() + ".getDefaultConnection", e);
        }
        return DataConnectTo.getInstance(DataConnectTo.LIB_REGRU).getDefaultConnection(dbName);
    }
    
    @Override
    public int uploadFileTo(Path filePath, String tableName) {
        int resultsUpload = 0;
        MysqlDataSource source = getDataSource();
        source.setDatabaseName(ConstantsFor.STR_VELKOM);
        try (Connection connection = source.getConnection()) {
            createTable(connection, tableName);
            String insertTo = String.format("INSERT INTO `velkom`.`%s` (`upstring`, `stamp`) VALUES (?,?);", tableName);
            try (PreparedStatement preparedStatementInsert = connection.prepareStatement(insertTo)) {
                Queue<String> strings = FileSystemWorker.readFileEncodedToQueue(filePath.toAbsolutePath().normalize(), "UTF-8");
                for (String s : strings) {
                    if (s.length() > 259) {
                        s = s.substring(s.length() - (s.length() - 261));
                    }
                    preparedStatementInsert.setString(1, s);
                    preparedStatementInsert.setLong(2, System.currentTimeMillis());
                    resultsUpload += preparedStatementInsert.executeUpdate();
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("MySqlLocalSRVInetStat.uploadFileTo: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return resultsUpload;
    }
    
    private void createTable(@NotNull Connection connection, String tableName) {
        final String[] createTable = getCreateQuery(tableName);
        for (String query : createTable) {
            try (PreparedStatement preparedStatementTable = connection.prepareStatement(query)) {
                System.out.println(" = " + preparedStatementTable.executeUpdate());
            }
            catch (SQLException e) {
                messageToUser.error(e.getMessage() + " see line: 97");
            }
        }
    }
    
    private String[] getCreateQuery(String tableName) {
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilder1 = new StringBuilder();
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder.append("CREATE TABLE IF NOT EXISTS ")
                .append(DBNAME_VELKOM_POINT)
                .append(tableName)
                .append("(\n")
                .append("  `idrec` mediumint(11) unsigned NOT NULL COMMENT '',\n")
                .append("  `stamp` bigint(13) unsigned NOT NULL COMMENT '',\n")
                .append("  `upstring` varchar(260) NOT NULL COMMENT ''\n")
                .append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n");
        
        stringBuilder2.append(ConstantsFor.SQL_ALTERTABLE)
                .append(DBNAME_VELKOM_POINT)
                .append(tableName)
                .append("\n")
                .append("  ADD PRIMARY KEY (`idrec`);\n");
        
        stringBuilder1.append(ConstantsFor.SQL_ALTERTABLE).append(tableName).append("\n")
                .append("  MODIFY `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '';").toString();
        return new String[]{stringBuilder.toString(), stringBuilder2.toString(), stringBuilder1.toString()
        };
    }
    
}
