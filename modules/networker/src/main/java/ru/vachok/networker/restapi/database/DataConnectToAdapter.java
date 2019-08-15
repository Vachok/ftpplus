// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.enums.PropertiesNames;

import java.sql.Connection;
import java.sql.Savepoint;
import java.util.Properties;


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
    
    public static @NotNull DataConnectToAdapter getI(String name) {
        ru.vachok.mysqlandprops.DataConnectTo dataConnectTo = new RegRuMysql();
        return (DataConnectToAdapter) dataConnectTo;
    }
    
    public static @NotNull MysqlDataSource getLibDataSource() {
        MysqlDataSource source = new MysqlDataSource();
        try {
            source = new RegRuMysql().getDataSource();
            source.setAutoReconnect(true);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            Properties props = AppComponents.getProps();
            source.setUser(props.getProperty(PropertiesNames.PR_DBUSER));
            source.setPassword(props.getProperty(PropertiesNames.PR_DBPASS));
        }
        return source;
    }
    
    @Override
    public void setSavepoint(Connection connection) {
        throw new InvokeEmptyMethodException("16.07.2019 (17:02)");
    }
    
    @Override
    public MysqlDataSource getDataSource() {
        return new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM).getDataSource();
    }
    
    @Override
    public abstract Savepoint getSavepoint(Connection connection);
    
}
