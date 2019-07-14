// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.MessageFormat;


/**
 @since 14.07.2019 (12:15) */
public interface DataConnectTo extends ru.vachok.mysqlandprops.DataConnectTo {
    
    
    MessageToUser messageToUser = new MessageLocal(DataConnectTo.class.getTypeName());
    
    MysqlDataSource getDataSourceLoc(String dbName);
    
    @Override
    default void setSavepoint(Connection connection) {
        throw new InvokeEmptyMethodException("14.07.2019 (15:44)");
    }
    
    @Override
    default MysqlDataSource getDataSource() {
        return getDataSourceLoc(ConstantsFor.DBNAME_WEBAPP);
    }
    
    @Override
    default Savepoint getSavepoint(Connection connection) {
        throw new InvokeEmptyMethodException("14.07.2019 (15:45)");
    }
    
    @Override
    default Connection getDefaultConnection(String dbName) {
        try {
            return getDataSourceLoc(dbName).getConnection();
        }
        catch (SQLException e) {
            messageToUser
                .error(MessageFormat
                    .format("DataConnectTo.getDefaultConnection says: {0}. Parameters: \n[dbName]: {1}", e.getMessage(), new TForms().fromArray(e)));
            return new ru.vachok.mysqlandprops.RegRuMysql().getDefaultConnection(dbName);
        }
    }
}
