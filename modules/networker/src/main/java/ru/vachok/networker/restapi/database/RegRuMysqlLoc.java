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
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
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
    
    private String dbName;
    
    @Override
    public MysqlDataSource getDataSourceSchema(String schemaName) {
        throw new InvokeEmptyMethodException("14.07.2019 (16:17)");
    }
    
    @Override
    public void setSavepoint(Connection connection) {
        throw new UnsupportedOperationException("14.07.2019 (16:17)");
    }
    
    @Override
    public MysqlDataSource getDataSource() {
        return getDataSourceLoc(dbName);
    }
    
    @Override
    public Savepoint getSavepoint(Connection connection) {
        throw new UnsupportedOperationException("14.07.2019 (16:17)");
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", RegRuMysqlLoc.class.getSimpleName() + "[\n", "\n]")
            .add("dbName = '" + dbName + "'")
            .toString();
    }
    
    @Override
    public Connection getDefaultConnection(String dbName) {
        this.dbName = dbName;
        try {
            return getDataSourceLoc(dbName).getConnection();
        }
        catch (SQLException e) {
            return anotherConnect(e);
        }
    }
    
    private Connection anotherConnect(Exception e) {
        try {
            return new RegRuMysql().getDataSourceSchema(dbName).getConnection();
        }
        catch (SQLException e1) {
            String methName = ".anotherConnect";
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + methName, e));
    
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + methName, e1));
            return new FakeConnection();
        }
    }
    
    private MysqlDataSource tuneDataSource() {
        InitProperties initProperties = new FileProps(ConstantsFor.class.getSimpleName());
        Properties props = initProperties.getProps();
        dataSource.setUser(props.getProperty(ConstantsFor.PR_DBUSER));
        dataSource.setPassword(props.getProperty(ConstantsFor.PR_DBPASS));
        
        dataSource.setUseInformationSchema(true);
        dataSource.setRequireSSL(false);
        dataSource.setUseSSL(false);
        
        dataSource.setEncoding("UTF-8");
        dataSource.setRelaxAutoCommit(true);
        
        try {
            dataSource.setLoginTimeout(5);
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("RegRuMysqlLoc.tuneDataSource says: {0}. Parameters: \n[]: {1}", e.getMessage(), new TForms().fromArray(e)));
        }
        dataSource.setInteractiveClient(true);
        
        dataSource.setCachePreparedStatements(true);
        dataSource.setCacheCallableStatements(true);
        dataSource.setCacheResultSetMetadata(true);
        dataSource.setCacheServerConfiguration(true);
        
        dataSource.setCacheDefaultTimezone(true);
        dataSource.setAutoClosePStmtStreams(true);
        dataSource.setAutoReconnect(true);
        return dataSource;
    }
    
    private MysqlDataSource getDataSourceLoc(String dbName) {
        this.dbName = dbName;
        String methName = ".getDataSourceLoc";
    
        MysqlDataSource defDataSource = new MysqlDataSource();
        MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
        defDataSource.setServerName("server202.hosting.reg.ru");
        defDataSource.setPassword(ConstantsFor.PR_DBPASS);
        defDataSource.setUser(ConstantsFor.PR_DBUSER);
        defDataSource.setEncoding("UTF-8");
        defDataSource.setCharacterEncoding("UTF-8");
        defDataSource.setDatabaseName(dbName);
        defDataSource.setUseSSL(false);
        defDataSource.setRelaxAutoCommit(true);
        defDataSource.setVerifyServerCertificate(false);
        defDataSource.setAutoClosePStmtStreams(true);
        defDataSource.setContinueBatchOnError(true);
    
        try {
            defDataSource.setLoginTimeout(5);
        }
        catch (SQLException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + methName, e));
        }
        try {
            dataSource.setSocketTimeout((int) TimeUnit.SECONDS.toMillis(ConstantsFor.DELAY));
        }
        catch (SQLException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + methName, e));
        }
        return defDataSource;
    }
}