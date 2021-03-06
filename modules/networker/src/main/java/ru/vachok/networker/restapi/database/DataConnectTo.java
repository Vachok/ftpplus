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

    String FIREBASE = "Firebase";

    String REGRUCONNECTION = "RegRuMysqlLoc";

    @NotNull
    @Contract(value = " -> new", pure = true)
    static DataConnectTo getRemoteReg() {
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

    @NotNull
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    static ru.vachok.networker.restapi.database.DataConnectTo getInstance(@NotNull String type) {
        switch (type) {
            case REGRUCONNECTION:
                return new RegRuMysqlLoc();
            case ConstantsFor.DBBASENAME_U0466446_PROPERTIES:
                return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
            case TESTING:
                return new TesterDB65SQL();
            case FIREBASE:
                return new FirebaseRealTime();
            case DEFAULT_I:
            default:
                return getDefaultI();
        }
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    static DataConnectTo getDefaultI() {
        return new MySqlLocalSRVInetStat();
    }
}
