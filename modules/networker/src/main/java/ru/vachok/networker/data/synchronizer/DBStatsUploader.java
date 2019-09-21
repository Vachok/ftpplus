package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @see DBStatsUploaderTest
 @since 08.09.2019 (10:08) */
class DBStatsUploader extends SyncData {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DBStatsUploader.class.getSimpleName());
    
    private String databaseTable;
    
    private Deque<String> fromFileToJSON = new ConcurrentLinkedDeque<>();
    
    private LocalFileWorker localFileWorker = new LocalFileWorker();
    
    DBStatsUploader() {
    }
    
    DBStatsUploader(@NotNull String syncDB) {
        if (syncDB.matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
            this.databaseTable = ConstantsFor.DB_INETSTATS + syncDB.replaceAll("\\Q.\\E", "_");
        }
        else {
            this.databaseTable = syncDB;
        }
    }
    
    public String getDatabaseTable() {
        return databaseTable;
    }
    
    public void setOption(Deque<String> option) {
        localFileWorker.setFromFileToJSON(option);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBStatsUploader{");
        sb.append("localFileWorker=").append(localFileWorker.toString());
        sb.append(", fromFileToJSON=").append(fromFileToJSON);
        sb.append(", databaseTable='").append(databaseTable).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    /**
     @return results count
     
     @see DBStatsUploaderTest#testSyncData()
     */
    @Override
    public String syncData() {
        if (databaseTable.isEmpty()) {
            throw new IllegalArgumentException(MessageFormat.format("Table {0} is null or illegal ", databaseTable));
        }
        if (fromFileToJSON.size() == 0) {
            makeTable(databaseTable);
        }
        return MessageFormat.format("Upload: {0} rows to {1}", uploadFromJSON(), databaseTable);
    }
    
    @Override
    public void setOption(Object option) {
        this.databaseTable = (String) option;
    }
    
    /**
     @see DBStatsUploaderTest#testSuperRun()
     */
    @Override
    public void superRun() {
        Path rootPath = Paths.get(".");
        rootPath = Paths.get(rootPath.toAbsolutePath().normalize() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.DIR_INETSTATS);
        File[] allStatFiles = rootPath.toAbsolutePath().normalize().toFile().listFiles();
        for (File stat : allStatFiles) {
            localFileWorker.setAbsNormalGeneralFile(stat.toPath());
            this.databaseTable = ConstantsFor.DB_INETSTATS + stat.getName().replace(".csv", "").replaceAll("\\Q.\\E", "_");
            uploadCollection(FileSystemWorker.readFileToList(stat.getAbsolutePath()), databaseTable);
        }
        
    }
    
    /**
     @param stringsCollection строки из файла, для загрузки.
     @param tableName имя sql, database.table
     @return PreparedStatement#executeUpdate()
     
     @see DBStatsUploaderTest#testUploadFileTo()
     */
    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        List<String> toJSON = new ArrayList<>(stringsCollection);
        Collections.sort(toJSON);
        this.databaseTable = tableName;
        for (String s : toJSON) {
            if (!s.isEmpty()) {
                fromFileToJSON.addFirst(localFileWorker.convertToJSON(s));
            }
        }
        List<String> sortedCollection = new ArrayList<>(fromFileToJSON);
        Collections.sort(sortedCollection);
        Collections.reverse(sortedCollection);
        fromFileToJSON.clear();
        sortedCollection.forEach(s->fromFileToJSON.addFirst(s));
        localFileWorker.setFromFileToJSON(fromFileToJSON);
        try {
            checkDeqSize();
        }
        catch (NoSuchElementException ignore) {
            //16.09.2019 (11:19)
        }
        return uploadFromJSON();
    }
    
    @Override
    public Deque<String> getFromFileToJSON() {
        return fromFileToJSON;
    }
    
    @Override
    String getDbToSync() {
        return databaseTable;
    }
    
    @Override
    public void setDbToSync(String dbToSync) {
        this.databaseTable = dbToSync;
    }
    
    @Override
    Map<String, String> makeColumns() {
        Map<String, String> colMap = new HashMap<>();
        colMap.put(ConstantsFor.DBCOL_IDREC, "mediumint(11)");
        colMap.put(ConstantsFor.DBCOL_STAMP, ConstantsFor.BIGINT_13);
        colMap.put(ConstantsFor.DBCOL_SQUIDANS, ConstantsFor.VARCHAR_20);
        colMap.put(ConstantsFor.DBCOL_BYTES, "int(11)");
        colMap.put(ConstantsFor.DBCOL_TIMESPEND, "int(11)");
        colMap.put("site", ConstantsFor.VARCHAR_190);
        return colMap;
    }
    
    protected int makeTable(@NotNull String name) {
        if (!name.contains(".") || name.matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
            name = ConstantsFor.DB_INETSTATS + name.toLowerCase().replaceAll("\\Q.\\E", "_");
        }
        setDbToSync(name);
        String[] sqlS = {
                ConstantsFor.SQL_ALTERTABLE + getDbToSync() + "\n" +
                        "  ADD PRIMARY KEY (`idrec`),\n" +
                        "  ADD UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,\n" +
                        "  ADD KEY `site` (`site`);",
        
                ConstantsFor.SQL_ALTERTABLE + getDbToSync() + "\n" +
                        "  MODIFY `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '';"};
        return createUploadStatTable(sqlS);
    }
    
    private int uploadToTable(@NotNull JsonObject jsonObject) {
        int retInt = 0;
        try (Connection connection = CONNECT_TO_LOCAL.getDefaultConnection(FileNames.DIR_INETSTATS)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("insert into " + getDbToSync() + " (stamp, squidans, bytes, site) values (?,?,?,?)")) {
                preparedStatement.setLong(1, Long.parseLong(jsonObject.getString(ConstantsFor.DBCOL_STAMP, String.valueOf(System.currentTimeMillis()))));
                String squidAns = jsonObject.getString(ConstantsFor.DBCOL_SQUIDANS, "NO ANSWER!");
                if (squidAns.length() >= 20) {
                    squidAns = squidAns.substring(Math.abs(20 - squidAns.length()));
                }
                preparedStatement.setString(2, squidAns);
                preparedStatement.setInt(3, Integer.parseInt(jsonObject.getString(ConstantsFor.DBCOL_BYTES, "0")));
                String site = jsonObject.getString("site", ConstantsFor.SITE_VELKOMFOOD);
                if (site.length() >= 190) {
                    site = site.substring(0, 189);
                }
                preparedStatement.setString(4, site);
                retInt += preparedStatement.executeUpdate();
            }
            catch (NumberFormatException ignore) {
                //17.09.2019 (4:38)
            }
        }
        catch (RuntimeException ignore) {
            retInt -= 1;
            localFileWorker.renewCopyFile(fromFileToJSON);
        }
        catch (SQLException e) {
            String message = e.getMessage();
            if (!message.contains(ConstantsFor.ERROR_DUPLICATEENTRY)) {
                messageToUser.error(this.getClass().getSimpleName(), jsonObject.toString(), e.getMessage() + " see line: 275");
            }
            else {
                retInt += 1;
                messageToUser.info(this.getClass().getSimpleName(), MessageFormat.format("{0}: {1} elements remain", databaseTable, fromFileToJSON.size()), e.getMessage());
                localFileWorker.renewOriginFile(fromFileToJSON);
            }
        }
        return retInt;
    }
    
    private int checkStamp() {
        int retInt;
    
        long fileStamp = Long.parseLong(Json.parse(fromFileToJSON.getLast()).asObject().getString(ConstantsFor.DBCOL_STAMP, "0"));
        long dbStamp = getLastStamp();
        if (fileStamp > dbStamp) {
            retInt = 0;
        }
        else {
            fromFileToJSON.clear();
            SyncData downloader = SyncData.getInstance(DOWNLOADER);
            downloader.setDbToSync(databaseTable);
            downloader.superRun();
            retInt = FileSystemWorker.countStringsInFile(Paths.get(databaseTable + FileNames.EXT_TABLE));
        }
        return retInt;
    }
    
    private long getLastStamp() {
        long retLong = System.currentTimeMillis();
        String sql = "select stamp from " + databaseTable + " order by stamp desc limit 1";
        try (Connection connection = CONNECT_TO_LOCAL.getDefaultConnection(databaseTable);
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                if (resultSet.last()) {
                    retLong = resultSet.getLong(ConstantsFor.DBCOL_STAMP);
                }
            }
        }
        catch (SQLException e) {
            retLong = 1;
        }
        return retLong;
    }
    
    private void checkDeqSize() {
        int locID = getLastLocalID(databaseTable);
        if (locID == -666) {
            locID = makeTable(databaseTable);
        }
        int deqSize = fromFileToJSON.size();
        int diff = deqSize - locID;
        int recordsDeleteCount = 0;
        if (diff == 0) {
            localFileWorker.renewOriginFile(fromFileToJSON);
        }
        else if (diff > 0) {
            recordsDeleteCount = Math.abs(diff - deqSize);
        }
        else {
            recordsDeleteCount = checkStamp();
        }
        messageToUser.warn(this.getClass().getSimpleName(), "checkDeqSize", MessageFormat.format("difference with {0} = {1}", databaseTable, recordsDeleteCount));
        for (int i = 0; i < recordsDeleteCount; i++) {
            fromFileToJSON.removeFirst();
        }
    }
    
    @Contract(pure = true)
    private int uploadFromJSON() {
        int retInt = 1;
        while (!fromFileToJSON.isEmpty()) {
            String jsStr = fromFileToJSON.removeFirst();
            retInt = fromFileToJSON.size();
            JsonObject jsonObject = localFileWorker.parseJSONObj(jsStr);
            retInt += uploadToTable(jsonObject);
        }
        return retInt;
    }
    
    private int createUploadStatTable(String[] sql) {
        try (Connection connection = CONNECT_TO_LOCAL.getDefaultConnection(databaseTable)) {
            try (PreparedStatement preparedStatementCreateTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + getDbToSync() + "(\n" +
                    "  `idrec` mediumint(11) unsigned NOT NULL COMMENT '',\n" +
                    "  `stamp` bigint(13) unsigned NOT NULL COMMENT '',\n" +
                    "  `squidans` varchar(20) NOT NULL COMMENT '',\n" +
                    "  `bytes` int(11) NOT NULL COMMENT '',\n" +
                    "  `timespend` int(11) NOT NULL DEFAULT '-666',\n" +
                    "  `site` varchar(190) NOT NULL COMMENT ''\n" +
                    ") ENGINE=MyISAM DEFAULT CHARSET=utf8;");
            ) {
                if (preparedStatementCreateTable.executeUpdate() == 0) {
                    for (String sqlCom : sql) {
                        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlCom)) {
                            messageToUser.info("preparedStatement", getDbToSync(), String.valueOf(preparedStatement.executeUpdate()));
                        }
                    }
                }
            }
        }
        catch (SQLException | RuntimeException e) {
            if (!fromFileToJSON.isEmpty()) {
                return uploadFromJSON();
            }
            else {
                fromFileToJSON.addAll(FileSystemWorker.readFileToList(databaseTable.split("\\Q.\\E")[1].replaceAll("_", ".") + ".csv"));
            }
        }
        return 0;
    }
    
}