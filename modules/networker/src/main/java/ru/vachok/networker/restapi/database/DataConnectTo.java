// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.sql.Connection;
import java.sql.Savepoint;
import java.util.Collection;


/**
 @see DataConnectToTest
 @since 14.07.2019 (12:15) */
public interface DataConnectTo extends ru.vachok.mysqlandprops.DataConnectTo {
    
    
    String DBNAME_VELKOM_POINT = "velkom.";
    
    String DBUSER_KUDR = "u0466446_kudr";
    
    String DBUSER_NETWORK = "u0466446_network";
    
    String LIB_REGRU = "RegRuMysql";
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    static ru.vachok.mysqlandprops.DataConnectTo getInstance(@NotNull String type) {
        switch (type) {
            case ConstantsFor.DBBASENAME_U0466446_PROPERTIES:
                return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
            case ConstantsFor.DBBASENAME_U0466446_WEBAPP:
                return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_WEBAPP);
            case LIB_REGRU:
                return new RegRuMysql();
            case ConstantsFor.DBBASENAME_U0466446_TESTING:
                return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_TESTING);
            default:
                return getDefaultI();
        }
    }
    
    @Contract(value = " -> new", pure = true)
    static @NotNull DataConnectTo getDefaultI() {
        return new MySqlLocalSRVInetStat();
    }
    
    int uploadCollection(Collection stringsCollection, String tableName);
    
    @Override
    default void setSavepoint(Connection connection) {
        throw new InvokeEmptyMethodException("14.07.2019 (15:44)");
    }
    
    @Override
    MysqlDataSource getDataSource();
    
    @Override
    default Savepoint getSavepoint(Connection connection) {
        throw new InvokeEmptyMethodException("14.07.2019 (15:45)");
    }
    
}
