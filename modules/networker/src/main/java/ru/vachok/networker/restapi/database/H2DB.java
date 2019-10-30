package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;
import java.text.MessageFormat;
import java.util.Collection;


public class H2DB implements DataConnectTo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, H2DB.class.getSimpleName());
    
    @Override
    public MysqlDataSource getDataSource() {
        return null;
    }
    
    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        return 0;
    }
    
    @Override
    public boolean dropTable(String dbPointTable) {
        return false;
    }
    
    static {
        try {
            Class.forName("org.h2.Driver");
        }
        catch (ClassNotFoundException e) {
            messageToUser.error(MessageFormat.format("H2DB.static initializer", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
        }
    }
    
    
    @Override
    public Connection getDefaultConnection(String dbName) {
        Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(dbName);
        try {
            connection = DriverManager.getConnection("jdbc:h2:mem:" + dbName);
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("H2DB.getDefaultConnection", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
        }
        return connection;
    }
}
