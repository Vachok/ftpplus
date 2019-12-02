package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.NetworkerStopException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;
import java.util.Collection;
import java.util.List;


/**
 @see H2DBTest
 @since 01.11.2019 (9:40) */
public class H2DB implements DataConnectTo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, H2DB.class.getSimpleName());
    
    private static final String TESTS_ONLY = "4 tests only";
    
    private final Connection connection;
    
    private String dbName;
    
    @Override
    public MysqlDataSource getDataSource() {
        throw new UnsupportedOperationException(TESTS_ONLY);
    }
    
    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        throw new UnsupportedOperationException(TESTS_ONLY);
    }
    
    @Override
    public boolean dropTable(String dbPointTable) {
        throw new UnsupportedOperationException(TESTS_ONLY);
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
    
    @Override
    public Connection getDefaultConnection(String dbName) {
        this.dbName = dbName;
        if (NetScanService.isReach(OtherKnownDevices.SRVMYSQL_HOME)) {
            return DataConnectTo.getInstance(DataConnectTo.TESTING).getDefaultConnection(dbName);
        }
        else {
            return this.connection;
        }
    }
    
    H2DB() throws NetworkerStopException {
        try {
            this.dbName = ConstantsFor.DB_VELKOMVELKOMPC;
            Driver driver = new org.h2.Driver();
            DriverManager.registerDriver(driver);
            Connection connection = null;
            this.connection = DriverManager.getConnection("jdbc:h2:mem:" + dbName.split("\\Q.\\E")[1] + ";MODE=MYSQL;DATABASE_TO_LOWER=TRUE");
        }
        catch (SQLException e) {
            messageToUser.error(getClass().getSimpleName(), "H2DB", FileSystemWorker.error(getClass().getSimpleName() + ".H2DB", e));
            throw new NetworkerStopException(this.getClass().getSimpleName(), "public H2DB()", 91);
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("H2DB{");
        sb.append('}');
        return sb.toString();
    }
}
