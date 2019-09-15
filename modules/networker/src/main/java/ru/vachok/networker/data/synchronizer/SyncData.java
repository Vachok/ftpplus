package ru.vachok.networker.data.synchronizer;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.stats.Stats;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Pattern;


/**
 @see SyncDataTest */
public abstract class SyncData implements DataConnectTo {
    
    
    static final DataConnectTo CONNECT_TO_REGRU = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.DBUSER_NETWORK);
    
    static final DataConnectTo CONNECT_TO_LOCAL = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
    
    static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, SyncData.class.getSimpleName());
    
    public static final String PC = "VelkomPCSync";
    
    private Deque<String> fromFileToJSON = new ConcurrentLinkedDeque<>();
    
    private String idColName = ConstantsFor.DBCOL_IDREC;
    
    @Contract(value = " -> new", pure = true)
    public static @NotNull SyncData getInstance(@NotNull String type) {
        switch (type) {
            case PC:
                return new VelkomPCSync();
            case Stats.DBUPLOAD:
                return new DBStatsUploader(type);
            default:
                return new SyncInDBStatistics(type);
        }
        
    }
    
    public abstract String syncData();
    
    public abstract void setOption(Object option);
    
    @Override
    public abstract int uploadFileTo(Collection stringsCollection, String tableName);
    
    @Override
    public MysqlDataSource getDataSource() {
        return CONNECT_TO_LOCAL.getDataSource();
    }
    
    @Override
    public Connection getDefaultConnection(String dbName) {
        return CONNECT_TO_LOCAL.getDefaultConnection(dbName);
    }
    
    Deque<String> getFromFileToJSON() {
        return fromFileToJSON;
    }
    
    void setFromFileToJSON(Deque<String> fromFileToJSON) {
        this.fromFileToJSON = fromFileToJSON;
    }
    
    int fillLimitDequeueFromDBWithFile(@NotNull Path syncFilePath, String dbToSync) {
        int lastLocalID = getLastLocalID(dbToSync);
        if (syncFilePath.toFile().exists()) {
            fromFileToJSON.addAll(FileSystemWorker.readFileToQueue(syncFilePath));
            int lastRemoteID = getLastRemoteID(getDbToSync());
            if (lastRemoteID == -666) {
                cutDequeFile(lastLocalID);
            }
        }
        else {
            String jsonFile = new DBRemoteDownloader(lastLocalID).writeJSON();
            fromFileToJSON.addAll(FileSystemWorker.readFileToQueue(Paths.get(jsonFile).toAbsolutePath().normalize()));
        }
        return fromFileToJSON.size();
    }
    
    private void cutDequeFile(int lastLocalID) {
        int lastRemoteID = fromFileToJSON.size();
        int diff = lastRemoteID - lastLocalID;
        diff = Math.abs(diff - lastRemoteID);
        for (int i = 0; i < diff; i++) {
            fromFileToJSON.poll();
        }
    }
    
    int getLastLocalID(String syncDB) {
        return getDBID(CONNECT_TO_LOCAL, syncDB);
    }
    
    int getLastRemoteID(String syncDB) {
        return getDBID(CONNECT_TO_REGRU, syncDB);
    }
    
    abstract String getDbToSync();
    
    public abstract void setDbToSync(String dbToSync);
    
    String getIdColName() {
        return idColName;
    }
    
    public void setIdColName(String idColName) {
        this.idColName = idColName;
    }
    
    abstract Map<String, String> makeColumns();
    
    @Contract("_ -> new")
    @NotNull String[] getCreateQuery(@NotNull String dbPointTableName, Map<String, String> columnsNameType) {
        if (!dbPointTableName.contains(".")) {
            dbPointTableName = DBNAME_VELKOM_POINT + dbPointTableName;
        }
        String[] dbTable = dbPointTableName.split("\\Q.\\E");
        if (dbTable[1].startsWith(String.valueOf(Pattern.compile("\\d")))) {
            throw new IllegalArgumentException(dbTable[1]);
        }
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilder1 = new StringBuilder();
        StringBuilder stringBuilder2 = new StringBuilder();
        
        stringBuilder.append("CREATE TABLE IF NOT EXISTS ")
            .append(dbTable[0])
            .append(".")
            .append(dbTable[1])
            .append("(\n")
            .append("  `idrec` mediumint(11) unsigned NOT NULL COMMENT '',\n")
            .append("  `stamp` bigint(13) unsigned NOT NULL COMMENT '',\n");
        Set<Map.Entry<String, String>> entries = columnsNameType.entrySet();
        entries.forEach(entry->stringBuilder.append("  `").append(entry.getKey()).append("` ").append(entry.getValue()).append(" NOT NULL COMMENT '',\n"));
        stringBuilder.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n");
        
        stringBuilder1.append(ConstantsFor.SQL_ALTERTABLE)
            .append(dbTable[0])
            .append(".")
            .append(dbTable[1])
            .append("\n")
            .append("  ADD PRIMARY KEY (`idrec`);\n");
        
        stringBuilder2.append(ConstantsFor.SQL_ALTERTABLE).append(dbPointTableName).append("\n")
            .append("  MODIFY `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '';").toString();
        return new String[]{stringBuilder.toString(), stringBuilder1.toString(), stringBuilder2.toString()};
    }
    
    private int getDBID(@NotNull DataConnectTo dataConnectTo, String syncDB) {
        MysqlDataSource source = dataConnectTo.getDataSource();
        
        try (Connection connection = source.getConnection()) {
            
            final String sql = String.format("select %s from %s ORDER BY %s DESC LIMIT 1", getIdColName(), syncDB, getIdColName());
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
            messageToUser.error(e.getMessage() + " see line: 169 ***");
            return -666;
        }
    }
    
    public abstract void superRun();
}
