package ru.vachok.networker.data.synchronizer;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.info.stats.Stats;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @see SyncDataTest */
public abstract class SyncData implements DataConnectTo {
    
    
    private static final String UPUNIVERSAL = "DBUploadUniversal";
    
    static final DataConnectTo CONNECT_TO_REGRU = DataConnectTo.getRemoteReg();
    
    static final DataConnectTo CONNECT_TO_LOCAL = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
    
    static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, SyncData.class.getSimpleName());
    
    static final String DOWNLOADER = "DBRemoteDownloader";
    
    public static final String BACKUPER = "BackupDB";
    
    private Deque<String> fromFileToJSON = new ConcurrentLinkedDeque<>();
    
    private String idColName = ConstantsFor.DBCOL_IDREC;
    
    abstract String getDbToSync();
    
    public abstract void setDbToSync(String dbToSync);
    
    @Contract(pure = true)
    private String getIdColName() {
        return idColName;
    }
    
    public abstract void setOption(Object option);
    
    public void setIdColName(String idColName) {
        this.idColName = idColName;
    }
    
    public abstract String syncData();
    
    public abstract void superRun();
    
    @Override
    public abstract int uploadCollection(Collection stringsCollection, String tableName);
    
    @Override
    public boolean dropTable(String dbPointTable) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.SyncData.dropTable( boolean ) at 20.09.2019 - (20:37)");
    }
    
    @Override
    public MysqlDataSource getDataSource() {
        MysqlDataSource source = CONNECT_TO_LOCAL.getDataSource();
        source.setDatabaseName(FileNames.DIR_INETSTATS);
        return source;
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
    
    int getLastLocalID(String syncDB) {
        return getDBID(DataConnectTo.getInstance(DataConnectTo.TESTING).getDataSource(), syncDB);
    }
    
    private int getDBID(@NotNull MysqlDataSource source, String syncDB) {
        if (source.getUser() == null || source.getUser().isEmpty()) {
            source.setUser(AppComponents.getProps().getProperty(PropertiesNames.DBUSER));
            source.setPassword(AppComponents.getProps().getProperty(PropertiesNames.DBPASS));
        }
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
    
    void setFromFileToJSON(Deque<String> fromFileToJSON) {
        this.fromFileToJSON = fromFileToJSON;
    }
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Contract("_ -> new")
    public static @NotNull SyncData getInstance(@NotNull String type) {
        switch (type) {
            case DOWNLOADER:
                return new DBRemoteDownloader(0);
            case Stats.DBUPLOAD:
                return new DBStatsUploader(type);
            case UPUNIVERSAL:
                return new DBUploadUniversal(DataConnectTo.DBNAME_VELKOM_POINT);
            case BACKUPER:
                return new BackupDB();
            default:
                return new InternetSync(type);
        }
        
    }
    
    abstract Map<String, String> makeColumns();
    
    int getLastRemoteID(String syncDB) {
        return getDBID(DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDataSource(), syncDB);
    }
    
}
