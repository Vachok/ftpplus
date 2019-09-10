package ru.vachok.networker.data.synchronizer;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.*;


public abstract class SyncData {
    
    
    static final DataConnectTo CONNECT_TO_REGRU = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.DBUSER_NETWORK);
    
    static final DataConnectTo CONNECT_TO_LOCAL = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
    
    static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, SyncData.class.getSimpleName());
    
    private String idColName = ConstantsFor.DBCOL_IDREC;
    
    private String dbToSync = ConstantsFor.TABLE_VELKOMPC;
    
    @Contract(value = " -> new", pure = true)
    public static @NotNull SyncData getInstance() {
        return new SyncInetStatistics();
    }
    
    public abstract String syncData();
    
    public abstract void setOption(Object option);
    
    int getLastLocalID() {
        return getDBID(CONNECT_TO_LOCAL);
    }
    
    private int getDBID(@NotNull DataConnectTo dataConnectTo) {
        MysqlDataSource source = dataConnectTo.getDataSource();
        source.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        try (Connection connection = source.getConnection()) {
            final String sql = String.format("select %s from %s ORDER BY %s DESC LIMIT 1", getIdColName(), getDbToSync(), getIdColName());
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    int retInt = 0;
                    while (resultSet.next()) {
                        if (resultSet.last()) {
                            retInt = resultSet.getInt(getIdColName());
                        }
                    }
                    return retInt;
                }
            }
        }
        catch (SQLException e) {
            return 0;
        }
    }
    
    String getIdColName() {
        return idColName;
    }
    
    public void setIdColName(String idColName) {
        this.idColName = idColName;
    }
    
    String getDbToSync() {
        return dbToSync;
    }
    
    public void setDbToSync(String dbToSync) {
        this.dbToSync = dbToSync;
    }
    
    int getLastRemoteID() {
        return getDBID(CONNECT_TO_REGRU);
    }
    
    void makeTable(String name) {
        String[] sqlS = {
                ConstantsFor.SQL_ALTERTABLE + name + "\n" +
                        "  ADD PRIMARY KEY (`idrec`),\n" +
                        "  ADD UNIQUE KEY `stamp` (`stamp`,`ip`,`bytes`) USING BTREE,\n" +
                        "  ADD KEY `ip` (`ip`);",
                
                ConstantsFor.SQL_ALTERTABLE + name + "\n" +
                        "  MODIFY `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '';"};
        createUploadStatTable(name, sqlS);
    }
    
    private int createUploadStatTable(@NotNull String aboutWhat, String[] sql) {
        if (aboutWhat.contains(".")) {
            aboutWhat = aboutWhat.replaceAll("\\Q.\\E", "_");
        }
        try (Connection connection = CONNECT_TO_LOCAL.getDefaultConnection(ConstantsFor.STR_INETSTATS)) {
            try (PreparedStatement preparedStatementCreateTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + aboutWhat + "(\n" +
                    "  `idrec` mediumint(11) unsigned NOT NULL COMMENT '',\n" +
                    "  `stamp` bigint(13) unsigned NOT NULL COMMENT '',\n" +
                    "  `squidans` varchar(20) NOT NULL COMMENT '',\n" +
                    "  `bytes` int(11) NOT NULL COMMENT '',\n" +
                    "  `timespend` int(11) NOT NULL DEFAULT '0',\n" +
                    "  `site` varchar(254) NOT NULL COMMENT ''\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            ) {
                if (preparedStatementCreateTable.executeUpdate() == 0) {
                    for (String sqlCom : sql) {
                        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlCom)) {
                            messageToUser.info("preparedStatement", aboutWhat, String.valueOf(preparedStatement.executeUpdate()));
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 142 ***");
            return -10;
        }
        return 0;
    }
    
    
}
