package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
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
    
    
    static {
        try {
            Driver driver = new org.h2.Driver();
            DriverManager.registerDriver(driver);
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("H2DB.static initializer", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
        }
        
    }
    
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
    public int createTable(@NotNull String dbPointTable, @NotNull List<String> additionalColumns) {
        if (dbPointTable.contains(".")) {
            dbPointTable = dbPointTable.split("\\Q.\\E")[1];
        }
        StringBuilder preSQL = new StringBuilder()
            .append("CREATE TABLE ").append(dbPointTable).append(" (\n")
            .append("\t`idRec` INT NOT NULL AUTO_INCREMENT,\n\t`tstamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),\n");
        if (additionalColumns.size() > 0) {
            for (String collumn : additionalColumns) {
                preSQL.append("\t");
                preSQL.append(collumn);
                preSQL.append(",\n");
            }
        }
        preSQL.append("\tPRIMARY KEY (`idRec`)")
            .append(");");
        final String sql = preSQL.toString();
        try (Connection connection = getDefaultConnection(dbPointTable)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                return preparedStatement.executeUpdate();
            }
        }
        catch (SQLException e) {
            messageToUser.error("H2DB.createTable", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace()));
            return -666;
        }
    }
    
    @SuppressWarnings({"resource", "JDBCResourceOpenedButNotSafelyClosed"})
    @Override
    public Connection getDefaultConnection(String dbName) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:h2:mem:" + dbName + ";MODE=MYSQL;DATABASE_TO_LOWER=TRUE");
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
