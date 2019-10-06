package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.SQLException;


/**
 @see TesterDB65SQLTest
 @since 05.10.2019 (16:27) */
public class TesterDB65SQL extends RegRuMysqlLoc {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, TesterDB65SQL.class.getSimpleName());
    
    public TesterDB65SQL(String dbName) {
        super(dbName);
    }
    
    @Override
    public Connection getDefaultConnection(String dbName) {
        MysqlDataSource testing = getDataSourceTesting();
        testing.setDatabaseName(dbName);
        Connection connection;
        try {
            messageToUser.info(testing.getServerName());
            connection = testing.getConnection();
        }
        catch (SQLException e) {
            e.printStackTrace();
            connection = super.getDefaultConnection(dbName);
        }
        return connection;
    }
    
    private @NotNull MysqlDataSource getDataSourceTesting() {
        if (!NetScanService.isReach(OtherKnownDevices.IP_SRVMYSQL_HOME)) {
            throw new InvokeIllegalException("10.10.111.65 is off!");
        }
        return DataConnectToAdapter.getLibDataSource();
    }
}