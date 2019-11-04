// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.sql.Connection;
import java.sql.Savepoint;
import java.util.Collection;
import java.util.List;


/**
 @see DataConnectToTest
 @since 14.07.2019 (12:15) */
public interface DataConnectTo extends ru.vachok.mysqlandprops.DataConnectTo {
    
    
    String DBNAME_VELKOM_POINT = "velkom.";
    
    String DEFAULT_I = "loc";
    
    String TESTING = "testing";
    
    String H2DB = "H2DB";
    
    @Contract(value = " -> new", pure = true)
    static @NotNull DataConnectTo getRemoteReg() {
        return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    }
    
    @Override
    default void setSavepoint(Connection connection) {
        throw new UnsupportedOperationException("14.07.2019 (15:44)");
    }
    
    @Override
    MysqlDataSource getDataSource();
    
    @Override
    default Savepoint getSavepoint(Connection connection) {
        throw new UnsupportedOperationException("14.07.2019 (15:45)");
    }
    
    int uploadCollection(Collection stringsCollection, String tableName);
    
    boolean dropTable(String dbPointTable);
    
    /**
     @param dbPointTable dbname.table
     @param additionalColumns unstandart column names <b>with type</b>
     @return executeQuery
     */
    int createTable(String dbPointTable, List<String> additionalColumns);
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    static @NotNull ru.vachok.networker.restapi.database.DataConnectTo getInstance(@NotNull String type) {
        switch (type) {
            case DEFAULT_I:
                return getDefaultI();
            case ConstantsFor.DBBASENAME_U0466446_PROPERTIES:
                return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
            case TESTING:
                return new TesterDB65SQL();
            case H2DB:
                return new H2DB();
            default:
                return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        }
    }
    
    @Contract(value = " -> new", pure = true)
    static @NotNull DataConnectTo getDefaultI() {
        return new MySqlLocalSRVInetStat();
    }
}
