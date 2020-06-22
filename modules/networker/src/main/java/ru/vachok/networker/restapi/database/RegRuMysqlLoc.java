// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.FilePropsLocal;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.restapi.database.RegRuMysqlLocTest
 @since 14.07.2019 (12:16) */
class RegRuMysqlLoc implements DataConnectTo {


    private static final Properties APP_PROPS = new FilePropsLocal(ConstantsFor.class.getSimpleName()).getProps();

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, RegRuMysqlLoc.class.getSimpleName());

    private String dbName;

    private String tableName;

    private MysqlDataSource mysqlDataSource;

    @Contract(pure = true)
    RegRuMysqlLoc(String dbName) {
        this.dbName = dbName;
        this.mysqlDataSource = getDataSource();
        mysqlDataSource.setUser("it");
        mysqlDataSource.setPassword(APP_PROPS.getProperty(PropertiesNames.DBPASS));
    }

    private @NotNull MysqlDataSource getDataSourceLoc() {
        MysqlDataSource defDataSource = new MysqlDataSource();
        defDataSource.setServerName(OtherKnownDevices.REG_RU_SERVER);
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
            this.mysqlDataSource = defDataSource;
            return defDataSource;
        }
        catch (SQLException e) {
            messageToUser.error("RegRuMysqlLoc.getDataSourceLoc", e.getMessage(), new TForms().networkerTrace(e.getStackTrace()));
            return DataConnectToAdapter.getLibDataSource();
        }
    }

    @Contract(pure = true)
    RegRuMysqlLoc() {
        this.dbName = ConstantsFor.U0466446_DBPREFIX;
        this.tableName = ConstantsFor.STR_VELKOM;
    }

    private @Nullable Connection conAlt(@NotNull String dbName) {
        return new TesterDB65SQL().getDefaultConnection(dbName);
    }

    @Override
    public boolean dropTable(String dbPointTable) {
        throw new TODOException("ru.vachok.networker.restapi.database.RegRuMysqlLoc.dropTable( boolean ) at 20.09.2019 - (20:37)");
    }

    @Override
    public int createTable(String dbPointTable, List<String> additionalColumns) {
        throw new TODOException("ru.vachok.networker.restapi.database.RegRuMysqlLoc.createTable( int ) at 04.11.2019 - (13:50)");
    }

    @Override
    public MysqlDataSource getDataSource() {
        MysqlDataSource defDataSource = getDataSourceLoc();
        APP_PROPS.setProperty(PropertiesNames.DBUSER, ConstantsFor.DB_USER);
        this.mysqlDataSource = defDataSource;
        return defDataSource;
    }

    @Override
    public Connection getDefaultConnection(@NotNull String dbName) {
        this.dbName = dbName;
        MysqlDataSource defDataSource = new MysqlDataSource();
        defDataSource.setServerName(OtherKnownDevices.REG_RU_SERVER);
        defDataSource.setPort(3306);
        defDataSource.setPassword(APP_PROPS.getProperty(PropertiesNames.DBPASS));
        defDataSource.setUser(APP_PROPS.getProperty(PropertiesNames.DBUSER));
        defDataSource.setEncoding("UTF-8");
        defDataSource.setCharacterEncoding("UTF-8");
        if (dbName.contains(".")) {
            this.tableName = dbName.split("\\Q.\\E")[0];
        }
        defDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        defDataSource.setUseSSL(false);
        defDataSource.setVerifyServerCertificate(false);
        defDataSource.setAutoClosePStmtStreams(true);
        defDataSource.setAutoReconnect(true);
        defDataSource.setCreateDatabaseIfNotExist(true);
        try {
            this.mysqlDataSource = defDataSource;
            messageToUser.info(tableName, defDataSource.getUser(), defDataSource.getSessionVariables());
            return defDataSource.getConnection();
        }
        catch (SQLException e) {
            messageToUser.error("RegRuMysqlLoc.getDefaultConnection", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            return conAlt(dbName);
        }
    }

    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        throw new TODOException("ru.vachok.networker.restapi.database.RegRuMysqlLoc.uploadCollection created 13.09.2019 (16:35)");
    }

    @Override
    public Savepoint getSavepoint(Connection connection) {
        throw new UnsupportedOperationException("14.07.2019 (16:17)");
    }

    @Override
    public void setSavepoint(Connection connection) {
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