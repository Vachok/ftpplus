package ru.vachok.networker.restapi.database;


import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.sql.Connection;


/**
 @since 15.07.2019 (11:05) */
public abstract class DataConnectToAdapter {
    
    
    public static Connection getRegRuMysqlLibConnection(String dbName) {
        DataConnectTo regRuMysql = new RegRuMysql();
        return regRuMysql.getDefaultConnection(dbName);
    }
}
