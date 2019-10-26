package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.SQLException;


/**
 @see TesterDB65SQLTest
 @since 05.10.2019 (16:27) */
@SuppressWarnings({"resource", "JDBCResourceOpenedButNotSafelyClosed"})
public class TesterDB65SQL extends MySqlLocalSRVInetStat {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, TesterDB65SQL.class.getSimpleName());
    
    @Override
    public Connection getDefaultConnection(@NotNull String dbName) {
        MysqlDataSource sourceT = getDataSource();
        sourceT.setDatabaseName(dbName);
        Connection connection;
        //noinspection OverlyBroadCatchBlock
        try {
            messageToUser.info(sourceT.getServerName());
            connection = sourceT.getConnection();
        }
        catch (Exception e) {
            messageToUser.error("TesterDB65SQL.getDefaultConnection", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace()));
            connection = super.getDefaultConnection(dbName);
        }
        try {
            String url = connection.getMetaData().getURL();
            messageToUser.warn(this.getClass().getSimpleName(), "return connect to: ", url);
        }
        catch (SQLException e) {
            messageToUser.error(TesterDB65SQL.class.getSimpleName(), e.getMessage(), " see line: 43 ***");
        }
        return connection;
    }
    
    @Override
    public MysqlDataSource getDataSource() {
        if (!NetScanService.isReach(OtherKnownDevices.SRVMYSQL_HOME)) {
            return super.getDataSource();
        }
        MysqlDataSource source = new MysqlDataSource();
        source.setServerName(OtherKnownDevices.SRVMYSQL_HOME);
        source.setCreateDatabaseIfNotExist(true);
        source.setUser("kudr");
        source.setPassword("36e42yoak8");
        return source;
    }
}