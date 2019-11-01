package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;


/**
 @see H2DBTest
 @since 01.11.2019 (9:40) */
public class H2DB implements DataConnectTo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, H2DB.class.getSimpleName());
    
    @Override
    public MysqlDataSource getDataSource() {
        throw new UnsupportedOperationException(toString());
    }
    
    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        throw new TODOException("01.11.2019 (9:22)");
    }
    
    @Override
    public boolean dropTable(String dbPointTable) {
        throw new TODOException("01.11.2019 (9:22)");
    }
    
    @Override
    public int createTable(String dbPointTable, List<String> additionalColumns) {
        throw new TODOException("01.11.2019 (9:24)");
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("H2DB{");
        sb.append('}');
        return sb.toString();
    }
    
    static {
        try {
            Driver driver = new org.h2.Driver();
            DriverManager.registerDriver(driver);
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("H2DB.static initializer", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
        }
        
    }
    
    
    @SuppressWarnings({"resource", "JDBCResourceOpenedButNotSafelyClosed"})
    @Override
    public Connection getDefaultConnection(String dbName) {
        Connection connection = DataConnectTo.getInstance(DataConnectTo.TESTING).getDefaultConnection(dbName);
        try {
            connection = DriverManager.getConnection("jdbc:h2:mem:" + dbName + ";MODE=MYSQL;DATABASE_TO_LOWER=TRUE");
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("H2DB.getDefaultConnection", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
        }
        return connection;
    }
}
