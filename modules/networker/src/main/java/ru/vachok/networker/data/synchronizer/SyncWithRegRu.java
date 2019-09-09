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
import java.util.Queue;
import java.util.StringJoiner;


/**
 @see SyncWithRegRuTest
 @since 08.09.2019 (14:55) */
@SuppressWarnings("InstanceVariableOfConcreteClass")
public class SyncWithRegRu implements SyncData {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, SyncWithRegRu.class.getSimpleName());
    
    private String aboutWhat = "";
    
    private Queue<String> queueFromFile;
    
    private DBStatsUploader dbStatsUploader = new DBStatsUploader();
    
    @Override
    public String syncData() {
        if (aboutWhat.isEmpty() || !aboutWhat.matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
            throw new IllegalArgumentException(aboutWhat);
        }
    
        Path rootPath = Paths.get(".");
        String statFile = ConstantsFor.FILESYSTEM_SEPARATOR + ConstantsFor.STR_INETSTATS + ConstantsFor.FILESYSTEM_SEPARATOR + aboutWhat.replaceAll("_", ".") + ".csv";
        String inetStatsPath = rootPath.toAbsolutePath().normalize().toString() + statFile;
    
        this.aboutWhat = inetStatsPath;
        this.queueFromFile = getLimitQueueFromFile(Paths.get(inetStatsPath));
    
        return uploadToTable();
    }
    
    private @NotNull Queue<String> getLimitQueueFromFile(Path inetStatsPath) {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        Queue<String> statAbout = FileSystemWorker.readFileToQueue(inetStatsPath);
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.STR_INETSTATS);
             PreparedStatement preparedStatement = connection
                 .prepareStatement("select idrec from " + aboutWhat.replaceAll("\\Q.\\E", "_") + " ORDER BY idrec DESC LIMIT 1");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                int idrec = resultSet.getInt("idrec");
                for (int i = 0; i < idrec; i++) {
                    statAbout.poll();
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 88 ***");
        }
        return statAbout;
    }
    
    private void getTableName(@NotNull Path rootPath) {
        File[] inetFiles = Paths.get(rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + ConstantsFor.STR_INETSTATS).toFile()
                .listFiles();
        for (File statCsv : inetFiles) {
            String tableName = statCsv.getName().replaceAll("\\Q.\\E", "_").replace("_csv", "");
            dbStatsUploader.setClassOption(tableName);
            makeTable(tableName);
        }
    }
    
    private @NotNull String uploadToTable() {
        while (!queueFromFile.isEmpty()) {
            String entryStat = queueFromFile.poll();
            if (entryStat.isEmpty() || !entryStat.contains(",")) {
                continue;
            }
            String[] valuesArr = entryStat.split(",");
            parseQueue(valuesArr);
        }
        return "inetStatsPath";
    }
    
    private void parseQueue(@NotNull String[] valuesArr) {
        dbStatsUploader.setClassOption(valuesArr);
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", SyncWithRegRu.class.getSimpleName() + "[\n", "\n]")
                .add("aboutWhat = '" + aboutWhat + "'")
                .toString();
    }
    
    private void makeTable(String name) {
        String[] sqlS = {
                ConstantsFor.SQL_ALTERTABLE + name + "\n" +
                "  ADD PRIMARY KEY (`idrec`),\n" +
                "  ADD UNIQUE KEY `stamp` (`stamp`,`ip`,`bytes`) USING BTREE,\n" +
                "  ADD KEY `ip` (`ip`);",
        
                ConstantsFor.SQL_ALTERTABLE + name + "\n" +
                "  MODIFY `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '';"};
        dbStatsUploader.createUploadStatTable(sqlS);
    }
    
    private @NotNull String makePathStr() {
        Path rootPath = Paths.get(".");
        rootPath = Paths.get(rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + ConstantsFor.STR_INETSTATS);
        
        makeTable(rootPath.toAbsolutePath().normalize().toString());
        return rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + aboutWhat.replaceAll("_", ".") + ".csv";
    }
}
