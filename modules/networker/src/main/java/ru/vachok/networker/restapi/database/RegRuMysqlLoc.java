// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.FakeConnection;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;


/**
 Class ru.vachok.networker.restapi.database.RegRuMysql
 <p>
 
 @see ru.vachok.networker.restapi.database.RegRuMysqlLocTest
 @since 14.07.2019 (12:16) */
public class RegRuMysqlLoc extends RegRuMysql {
    
    
    private static final Properties APP_PROPS = AppComponents.getProps();
    
    private static MysqlDataSource dataSource = new MysqlDataSource();
    
    private static MessageToUser messageToUser = new MessageLocal(RegRuMysqlLoc.class.getSimpleName());
    
    @Override
    public Connection getDefaultConnection(String dbName) {
        Connection connection = new FakeConnection();
        try {
            return tuneDataSource().getConnection();
        }
        catch (SQLException e) {
            messageToUser
                .error(MessageFormat.format("RegRuMysqlLoc.getDefaultConnection says: {0}. Parameters: \n[dbName]: {1}", e.getMessage(), new TForms().fromArray(e)));
        }
        return connection;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", RegRuMysqlLoc.class.getSimpleName() + "[\n", "\n]")
            .add("dataSource = " + dataSource.getServerName())
            .toString();
    }
    
    private static MysqlDataSource tuneDataSource() {
        dataSource.setUser(APP_PROPS.getProperty("dbuser"));
        dataSource.setPassword(APP_PROPS.getProperty("dbpass"));
        
        dataSource.setUseInformationSchema(true);
        dataSource.setRequireSSL(false);
        dataSource.setUseSSL(false);
        
        dataSource.setEncoding("UTF-8");
        dataSource.setRelaxAutoCommit(true);
        
        try {
            dataSource.setLoginTimeout(10);
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("RegRuMysqlLoc.tuneDataSource says: {0}. Parameters: \n[]: {1}", e.getMessage(), new TForms().fromArray(e)));
        }
        try {
            dataSource.setConnectTimeout(Math.toIntExact(TimeUnit.SECONDS.toMillis(5)));
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
        }
        dataSource.setInteractiveClient(true);
        dataSource.setEnableQueryTimeouts(true);
        
        dataSource.setCachePreparedStatements(true);
        dataSource.setCacheCallableStatements(true);
        dataSource.setCacheResultSetMetadata(true);
        dataSource.setCacheServerConfiguration(true);
        dataSource.setMaintainTimeStats(true);
        dataSource.setUseReadAheadInput(true);
        dataSource.setAutoSlowLog(true);
        
        dataSource.setCacheDefaultTimezone(true);
        dataSource.setAutoClosePStmtStreams(true);
        dataSource.setAutoReconnect(true);
        return dataSource;
    }
}