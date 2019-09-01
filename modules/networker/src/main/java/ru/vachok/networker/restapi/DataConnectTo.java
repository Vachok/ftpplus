// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;

import java.sql.Connection;
import java.sql.Savepoint;


/**
 @since 14.07.2019 (12:15) */
public interface DataConnectTo extends ru.vachok.mysqlandprops.DataConnectTo {
    
    String DBUSER_KUDR = "u0466446_kudr";
    
    String DBUSER_NETWORK = "u0466446_network";
    
    static DataConnectTo getI(String type) {
        switch (type) {
            case ConstantsFor.DBBASENAME_U0466446_VELKOM:
                return getDefaultI();
            case ConstantsFor.DBBASENAME_U0466446_PROPERTIES:
                return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
            default:
                return getDefaultI();
        }
    }
    
    @Contract(value = " -> new", pure = true)
    static @NotNull DataConnectTo getDefaultI() {
        return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    }
    
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
