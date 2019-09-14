package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @see DBStatsUploaderTest
 @since 08.09.2019 (10:08) */
class DBStatsUploader extends SyncData {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DBStatsUploader.class.getSimpleName());
    
    private String syncTable = "";
    
    private String[] classOpt;
    
    private Deque<String> fromFileToJSON = new ConcurrentLinkedDeque<>();
    
    public void setOption(String[] option) {
        this.classOpt = option;
    }
    
    public void setOption(Deque<String> option) {
        this.fromFileToJSON = option;
    }
    
    @Override
    public String syncData() {
        if (syncTable.isEmpty()) {
            throw new IllegalArgumentException(MessageFormat.format("Table {0} is null or illegal ", syncTable));
        }
        if (fromFileToJSON.size() == 0) {
            makeTable(syncTable);
        }
        return MessageFormat.format("Upload: {0} rows to {1}", uploadToTableIP(), syncTable);
    }
    
    @Override
    public void setOption(Object option) {
        this.syncTable = (String) option;
    }
    
    @Override
    public String toString() {
        try {
            final StringBuilder sb = new StringBuilder("DBStatsUploader{");
            sb.append("syncTable='").append(syncTable).append('\'');
            sb.append(", fromFileToJSON=").append(fromFileToJSON);
            sb.append(", classOpt=").append(Arrays.toString(classOpt));
            sb.append('}');
            return sb.toString();
        }
        catch (RuntimeException e) {
            return e.getMessage();
        }
    }
    
    @Override
    public int uploadFileTo(Collection stringsCollection, String tableName) {
        List<String> toJSON = new ArrayList<>(stringsCollection);
        setDbToSync(tableName);
        for (String s : toJSON) {
            if (!s.isEmpty()) {
                fromFileToJSON.addFirst(convertToJSON(s));
            }
        }
        return uploadToTableIP();
    }
    
    private @NotNull String convertToJSON(@NotNull String stringFromUserIPInetStatisticsFile) {
        JsonObject jsonObject = new JsonObject();
        String[] splittedString = stringFromUserIPInetStatisticsFile.split(",");
        if (splittedString.length < 4) {
            jsonObject.add(ConstantsFor.STR_ERROR, stringFromUserIPInetStatisticsFile);
            return jsonObject.toString();
        }
        long timeStamp = parseStamp(splittedString[0]);
        jsonObject.add(ConstantsFor.DBCOL_STAMP, String.valueOf(timeStamp));
        jsonObject.add(ConstantsFor.DBCOL_RESPONSE, splittedString[1]);
        jsonObject.add(ConstantsFor.DBCOL_BYTES, splittedString[2]);
        jsonObject.add("site", splittedString[3]);
        return jsonObject.toString();
    }
    
    void superRun() {
        DBStatsUploader dbStatsUploader = new DBStatsUploader();
        File[] allStatFiles = new File(".\\inetstats").listFiles();
        for (File stat : allStatFiles) {
            dbStatsUploader.setOption(stat.getName().replace(".csv", ""));
            String syncStr = dbStatsUploader.syncData();
            messageToUser.info("syncStr = " + syncStr);
        }
    
    }
    
    private long parseStamp(@NotNull String strToParse) {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
        Date parsedDate;
        try {
            parsedDate = format.parse(strToParse);
        }
        catch (ParseException e) {
            return System.currentTimeMillis();
        }
        return parsedDate.getTime();
    }
    
    protected int makeTable(@NotNull String name) {
        setDbToSync(ConstantsFor.DB_INETSTATS + name.toLowerCase().replaceAll("\\Q.\\E", "_"));
        String[] sqlS = {
            ConstantsFor.SQL_ALTERTABLE + getDbToSync() + "\n" +
                "  ADD PRIMARY KEY (`idrec`),\n" +
                "  ADD UNIQUE KEY `stamp` (`stamp`,`site`,`bytes`) USING BTREE,\n" +
                "  ADD KEY `site` (`site`);",
    
            ConstantsFor.SQL_ALTERTABLE + getDbToSync() + "\n" +
                "  MODIFY `idrec` mediumint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '';"};
        return createUploadStatTable(sqlS);
    }
    
    private int createUploadStatTable(String[] sql) {
        try (Connection connection = CONNECT_TO_LOCAL.getDefaultConnection(ConstantsFor.STR_INETSTATS)) {
            try (PreparedStatement preparedStatementCreateTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + getDbToSync() + "(\n" +
                "  `idrec` mediumint(11) unsigned NOT NULL COMMENT '',\n" +
                "  `stamp` bigint(13) unsigned NOT NULL COMMENT '',\n" +
                "  `squidans` varchar(20) NOT NULL COMMENT '',\n" +
                "  `bytes` int(11) NOT NULL COMMENT '',\n" +
                "  `timespend` int(11) NOT NULL DEFAULT '0',\n" +
                "  `site` varchar(190) NOT NULL COMMENT ''\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
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
        catch (SQLException e) {
            return readDirectlyFile();
        }
        return 0;
    }
    
    @Contract(pure = true)
    private int uploadFromJSON() {
        int retInt = fromFileToJSON.size();
        while (!fromFileToJSON.isEmpty()) {
            String jsStr = fromFileToJSON.removeFirst();
            retInt = fromFileToJSON.size();
            JsonObject jsonObject = parseJSONObj(jsStr);
            retInt += uploadToTableAsJSON(Objects.requireNonNull(jsonObject, jsStr));
        }
        return retInt;
    }
    
    private JsonObject parseJSONObj(@NotNull String jsStr) {
        JsonObject object = new JsonObject();
        try {
            object = Json.parse(jsStr.replace("},", "}")).asObject();
        }
        catch (com.eclipsesource.json.ParseException e) {
            object.set(PropertiesNames.ERROR, e.getMessage());
            messageToUser.error(e.getMessage() + " see line: 209 ***");
        }
        return object;
    }
    
    private int uploadToTableAsJSON(@NotNull JsonObject object) {
        String[] names = new String[object.names().size()];
        this.classOpt = new String[object.names().size()];
        
        try {
            for (int i = 0; i < object.names().size(); i++) {
                String name = object.names().get(i);
                names[i] = name;
                classOpt[i] = object.getString(name, name);
            }
        }
        catch (RuntimeException ignore) {
            //11.09.2019 (12:13)
        }
        final String sql = buildSqlString(names);
        int executeUpdate = -142;
        MysqlDataSource dSource = CONNECT_TO_LOCAL.getDataSource();
        dSource.setDatabaseName("inet");
        getCreateQuery(syncTable, makeColumns());
        try (Connection connection = dSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            executeUpdate = preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            if (!e.getMessage().contains(ConstantsFor.STR_DUPLICATE)) {
                System.out.println("sql = " + sql);
                messageToUser.error(e.getMessage() + " see line: 268 ***");
                executeUpdate = -268;
            }
        }
        return executeUpdate;
    }
    
    private int readDirectlyFile() {
        int retInt = 0;
        Path root = Paths.get(".").toAbsolutePath().normalize();
        Queue<String> fileAsLi = FileSystemWorker
            .readFileToQueue(Paths.get(root + ConstantsFor.FILESYSTEM_SEPARATOR + ConstantsFor.STR_INETSTATS + ConstantsFor.FILESYSTEM_SEPARATOR + syncTable
                .replace(ConstantsFor.DB_INETSTATS, "")
                .replaceAll("_", ".") + ".csv"));
    
        int databaseID = getLastLocalID(getDbToSync());
        int locID = fileAsLi.size() - databaseID;
        if (locID <= 0) {
            return 1;
        }
        else {
            for (int i = 0; i < databaseID; i++) {
                fileAsLi.remove();
            }
            messageToUser.warn(MessageFormat.format("Sync: {0} entries!", locID));
            while (!fileAsLi.isEmpty()) {
                String recordLog = fileAsLi.remove();
                if (!recordLog.isEmpty()) {
                    setOption(recordLog.split(","));
                    retInt += uploadToTableIP();
                }
            }
            return retInt;
        }
    }
    
    @Override
    Map<String, String> makeColumns() {
        Map<String, String> colMap = new HashMap<>();
        colMap.put(ConstantsFor.DBCOL_IDREC, "mediumint(11)");
        colMap.put(ConstantsFor.DBCOL_STAMP, "bigint(13)");
        colMap.put(ConstantsFor.DBCOL_SQUIDANS, "varchar(20)");
        colMap.put(ConstantsFor.DBCOL_BYTES, "int(11)");
        colMap.put(ConstantsFor.DBCOL_TIMESPEND, "int(11)");
        colMap.put("site", "varchar(190)");
        return colMap;
    }
    
    private int uploadToTableIP() {
        int retInt = 0;
        if (classOpt == null) {
            retInt = uploadFromJSON();
        }
        else {
            String[] valuesArr = classOpt;
            try (Connection connection = CONNECT_TO_LOCAL.getDefaultConnection(ConstantsFor.STR_INETSTATS)) {
                
                try (PreparedStatement preparedStatement = connection
                    .prepareStatement("insert into " + getDbToSync() + "(stamp, squidans , bytes, site) values (?,?,?,?)")) {
                    preparedStatement.setLong(1, parseStamp(valuesArr[0]));
                    preparedStatement.setString(2, valuesArr[1]);
                    preparedStatement.setInt(3, Integer.parseInt(valuesArr[2]));
                    preparedStatement.setString(4, valuesArr[4]);
                    retInt += preparedStatement.executeUpdate();
                }
                catch (IndexOutOfBoundsException ignore) {
                    //14.09.2019 (13:44)
                }
            }
            catch (SQLException e) {
                String message = e.getMessage();
                if (!message.contains("Duplicate entry")) {
                    messageToUser.error(e.getMessage() + " see line: 103 ***");
                }
                retInt = -103;
            }
        }
        return retInt;
    }
    
    private @NotNull String buildSqlString(String[] names) {
        StringBuilder stringBuilder = new StringBuilder().append("insert into ")
            .append(getDbToSync())
            .append(" (")
            .append(Arrays.toString(names).replace("[", "").replace("]", ""))
            .append(") values (");
        for (int i = 0; i < names.length; i++) {
            stringBuilder.append("'").append(classOpt[i]).append("', ");
        }
        stringBuilder.replace(stringBuilder.length() - 2, stringBuilder.length(), ")");
        return stringBuilder.toString();
    }
    
    @Override
    String getDbToSync() {
        return syncTable;
    }
    
    @Override
    public void setDbToSync(String dbToSync) {
        this.syncTable = dbToSync;
    }
    
}