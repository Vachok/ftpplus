package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @see SyncInetStatisticsTest
 @since 08.09.2019 (14:55) */
@SuppressWarnings("InstanceVariableOfConcreteClass")
class SyncInetStatistics extends SyncData {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, SyncInetStatistics.class.getSimpleName());
    
    private String ipAddress = "";
    
    private Deque<String> fromFileToJSON;
    
    private DBStatsUploader dbStatsUploader = new DBStatsUploader();
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SyncInetStatistics{");
        sb.append("queueFromFile=").append(fromFileToJSON.size());
        sb.append(", dbStatsUploader=").append(dbStatsUploader);
        sb.append(", aboutWhat='").append(ipAddress).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String syncData() {
        if (ipAddress.isEmpty() || !ipAddress.matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
            throw new IllegalArgumentException(ipAddress);
        }
    
        Path rootPath = Paths.get(".");
        String statFile = ConstantsFor.FILESYSTEM_SEPARATOR + ConstantsFor.STR_INETSTATS + ConstantsFor.FILESYSTEM_SEPARATOR + ipAddress.replaceAll("_", ".") + ".csv";
        String inetStatsPath = rootPath.toAbsolutePath().normalize().toString() + statFile;
    
        this.fromFileToJSON = getLimitDequeueFromFile(Paths.get(inetStatsPath));
    
        return convertToJSON();
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
    
    private @NotNull Deque<String> getLimitDequeueFromFile(Path inetStatsPath) {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        Queue<String> statAbout = FileSystemWorker.readFileToQueue(inetStatsPath);
        setDbToSync(ipAddress.replaceAll("\\Q.\\E", "_"));
        int lastRemoteID = getLastRemoteID();
        int lastLocalID = getLastLocalID();
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.STR_INETSTATS);
             PreparedStatement preparedStatement = connection
                     .prepareStatement("select idrec from " + ipAddress.replaceAll("\\Q.\\E", "_") + " ORDER BY idrec DESC LIMIT 1");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                int idrec = resultSet.getInt(ConstantsFor.DBCOL_IDREC);
                for (int i = 0; i < idrec; i++) {
                    statAbout.poll();
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 88 ***");
        }
        Deque<String> retDeq = new ConcurrentLinkedDeque<>();
        retDeq.addAll(statAbout);
        return retDeq;
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
        return "inetStatsPath";
    }
    
    private @NotNull String makeJSONString(@NotNull String entryStat) {
        String[] dbFields = entryStat.split(",");
        
        dbFields[0] = "\"stamp\":\"" + dbFields[0].replaceAll(">", "") + "\"";
        dbFields[1] = "\"squidans\":\"" + dbFields[1].replaceAll(">", "") + "\"";
        dbFields[2] = "\"bytes\":\"" + dbFields[2].replaceAll(">", "") + "\"";
        
        dbFields[3] = "\"timespend\":\"" + 0 + "\"";
        dbFields[4] = "\"site\":\"" + dbFields[4].replaceAll("<br>", "") + "\"";
        return Arrays.toString(dbFields);
    }
    
    private @NotNull String makePathStr() {
        Path rootPath = Paths.get(".");
        rootPath = Paths.get(rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + ConstantsFor.STR_INETSTATS);
        
        makeTable(rootPath.toAbsolutePath().normalize().toString());
        return rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + ipAddress.replaceAll("_", ".") + ".csv";
    }
}
