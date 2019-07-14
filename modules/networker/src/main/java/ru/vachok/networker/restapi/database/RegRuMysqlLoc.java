// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.FakeConnection;
import ru.vachok.networker.restapi.DataConnectTo;
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
public class RegRuMysqlLoc extends RegRuMysql implements DataConnectTo {
    
    
    private static final Properties APP_PROPS = AppComponents.getProps();
    
    private static MysqlDataSource dataSource = new MysqlDataSource();
    
    private static MessageToUser messageToUser = new MessageLocal(RegRuMysqlLoc.class.getSimpleName());
    
    @Override
    public Connection getDefaultConnection(String dbName) {
        try {
            return tuneDataSource().getConnection();
        }
        catch (SQLException e) {
            messageToUser
                .error(MessageFormat.format("RegRuMysqlLoc.getDefaultConnection says: {0}. Parameters: \n[dbName]: {1}", e.getMessage(), new TForms().fromArray(e)));
            return anotherConnect();
        }
    }
    
    private Connection anotherConnect() {
        try {
            return new RegRuMysql().getDataSourceSchema(ConstantsFor.DBBASENAME_U0466446_VELKOM).getConnection();
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("RegRuMysqlLoc.anotherConnect says: {0}. Parameters: \n[]: {1}", e.getMessage(), new TForms().fromArray(e)));
            return new FakeConnection();
        }
    }
    
    private static MysqlDataSource tuneDataSource() {
        InitProperties initProperties = new FileProps(ConstantsFor.class.getSimpleName());
    
        dataSource.setUser(initProperties.getProps().getProperty(ConstantsFor.PR_DBUSER));
        dataSource.setPassword(initProperties.getProps().getProperty(ConstantsFor.PR_DBPASS));
        
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
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", RegRuMysqlLoc.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}