package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;

import java.sql.Connection;
import java.sql.Savepoint;


/**
 @since 15.07.2019 (11:05)
 @see ru.vachok.networker.restapi.database.DataConnectToAdapterTest
 */
public abstract class DataConnectToAdapter implements DataConnectTo {
    
    
    private static String dbName;
    
    public static void setDbName(String dbName) {
        DataConnectToAdapter.dbName = dbName;
    }
    
    public static Connection getRegRuMysqlLibConnection(String dbName) {
        DataConnectTo regRuMysql = new RegRuMysql();
        return regRuMysql.getDefaultConnection(dbName);
    }
    
    public static DataConnectToAdapter getI(String name) {
        ru.vachok.mysqlandprops.DataConnectTo dataConnectTo = new RegRuMysql();
        return (DataConnectToAdapter) dataConnectTo;
    }
    
    @Override
    public void setSavepoint(Connection connection) {
        throw new InvokeEmptyMethodException("16.07.2019 (17:02)");
    }
    
    @Override
    public MysqlDataSource getDataSource() {
        return null;
    }
    
    @Override
    public abstract Savepoint getSavepoint(Connection connection);
    
}
