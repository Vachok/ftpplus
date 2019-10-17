package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.SQLException;


/**
 @see TesterDB65SQLTest
 @since 05.10.2019 (16:27) */
public class TesterDB65SQL extends MySqlLocalSRVInetStat {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, TesterDB65SQL.class.getSimpleName());
    
    @Override
    public Connection getDefaultConnection(String dbName) {
        MysqlDataSource sourceT = getDataSource();
        sourceT.setDatabaseName(dbName);
        Connection connection;
        try {
            messageToUser.info(sourceT.getServerName());
            connection = sourceT.getConnection();
        }
        catch (SQLException e) {
            messageToUser.error(TesterDB65SQL.class.getSimpleName(), e.getMessage(), " see line: 42 ***");
            connection = super.getDefaultConnection(dbName);
        }
        return connection;
    }
    
    @Override
    public MysqlDataSource getDataSource() {
        MysqlDataSource source = new MysqlDataSource();
        source.setServerName(OtherKnownDevices.SRVMYSQL_HOME);
        source.setCreateDatabaseIfNotExist(true);
        source.setUser("kudr");
        source.setPassword("36e42yoak8");
        return source;
    }
}