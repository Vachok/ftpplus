// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ConstantsNet;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.FilePropsLocal;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;


/**
 Class ru.vachok.networker.restapi.database.RegRuMysql
 <p>
 
 @see ru.vachok.networker.restapi.database.RegRuMysqlLocTest
 @since 14.07.2019 (12:16) */
class RegRuMysqlLoc implements DataConnectTo {
    
    
    @Override
    public boolean dropTable(String dbPointTable) {
        throw new TODOException("ru.vachok.networker.restapi.database.RegRuMysqlLoc.dropTable( boolean ) at 20.09.2019 - (20:37)");
    }
    
    private static final Properties APP_PROPS = new FilePropsLocal(ConstantsFor.class.getSimpleName()).getProps();
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, RegRuMysqlLoc.class.getSimpleName());
    
    private String dbName;
    
    private MysqlDataSource mysqlDataSource;
    
    @Contract(pure = true)
    public RegRuMysqlLoc(String dbName) {
        this.dbName = dbName;
        this.mysqlDataSource = getDataSource();
    }
    
    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        throw new TODOException("ru.vachok.networker.restapi.database.RegRuMysqlLoc.uploadCollection created 13.09.2019 (16:35)");
    }
    
    @Override
    public MysqlDataSource getDataSource() {
        MysqlDataSource defDataSource = getDataSourceLoc();
        defDataSource.setPassword(APP_PROPS.getProperty(PropertiesNames.DBPASS));
        defDataSource.setUser(APP_PROPS.getProperty(PropertiesNames.DBUSER));
        return defDataSource;
    }
    
    @Override
    public void setSavepoint(Connection connection) {
        throw new UnsupportedOperationException("14.07.2019 (16:17)");
    }
    
    private @NotNull MysqlDataSource getDataSourceLoc() {
        MysqlDataSource defDataSource = new MysqlDataSource();
        defDataSource.setServerName(ConstantsNet.REG_RU_SERVER);
        defDataSource.setEncoding("UTF-8");
        defDataSource.setCharacterEncoding("UTF-8");
        defDataSource.setDatabaseName(dbName);
        defDataSource.setUseSSL(false);
        defDataSource.setVerifyServerCertificate(false);
        defDataSource.setContinueBatchOnError(true);
        defDataSource.setAutoReconnect(true);
        defDataSource.setCachePreparedStatements(true);
        defDataSource.setPassword(APP_PROPS.getProperty(PropertiesNames.DBPASS));
        defDataSource.setUser(APP_PROPS.getProperty(PropertiesNames.DBUSER));
        try {
            defDataSource.setLoginTimeout(5);
            defDataSource.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(60));
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat
                    .format("RegRuMysqlLoc.getDataSourceLoc\n{0}: {1}\nParameters: [dbName]\nReturn: com.mysql.jdbc.jdbc2.optional.MysqlDataSource\nStack:\n{2}", e
                            .getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        this.mysqlDataSource = defDataSource;
        return defDataSource;
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
            this.mysqlDataSource = defDataSource;
            return defDataSource.getConnection();
        }
        catch (SQLException e) {
            messageToUser.error(this.getClass().getSimpleName(), e.getMessage(), " see line: 95");
            FileSystemWorker.error(getClass().getSimpleName() + ".getDefaultConnection", e);
            return conAlt();
        }
    
    
    }
    
    private Connection conAlt() {
        MysqlDataSource source = DataConnectTo.getDefaultI().getDataSource();
        source.setDatabaseName(dbName);
        Connection connection = null;
        try {
            connection = source.getConnection();
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("RegRuMysqlLoc.conAlt {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms()
                .exceptionNetworker(e.getStackTrace())));
        }
        return connection;
    }
    
    @Override
    public Savepoint getSavepoint(Connection connection) {
        throw new UnsupportedOperationException("14.07.2019 (16:17)");
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", RegRuMysqlLoc.class.getSimpleName() + "[\n", "\n]")
                .add("dbName = '" + dbName + "'")
                .add("dbURL = '" + mysqlDataSource.getURL() + "'")
                .toString();
    }
    
}