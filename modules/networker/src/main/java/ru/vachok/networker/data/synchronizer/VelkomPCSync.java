package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;


/**
 @since 15.09.2019 (9:06) */
class VelkomPCSync extends SyncData {
    
    
    private static final String DB = ConstantsFor.DBBASENAME_U0466446_VELKOM + "." + ConstantsFor.TABLE_VELKOMPC;
    
    private Collection collection;
    
    @Override
    public String syncData() {
        Path rootPath = Paths.get(".");
        int locID = getLastLocalID(DB);
        DBRemoteDownloader downloader = new DBRemoteDownloader(locID);
        downloader.setDbToSync(this.DB);
        downloader.writeJSON();
        return velkomPCSync(rootPath);
    }
    
    @Override
    String getDbToSync() {
        return DB;
    }
    
    @Override
    public void setOption(Object option) {
        this.collection = (Collection) option;
    }
    
    @Override
    public int uploadFileTo(Collection stringsCollection, String tableName) {
        collection = stringsCollection;
        return collection.size();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", VelkomPCSync.class.getSimpleName() + "[\n", "\n]")
            .add("dbToSync = '" + DB + "'")
            .toString();
    }
    
    @Override
    public void superRun() {
        String dbLocal = DataConnectTo.DBNAME_VELKOM_POINT + ConstantsFor.DB_PCUSERAUTO;
        int lastLocalID = getLastLocalID(dbLocal);
        if (lastLocalID == -666) {
            @NotNull String[] query = getCreateQuery(dbLocal, makeColumns());
            makeTable(query);
        }
        DBRemoteDownloader downloader = new DBRemoteDownloader(lastLocalID);
        downloader.setDbToSync(dbLocal);
        FileSystemWorker.writeFile(dbLocal, downloader.syncData());
        DBStatsUploader uploader = new DBStatsUploader(DataConnectTo.DBNAME_VELKOM_POINT);
        uploader.syncData();
    }
    
    @Override
    Map<String, String> makeColumns() {
        Map<String, String> colMap = new HashMap<>();
        colMap.put(ConstantsFor.DBFIELD_PCNAME, ConstantsFor.VARCHAR_20);
        colMap.put(ConstantsFor.DBFIELD_USERNAME, "varchar(45)");
        colMap.put("lastmod", "enum('DO0213', 'HOME', 'rups00')");
        colMap.put(ConstantsNet.DB_FIELD_WHENQUERIED, "timestamp");
        return colMap;
    }
    
    private void makeTable(@NotNull String[] queries) {
        
        try (Connection connection = CONNECT_TO_LOCAL.getDefaultConnection(DataConnectTo.DBNAME_VELKOM_POINT)) {
            for (String query : queries) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    messageToUser.info(this.getClass().getSimpleName(), "preparedStatement", String.valueOf(preparedStatement.executeUpdate()));
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 97 ***");
        }
    }
    
    private String velkomPCSync(Path rootPath) {
        rootPath = Paths.get(rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + DB + FileNames.EXT_TABLE);
        messageToUser.info(fillLimitDequeueFromDBWithFile(rootPath, DB) + SyncInDBStatistics.LIMDEQ_STR);
        Deque<String> jsonDeq = getFromFileToJSON();
        DBUploadUniversal dbUploadUniversal = new DBUploadUniversal(jsonDeq, DB);
        dbUploadUniversal.syncData();
        return dbUploadUniversal.toString();
    }
    
    @Override
    public void setDbToSync(String dbToSync) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " = " + DB);
    }
}