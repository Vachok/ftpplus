// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.FilePropsLocal;

import java.sql.*;
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
    
    
    private static final Properties APP_PROPS = new FilePropsLocal(ConstantsFor.class.getSimpleName()).getProps();
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, RegRuMysqlLoc.class.getSimpleName());
    
    private String dbName;
    
    private MysqlDataSource mysqlDataSource;
    
    @Contract(pure = true)
    public RegRuMysqlLoc(String dbName) {
        this.dbName = dbName;
        this.mysqlDataSource = getDataSource();
        mysqlDataSource.setUser(ConstantsFor.DB_USER);
        mysqlDataSource.setPassword(getFromLocalDB());
    }
    
    @NotNull MysqlDataSource getDataSourceLoc() {
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
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat
                .format("RegRuMysqlLoc.getDataSourceLoc\n{0}: {1}\nParameters: [dbName]\nReturn: com.mysql.jdbc.jdbc2.optional.MysqlDataSource\nStack:\n{2}", e
                    .getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        this.mysqlDataSource = defDataSource;
        return defDataSource;
    }
    
    private String getFromLocalDB() {
        DataConnectTo locConTo = DataConnectTo.getDefaultI();
        final String sql = "SELECT * FROM velkom.props_ro";
        try (Connection connection = locConTo.getDefaultConnection("velkom.props")) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    while (resultSet.next()) {
                        String passS = resultSet.getString(ConstantsFor.DBCOL_VALUEOFPROPERTY);
                        if (!passS.equals(ConstantsFor.DB_USER)) {
                            stringBuilder.append(passS);
                        }
                    }
                    return stringBuilder.toString();
                }
            }
        }
        catch (SQLException e) {
            return MessageFormat.format("RegRuMysqlLoc.getPassLocalDB\n{0}:\n{1}", e.getMessage(), new TForms().exceptionNetworker(e.getStackTrace()));
        }
    }
    
    @Override
    public boolean dropTable(String dbPointTable) {
        throw new TODOException("ru.vachok.networker.restapi.database.RegRuMysqlLoc.dropTable( boolean ) at 20.09.2019 - (20:37)");
    }
    
    @Override
    public MysqlDataSource getDataSource() {
        MysqlDataSource defDataSource = getDataSourceLoc();
        APP_PROPS.setProperty(PropertiesNames.DBUSER, ConstantsFor.DB_USER);
        APP_PROPS.setProperty(PropertiesNames.DBPASS, getFromLocalDB());
        
        this.mysqlDataSource = defDataSource;
        return defDataSource;
    }
    
    @Override
    public Connection getDefaultConnection(String dbName) {
        MysqlDataSource defDataSource = new MysqlDataSource();
        defDataSource.setServerName(OtherKnownDevices.SRVMYSQL_HOME);
        defDataSource.setPort(3306);
        defDataSource.setPassword(APP_PROPS.getProperty(PropertiesNames.DBPASS));
        defDataSource.setUser(APP_PROPS.getProperty(PropertiesNames.DBUSER));
        defDataSource.setEncoding("UTF-8");
        defDataSource.setCharacterEncoding("UTF-8");
        if (dbName.contains(".")) {
            throw new InvokeIllegalException(MessageFormat.format("Database name must me without comma! {0}\n{1}", dbName, this.getClass().getTypeName()));
        }
        defDataSource.setDatabaseName(dbName);
        defDataSource.setUseSSL(false);
        defDataSource.setVerifyServerCertificate(false);
        defDataSource.setAutoClosePStmtStreams(true);
        defDataSource.setAutoReconnect(true);
        defDataSource.setCreateDatabaseIfNotExist(true);
        try {
            this.mysqlDataSource = defDataSource;
            return defDataSource.getConnection();
        }
        catch (SQLException e) {
            messageToUser.error("RegRuMysqlLoc.getDefaultConnection", e.getMessage(), TForms.exceptionNetworker(e.getStackTrace()));
    
            return conAlt();
        }
    }
    
    private @Nullable Connection conAlt() {
        String dbPas = getFromLocalDB();
        UsefulUtilities.setPreference(PropertiesNames.DBPASS, dbPas);
        APP_PROPS.setProperty(PropertiesNames.DBPASS, dbPas);
        
        String dbUser = ConstantsFor.DB_USER;
        UsefulUtilities.setPreference(PropertiesNames.DBUSER, dbUser);
        APP_PROPS.setProperty(PropertiesNames.DBUSER, dbUser);
        DataConnectTo dataI = DataConnectTo.getDefaultI();
        MysqlDataSource source = dataI.getDataSource();
        source.setDatabaseName(ConstantsFor.STR_VELKOM);
        
        try {
            return source.getConnection();
        }
        catch (SQLException e) {
            messageToUser.error("RegRuMysqlLoc.conAlt", e.getMessage(), new TForms().exceptionNetworker(e.getStackTrace()));
            return null;
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