// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.Driver;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsNet;
import ru.vachok.networker.componentsrepo.data.enums.PropertiesNames;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.FilePropsLocal;

import java.sql.Connection;
import java.sql.DriverManager;
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
public class RegRuMysqlLoc implements DataConnectTo {
    
    
    private static final Properties APP_PROPS = new FilePropsLocal(ConstantsFor.class.getSimpleName()).getProps();
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, RegRuMysqlLoc.class.getSimpleName());
    
    private String dbName;
    
    @Contract(pure = true)
    public RegRuMysqlLoc(String dbName) {
        this.dbName = dbName;
    }
    
    static {
        try {
            Driver driver = new com.mysql.jdbc.Driver();
            DriverManager.registerDriver(driver);
        }
        catch (SQLException e) {
            messageToUser.error(FileSystemWorker.error(RegRuMysqlLoc.class.getSimpleName() + ConstantsFor.STATIC_INITIALIZER, e));
        }
        
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
    public Connection getDefaultConnection(String dbName) {
        MysqlDataSource defDataSource = new MysqlDataSource();
        defDataSource.setServerName(ConstantsNet.REG_RU_SERVER);
        defDataSource.setPort(3306);
        defDataSource.setPassword(APP_PROPS.getProperty(PropertiesNames.DBPASS));
        defDataSource.setUser(APP_PROPS.getProperty(PropertiesNames.DBUSER));
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
    public Savepoint getSavepoint(Connection connection) {
        throw new UnsupportedOperationException("14.07.2019 (16:17)");
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", RegRuMysqlLoc.class.getSimpleName() + "[\n", "\n]")
                .add("dbName = '" + dbName + "'")
                .toString();
    }
    
    private @NotNull MysqlDataSource getDataSourceLoc(String dbName) {
        this.dbName = dbName;
        MysqlDataSource defDataSource = new MysqlDataSource();
        defDataSource.setServerName(ConstantsNet.REG_RU_SERVER);
        defDataSource.setPassword(APP_PROPS.getProperty(PropertiesNames.DBPASS));
        defDataSource.setUser(APP_PROPS.getProperty(PropertiesNames.DBUSER));
        defDataSource.setEncoding("UTF-8");
        defDataSource.setCharacterEncoding("UTF-8");
        defDataSource.setDatabaseName(dbName);
        defDataSource.setUseSSL(false);
        defDataSource.setVerifyServerCertificate(false);
        defDataSource.setContinueBatchOnError(true);
        defDataSource.setAutoReconnect(true);
        defDataSource.setCachePreparedStatements(true);
        try {
            defDataSource.setLoginTimeout(5);
            defDataSource.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(60));
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat
                    .format("RegRuMysqlLoc.getDataSourceLoc\n{0}: {1}\nParameters: [dbName]\nReturn: com.mysql.jdbc.jdbc2.optional.MysqlDataSource\nStack:\n{2}", e
                            .getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        return defDataSource;
    }
    
    private @NotNull MysqlDataSource tuneDataSource() {
        InitProperties initProperties = new FileProps(ConstantsFor.class.getSimpleName());
        Properties props = initProperties.getProps();
        MysqlDataSource dataSource = new MysqlDataSource();
    
        dataSource.setUser(props.getProperty(PropertiesNames.DBUSER));
        dataSource.setPassword(props.getProperty(PropertiesNames.DBPASS));
        
        dataSource.setUseInformationSchema(true);
        dataSource.setRequireSSL(false);
        dataSource.setUseSSL(false);
        
        dataSource.setEncoding("UTF-8");
        dataSource.setRelaxAutoCommit(true);
        dataSource.setContinueBatchOnError(true);
        dataSource.setCreateDatabaseIfNotExist(true);
        
        try {
            dataSource.setLoginTimeout(5);
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("RegRuMysqlLoc.tuneDataSource says: {0}. Parameters: \n[]: {1}", e.getMessage(), new TForms().fromArray(e)));
        }
        dataSource.setInteractiveClient(true);
        
        dataSource.setCachePreparedStatements(true);
        dataSource.setCachePrepStmts(true);
        dataSource.setCacheCallableStatements(true);
        dataSource.setCacheResultSetMetadata(true);
        dataSource.setCacheServerConfiguration(true);
        
        dataSource.setCacheDefaultTimezone(true);
        dataSource.setAutoClosePStmtStreams(true);
        dataSource.setAutoReconnect(true);
        return dataSource;
    }
}