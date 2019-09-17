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
 @see VelkomPCSyncTest
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
        String json = downloader.syncData();
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
    public int uploadCollection(Collection stringsCollection, String tableName) {
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
    Map<String, String> makeColumns() {
        Map<String, String> colMap = new HashMap<>();
        colMap.put(ConstantsFor.DBFIELD_PCNAME, ConstantsFor.VARCHAR_20);
        colMap.put(ConstantsFor.DBFIELD_USERNAME, "VARCHAR(45) NOT NULL DEFAULT 'no data'");
        colMap.put("lastmod", "enum('DO0213', 'HOME', 'rups00')");
        colMap.put(ConstantsNet.DB_FIELD_WHENQUERIED, " TIMESTAMP on update CURRENT_TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
        return colMap;
    }
    
    /**
     @see VelkomPCSyncTest#testSuperRun()
     */
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
        Path pathToJSONFile = Paths.get(downloader.syncData()).toAbsolutePath().normalize();
        Queue<String> readFileToQueue = FileSystemWorker.readFileToQueue(pathToJSONFile);
        DBUploadUniversal dbUploadUniversal = new DBUploadUniversal(readFileToQueue, dbLocal);
        dbUploadUniversal.syncData();
        pathToJSONFile.toFile().deleteOnExit();
    }
    
    private void makeTable(@NotNull String[] queries) {
        try (Connection connection = CONNECT_TO_LOCAL.getDefaultConnection(DataConnectTo.DBNAME_VELKOM_POINT)) {
            messageToUser.info(this.getClass().getSimpleName(), "connected: ", connection.getMetaData().getURL());
            for (String query : queries) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    messageToUser.info(this.getClass().getSimpleName(), query, String.valueOf(preparedStatement.executeUpdate()));
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