// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.sql.Connection;
import java.sql.Savepoint;


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
        return new RegRuMysqlLoc().getDefaultConnection(dbName);
    }
    
    @Override
    default Savepoint getSavepoint(Connection connection) {
        throw new InvokeEmptyMethodException("14.07.2019 (15:45)");
    }
}
