package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 @see SyncInDBStatisticsTest
 @since 08.09.2019 (14:55) */
@SuppressWarnings("InstanceVariableOfConcreteClass")
class SyncInDBStatistics extends SyncData {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, SyncInDBStatistics.class.getSimpleName());
    
    private static final String LIMDEQ_STR = " fillLimitDequeFromDBWithFile";
    
    private String ipAddress = getDbToSync();
    
    private DBStatsUploader dbStatsUploader = new DBStatsUploader();
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SyncInetStatistics{");
    
        sb.append(", dbStatsUploader=").append(dbStatsUploader);
        sb.append(", aboutWhat='").append(ipAddress).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String syncData() {
        StringBuilder result = new StringBuilder();
        Path rootPath = Paths.get(".");
        if (ipAddress.equalsIgnoreCase(ConstantsFor.DBBASENAME_U0466446_VELKOM + "." + ConstantsFor.TABLE_VELKOMPC)) {
            result.append(new SyncInDBStatistics.VelkomPCSync().syncData());
        }
        else {
            result.append(inetStatSync(rootPath));
        }
        return result.toString();
    }
    
    private @NotNull String inetStatSync(Path rootPath) {
        if (ipAddress.isEmpty() || !ipAddress.matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
            throw new IllegalArgumentException(ipAddress);
        }
        
        String statFile = ConstantsFor.FILESYSTEM_SEPARATOR + ConstantsFor.STR_INETSTATS + ConstantsFor.FILESYSTEM_SEPARATOR + ipAddress
            .replaceAll("_", ".") + ".csv";
        String inetStatsPath = rootPath.toAbsolutePath().normalize().toString() + statFile;
        messageToUser.info(fillLimitDequeueFromDBWithFile(Paths.get(inetStatsPath)) + LIMDEQ_STR);
        return convertToJSON();
    }
    
    private @NotNull String convertToJSON() {
        Iterator<String> deqIterator = getFromFileToJSON().iterator();
        
        while (deqIterator.hasNext()) {
            String entryStat = getFromFileToJSON().removeFirst();
            if (entryStat.isEmpty() || !entryStat.contains(",")) {
                continue;
            }
    
            getFromFileToJSON().addLast(makeJSONString(entryStat));
            
            if (entryStat.contains("[") & entryStat.contains("]")) {
                break;
            }
            
        }
        return new TForms().fromArray(getFromFileToJSON());
    }
    
    private @NotNull String makeJSONString(@NotNull String entryStat) {
        String[] dbFields = entryStat.split(",");
        try {
            dbFields[0] = "\"stamp\":\"" + dbFields[0].replaceAll(">", "") + "\"";
            dbFields[1] = "\"squidans\":\"" + dbFields[1].replaceAll(">", "") + "\"";
            dbFields[2] = "\"bytes\":\"" + dbFields[2].replaceAll(">", "") + "\"";
    
            dbFields[3] = "\"timespend\":\"" + 0 + "\"";
            dbFields[4] = "\"site\":\"" + dbFields[4].replaceAll("<br>", "") + "\"";
        }
        catch (IndexOutOfBoundsException ignore) {
            //10.09.2019 (19:37)
        }
        return Arrays.toString(dbFields);
    }
    
    @Override
    public void setOption(Object option) {
        if (option instanceof Deque) {
            setFromFileToJSON((Deque<String>) option);
        }
        else {
            this.ipAddress = (String) option;
        }
    }
    
    @Override
    public int uploadFileTo(Collection stringsCollection, String tableName) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.SyncInDBStatistics.uploadFileTo( int ) at 14.09.2019 - (9:11)");
    }
    
    @Override
    String getDbToSync() {
        return ipAddress;
    }
    
    @Override
    public void setDbToSync(String dbToSync) {
        this.ipAddress = dbToSync;
    }
    
    @Override
    Map<String, String> makeColumns() {
        Map<String, String> colMap = new HashMap<>();
        colMap.put(ConstantsFor.DBCOL_SQUIDANS, ConstantsFor.VARCHAR_20);
        colMap.put("site", ConstantsFor.VARCHAR_190);
        colMap.put(ConstantsFor.DBCOL_BYTES, "int(11)");
        colMap.put(ConstantsFor.DBCOL_STAMP, ConstantsFor.BIGINT_13);
        return colMap;
    }
    
    private void getTableName(@NotNull Path rootPath) {
        File[] inetFiles = Paths.get(rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + ConstantsFor.STR_INETSTATS).toFile()
            .listFiles();
        for (File statCsv : inetFiles) {
            String tableName = statCsv.getName().replaceAll("\\Q.\\E", "_").replace("_csv", "");
            dbStatsUploader.setOption(tableName);
        }
    }
    
    static class VelkomPCSync extends SyncData {
        
        
        private String dbToSync = ConstantsFor.DBBASENAME_U0466446_VELKOM + "." + ConstantsFor.TABLE_VELKOMPC;
        
        @Override
        public String syncData() {
            Path rootPath = Paths.get(".");
            setDbToSync(this.dbToSync);
            int locID = getLastLocalID(dbToSync);
            DBRemoteDownloader downloader = new DBRemoteDownloader(locID);
            downloader.setDbToSync(this.dbToSync);
            downloader.writeJSON();
            return velkomPCSync(rootPath);
        }
        
        private String velkomPCSync(Path rootPath) {
            setDbToSync(dbToSync);
            rootPath = Paths.get(rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + dbToSync + FileNames.EXT_TABLE);
            messageToUser.info(fillLimitDequeueFromDBWithFile(rootPath) + LIMDEQ_STR);
            DBStatsUploader dbStatsUploader = new DBStatsUploader();
            dbStatsUploader.setOption(getFromFileToJSON());
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
        String getDbToSync() {
            return dbToSync;
        }
        
        @Override
        public void setDbToSync(String dbToSync) {
            this.dbToSync = dbToSync;
        }
        
        @Override
        Map<String, String> makeColumns() {
            throw new TODOException("ru.vachok.networker.data.synchronizer.SyncInDBStatistics.VelkomPCSync.makeColumns( Map<String, String> ) at 14.09.2019 - (14:15)");
        }
        
    }
}
