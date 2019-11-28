package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.info.NetScanService;
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
    
    private Connection connection;
    
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
    
    static {
        try {
            Driver driver = new org.h2.Driver();
            DriverManager.registerDriver(driver);
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("H2DB.static initializer", e.getMessage(), AbstractForms.networkerTrace(e)));
        }
        
    }
    
    @Override
    public int createTable(@NotNull String dbPointTable, @NotNull List<String> additionalColumns) {
        if (this.connection == null) {
            getDefaultConnection(dbPointTable);
        }
        String dbName = "null";
        if (dbPointTable.contains(".")) {
            dbName = dbPointTable.split("\\Q.\\E")[0];
        }
        final String sql = "CREATE SCHEMA " + dbName;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return preparedStatement.executeUpdate();
        }
        
        catch (SQLException e) {
            messageToUser.error("H2DB.createTable", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            return -666;
        }
    }
    
    @SuppressWarnings({"resource", "JDBCResourceOpenedButNotSafelyClosed"})
    @Override
    public Connection getDefaultConnection(String dbName) {
        if (NetScanService.isReach("srv-mysql-h")) {
            return DataConnectTo.getInstance(DataConnectTo.TESTING).getDefaultConnection(dbName);
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:h2:mem:" + dbName.split("\\Q.\\E")[1] + ";MODE=MYSQL;DATABASE_TO_LOWER=TRUE");
            this.connection = connection;
            
        }
        catch (SQLException e) {
            messageToUser.warn(H2DB.class.getSimpleName(), "getDefaultConnection", e.getMessage() + Thread.currentThread().getState().name());
        }
        return connection;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("H2DB{");
        sb.append('}');
        return sb.toString();
    }
}
