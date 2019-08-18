// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;

import java.sql.Connection;
import java.sql.Savepoint;


/**
 @since 14.07.2019 (12:15) */
public interface DataConnectTo extends ru.vachok.mysqlandprops.DataConnectTo {
    
    String DBUSER_KUDR = "u0466446_kudr";
    
    String DBUSER_NETWORK = "u0466446_network";
    
    @Override
    default void setSavepoint(Connection connection) {
        throw new InvokeEmptyMethodException("14.07.2019 (15:44)");
    }
    
    @Override
    MysqlDataSource getDataSource();
    
    @Override
    Connection getDefaultConnection(String dbName);
    
    @Override
    default Savepoint getSavepoint(Connection connection) {
        throw new InvokeEmptyMethodException("14.07.2019 (15:45)");
    }
}
