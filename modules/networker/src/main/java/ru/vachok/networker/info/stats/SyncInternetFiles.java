package ru.vachok.networker.info.stats;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Queue;
import java.util.StringJoiner;


/**
 @since 08.09.2019 (14:55) */
@SuppressWarnings("InstanceVariableOfConcreteClass")
public class SyncInternetFiles implements Stats {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, SyncInternetFiles.class.getSimpleName());
    
    private String aboutWhat;
    
    private DBStatsUploader dbStatsUploader = new DBStatsUploader();
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        throw new TODOException("ru.vachok.networker.info.stats.SyncInternetFiles.getInfoAbout( String ) at 08.09.2019 - (14:56)");
    }
    
    @Override
    public String getInfo() {
        return uploadToTable();
    }
    
    private @NotNull String uploadToTable() {
        Path rootPath = Paths.get(".");
        getTableName(rootPath);
        String inetStatsPath = rootPath.toAbsolutePath().normalize()
            .toString() + ConstantsFor.FILESYSTEM_SEPARATOR + "inetstats" + ConstantsFor.FILESYSTEM_SEPARATOR + aboutWhat.replaceAll("_", ".") + ".csv";
        Queue<String> statAbout = limitedQueue(inetStatsPath);
        while (!statAbout.isEmpty()) {
            String entryStat = statAbout.poll();
            if (entryStat.isEmpty() || !entryStat.contains(",")) {
                continue;
            }
            String[] valuesArr = entryStat.split(",");
            parseQueue(valuesArr);
        }
        return inetStatsPath;
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
    
    private @NotNull Queue<String> limitedQueue(String inetStatsPath) {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        Paths.get(inetStatsPath);
        Queue<String> statAbout = FileSystemWorker.readFileToQueue(Paths.get(inetStatsPath));
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
    
    private void parseQueue(@NotNull String[] valuesArr) {
        dbStatsUploader.setClassOption(valuesArr);
        dbStatsUploader.getInfo();
    }
    
    private void makeTable(String name) {
        String[] sqlS = {
            "ALTER TABLE " + name + "\n" +
                "  ADD PRIMARY KEY (`idrec`),\n" +
                "  ADD UNIQUE KEY `stamp` (`stamp`,`ip`,`bytes`) USING BTREE,\n" +
                "  ADD KEY `ip` (`ip`);",
            
            "ALTER TABLE " + name + "\n" +
                "  MODIFY `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '';"};
        dbStatsUploader.createUploadStatTable(sqlS);
    }
    
    @Override
    public void setClassOption(Object option) {
        this.aboutWhat = (String) option;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", SyncInternetFiles.class.getSimpleName() + "[\n", "\n]")
            .add("aboutWhat = '" + aboutWhat + "'")
            .toString();
    }
    
    private @NotNull String makePathStr() {
        Path rootPath = Paths.get(".");
        rootPath = Paths.get(rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + ConstantsFor.STR_INETSTATS);
        
        makeTable(rootPath.toAbsolutePath().normalize().toString());
        return rootPath.toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + aboutWhat.replaceAll("_", ".") + ".csv";
    }
}
