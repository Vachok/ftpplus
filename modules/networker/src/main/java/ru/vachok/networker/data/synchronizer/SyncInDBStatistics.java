package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @see SyncInDBStatisticsTest
 @since 08.09.2019 (14:55) */
@SuppressWarnings("InstanceVariableOfConcreteClass")
class SyncInDBStatistics extends SyncData {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, SyncInDBStatistics.class.getSimpleName());
    
    private static final String LIMDEQ_STR = " fillLimitDequeFromDBWithFile";
    
    private String ipAddress = getDbToSync();
    
    private Deque<String> fromFileToJSON = new ConcurrentLinkedDeque<>();
    
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
        if (ipAddress.equalsIgnoreCase(ConstantsFor.TABLE_VELKOMPC)) {
            rootPath = Paths.get(rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + ipAddress + FileNames.EXT_TABLE);
            messageToUser.info(fillLimitDequeueFromDBWithFile(rootPath) + LIMDEQ_STR);
            DBStatsUploader dbStatsUploader = new DBStatsUploader();
            dbStatsUploader.setOption(fromFileToJSON);
            result.append(dbStatsUploader.toString());
        }
        else {
            if (ipAddress.isEmpty() || !ipAddress.matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
                throw new IllegalArgumentException(ipAddress);
            }
        
            String statFile = ConstantsFor.FILESYSTEM_SEPARATOR + ConstantsFor.STR_INETSTATS + ConstantsFor.FILESYSTEM_SEPARATOR + ipAddress
                .replaceAll("_", ".") + ".csv";
            String inetStatsPath = rootPath.toAbsolutePath().normalize().toString() + statFile;
            messageToUser.info(fillLimitDequeueFromDBWithFile(Paths.get(inetStatsPath)) + LIMDEQ_STR);
            result.append(convertToJSON());
        }
        return result.toString();
    }
    
    private int fillLimitDequeueFromDBWithFile(@NotNull Path inetStatsPath) {
        int lastLocalID = getLastLocalID();
        
        if (inetStatsPath.toFile().exists()) {
            fromFileToJSON.addAll(FileSystemWorker.readFileToQueue(inetStatsPath));
            int lastRemoteID = getLastRemoteID();
            for (int i = 0; i < (lastRemoteID - lastLocalID); i++) {
                fromFileToJSON.poll();
            }
        }
        else {
            String jsonFile = new DBRemoteDownloader(lastLocalID).writeJSON();
            fromFileToJSON.addAll(FileSystemWorker.readFileToQueue(Paths.get(jsonFile).toAbsolutePath().normalize()));
        }
        setDbToSync(ipAddress.replaceAll("\\Q.\\E", "_"));
        DBStatsUploader statsUploader = new DBStatsUploader();
        statsUploader.setOption(fromFileToJSON);
        String syncData = statsUploader.syncData();
        return fromFileToJSON.size();
    }
    
    private @NotNull String convertToJSON() {
        Iterator<String> deqIterator = fromFileToJSON.iterator();
        
        while (deqIterator.hasNext()) {
            String entryStat = fromFileToJSON.removeFirst();
            if (entryStat.isEmpty() || !entryStat.contains(",")) {
                continue;
            }
            
            fromFileToJSON.addLast(makeJSONString(entryStat));
    
            if (entryStat.contains("[") & entryStat.contains("]")) {
                break;
            }
            
        }
        return new TForms().fromArray(fromFileToJSON);
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
            this.fromFileToJSON = (Deque<String>) option;
        }
        else {
            this.ipAddress = (String) option;
        }
    }
    
    private void getTableName(@NotNull Path rootPath) {
        File[] inetFiles = Paths.get(rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + ConstantsFor.STR_INETSTATS).toFile()
            .listFiles();
        for (File statCsv : inetFiles) {
            String tableName = statCsv.getName().replaceAll("\\Q.\\E", "_").replace("_csv", "");
            dbStatsUploader.setOption(tableName);
            makeTable(tableName);
        }
    }
    
    private @NotNull String makePathStr() {
        Path rootPath = Paths.get(".");
        rootPath = Paths.get(rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + ConstantsFor.STR_INETSTATS);
        
        makeTable(rootPath.toAbsolutePath().normalize().toString());
        return rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + ipAddress.replaceAll("_", ".") + ".csv";
    }
}
