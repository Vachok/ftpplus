package ru.vachok.networker.data.synchronizer;


import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.StringJoiner;


/**
 
 @since 15.09.2019 (9:06) */
class VelkomPCSync extends SyncData {
    
    
    private static final String DB = ConstantsFor.DBBASENAME_U0466446_VELKOM + "." + ConstantsFor.TABLE_VELKOMPC;
    
    @Override
    public String syncData() {
        Path rootPath = Paths.get(".");
        
        int locID = getLastLocalID(DB);
        DBRemoteDownloader downloader = new DBRemoteDownloader(locID);
        downloader.setDbToSync(this.DB);
        downloader.writeJSON();
        return velkomPCSync(rootPath);
    }
    
    private String velkomPCSync(Path rootPath) {
        rootPath = Paths.get(rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + DB + FileNames.EXT_TABLE);
        messageToUser.info(fillLimitDequeueFromDBWithFile(rootPath, DB) + SyncInDBStatistics.LIMDEQ_STR);
        DBStatsUploader dbStatsUploader = new DBStatsUploader(DB);
        Deque<String> jsonDeq = getFromFileToJSON();
        dbStatsUploader.setOption(jsonDeq);
        dbStatsUploader.syncData();
        return dbStatsUploader.toString();
    }
    
    @Override
    public void setOption(Object option) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.SyncInDBStatistics.VelkomPCSync.setOption( void ) at 14.09.2019 - (14:15)");
    }
    
    @Override
    public int uploadFileTo(Collection stringsCollection, String tableName) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.SyncInDBStatistics.VelkomPCSync.uploadFileTo( int ) at 14.09.2019 - (14:15)");
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", VelkomPCSync.class.getSimpleName() + "[\n", "\n]")
            .add("dbToSync = '" + DB + "'")
            .toString();
    }
    
    String getDbToSync() {
        return DB;
    }
    
    @Override
    public void setDbToSync(String dbToSync) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " = " + DB);
    }
    
    @Override
    Map<String, String> makeColumns() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.SyncInDBStatistics.VelkomPCSync.makeColumns( Map<String, String> ) at 14.09.2019 - (14:15)");
    }
    
    @Override
    void superRun() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.VelkomPCSync.superRun( void ) at 15.09.2019 - (10:19)");
    }
}