// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.prefs.Preferences;


/**
 @since 14.07.2019 (12:15) */
public interface DataConnectTo extends ru.vachok.mysqlandprops.DataConnectTo {
    
    
    MessageToUser messageToUser = new MessageLocal(DataConnectTo.class.getTypeName());
    
    @Override
    default void setSavepoint(Connection connection) {
        throw new InvokeEmptyMethodException("14.07.2019 (15:44)");
    }
    
    @Override
    default MysqlDataSource getDataSource() {
        return new RegRuMysqlLoc().getDataSource();
    }
    
    @Override
    default Connection getDefaultConnection(String dbName) {
        Preferences pref = AppComponents.getUserPref();
        String methName = ".getDefaultConnection";
    
        Connection connection = new RegRuMysqlLoc().anotherConnect(dbName);
    
        try {
            MysqlDataSource schema = new RegRuMysql().getDataSourceSchema(dbName);
            connection = schema.getConnection();
        }
        catch (SQLException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + methName, e));
        }
        MysqlDataSource defDataSource = new MysqlDataSource();
        defDataSource.setServerName(ConstantsNet.REG_RU_SERVER);
        defDataSource.setPassword(pref.get(ConstantsFor.PR_DBPASS, ""));
        defDataSource.setUser(pref.get(ConstantsFor.PR_DBUSER, "u0466446_kudr"));
        defDataSource.setEncoding("UTF-8");
        defDataSource.setCharacterEncoding("UTF-8");
        defDataSource.setDatabaseName(dbName);
        defDataSource.setUseSSL(false);
        defDataSource.setVerifyServerCertificate(false);
        defDataSource.setAutoClosePStmtStreams(true);
        try {
            connection = defDataSource.getConnection();
        }
        catch (SQLException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + methName, e));
        }
        return connection;
    }
    
    @Override
    default Savepoint getSavepoint(Connection connection) {
        throw new InvokeEmptyMethodException("14.07.2019 (15:45)");
    }
}
