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
    
    
    public static final String PC = "VelkomPCSync";
    
    private static final String UPUNIVERSAL = "DBUploadUniversal";
    
    static final ru.vachok.mysqlandprops.DataConnectTo CONNECT_TO_REGRU = DataConnectTo.getInstance(DataConnectTo.LOCAL_REGRU);
    
    static final ru.vachok.mysqlandprops.DataConnectTo CONNECT_TO_LOCAL = DataConnectTo.getDefaultI();
    
    static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, SyncData.class.getSimpleName());
    
    static final String DOWNLOADER = "DBRemoteDownloader";
    
    private Deque<String> fromFileToJSON = new ConcurrentLinkedDeque<>();
    
    private String idColName = ConstantsFor.DBCOL_IDREC;
    
    @Contract(value = " -> new", pure = true)
    public static @NotNull SyncData getInstance(@NotNull String type) {
        switch (type) {
            case DOWNLOADER:
                return new DBRemoteDownloader(0);
            case PC:
                return new VelkomPCSync();
            case Stats.DBUPLOAD:
                return new DBStatsUploader(type);
            case UPUNIVERSAL:
                return new DBUploadUniversal(DataConnectTo.DBNAME_VELKOM_POINT);
            default:
                return new SyncInDBStatistics(type);
        }
        
    }
    
    public abstract String syncData();
    
    public abstract void setOption(Object option);
    
    public abstract void superRun();
    
    @Override
    public abstract int uploadCollection(Collection stringsCollection, String tableName);
    
    @Override
    public MysqlDataSource getDataSource() {
        return CONNECT_TO_LOCAL.getDataSource();
    }
    
    @Override
    public Connection getDefaultConnection(String dbName) {
        try {
            MysqlDataSource source = DataConnectTo.getDefaultI().getDataSource();
            source.setDatabaseName(dbName);
            return source.getConnection();
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 76 ***");
            return DataConnectTo.getDefaultI().getDefaultConnection(dbName);
        }
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
            String jsonFile = new DBRemoteDownloader(lastLocalID).syncData();
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
        return getDBID((DataConnectTo) CONNECT_TO_LOCAL, syncDB);
    }
    
    private int getDBID(DataConnectTo dataConnectTo, String syncDB) {
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
        if (!dbPointTableName.contains(".") || dbPointTableName.matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
            throw new IllegalArgumentException(dbPointTableName);
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
                .append("(\n");
        if (!columnsNameType.containsKey(ConstantsFor.DBCOL_IDREC)) {
            stringBuilder.append("  `idrec` INT(11),\n");
        }
        if (!columnsNameType.containsKey(ConstantsFor.DBCOL_STAMP)) {
            stringBuilder.append("  `stamp` BIGINT(13) NOT NULL DEFAULT '442278000000' ,\n");
        }
        Set<Map.Entry<String, String>> entries = columnsNameType.entrySet();
        entries.forEach(entry->stringBuilder.append("  `").append(entry.getKey()).append("` ").append(entry.getValue()).append(",\n"));
        stringBuilder.replace(stringBuilder.length() - 2, stringBuilder.length(), "");
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
    
    int getLastRemoteID(String syncDB) {
        return getDBID((DataConnectTo) CONNECT_TO_REGRU, syncDB);
    }
}
