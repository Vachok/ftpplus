package ru.vachok.networker.restapi.database;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;
import java.util.List;


/**
 @see TesterDB65SQLTest
 @since 05.10.2019 (16:27) */
@SuppressWarnings({"resource", "JDBCResourceOpenedButNotSafelyClosed"})
public class TesterDB65SQL extends MySqlLocalSRVInetStat {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, TesterDB65SQL.class.getSimpleName());
    
    private String dbName;
    
    @Override
    public int createTable(@NotNull String dbPointTable, List<String> additionalColumns) {
        final String sql = String
                .format("CREATE TABLE `%s` (\n\t`idrec` INT(7) UNSIGNED NOT NULL DEFAULT 0,\n\t`stamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),\n\t`upstring` VARCHAR(1000) NOT NULL DEFAULT '-',\n\tPRIMARY KEY (`idrec`),\n\tUNIQUE INDEX `upstring` (`upstring`)\n)\nCOLLATE='utf8_general_ci'\nENGINE=MyISAM\nMAX_ROWS=1000000\n;\n", dbPointTable);
        messageToUser.warn(dbPointTable, this.getClass().getSimpleName() + " creating...", sql);
        int retInt = 0;
        try (Connection connection = getDefaultConnection(dbPointTable);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            retInt = preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            messageToUser.error("TesterDB65SQL.createTable", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        return retInt;
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
    
    @Override
    public Connection getDefaultConnection(@NotNull String dbName) {
        this.dbName = dbName;
        MysqlDataSource sourceT = getDataSource();
        if (dbName.contains(".")) {
            sourceT.setDatabaseName(dbName.split("\\Q.\\E")[0]);
        }
        else {
            sourceT.setDatabaseName(dbName);
        }
        Connection connection;
        //noinspection OverlyBroadCatchBlock
        try {
            messageToUser.info(sourceT.getServerName());
            connection = sourceT.getConnection();
        }
        catch (Exception e) {
            messageToUser.warn(TesterDB65SQL.class.getSimpleName(), "getDefaultConnection", e.getMessage() + Thread.currentThread().getState().name());
            connection = alternateConnection();
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
    
    private Connection alternateConnection() {
        return DataConnectTo.getInstance(DataConnectTo.REGRUCONNECTION).getDefaultConnection(dbName.split("\\Q.\\E")[1]);
    }
    
    @Override
    public boolean dropTable(String dbPointTable) {
        final String sql = String.format("drop table `%s`", dbPointTable);
        try (Connection connection = getDefaultConnection(dbPointTable);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return preparedStatement.executeUpdate() == 0;
        }
        catch (SQLException e) {
            messageToUser.error("TesterDB65SQL.dropTable", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            return false;
        }
    }
}