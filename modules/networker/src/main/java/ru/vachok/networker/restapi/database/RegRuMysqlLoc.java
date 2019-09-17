// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.FilePropsLocal;

import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 Class ru.vachok.networker.restapi.database.RegRuMysql
 <p>
 
 @see ru.vachok.networker.restapi.database.RegRuMysqlLocTest
 @since 14.07.2019 (12:16) */
class RegRuMysqlLoc implements DataConnectTo {
    
    
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
        this.mysqlDataSource = defDataSource;
        return defDataSource;
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
            this.mysqlDataSource = defDataSource;
            return defDataSource.getConnection();
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 95");
            FileSystemWorker.error(getClass().getSimpleName() + ".getDefaultConnection", e);
        }
        this.mysqlDataSource = DataConnectTo.getInstance(DataConnectTo.LIB_REGRU).getDataSource();
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
                .add("dbURL = '" + mysqlDataSource.getURL() + "'")
                .toString();
    }
    
}