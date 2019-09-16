package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.sql.*;
import java.text.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @see DBStatsUploaderTest
 @since 08.09.2019 (10:08) */
class DBStatsUploader extends SyncData {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DBStatsUploader.class.getSimpleName());
    
    private String databaseTable;
    
    private String[] classOpt;
    
    private Deque<String> fromFileToJSON = new ConcurrentLinkedDeque<>();
    
    public DBStatsUploader(String syncDB) {
        this.databaseTable = syncDB;
    }
    
    public void setOption(String[] option) {
        this.classOpt = option;
    }
    
    public void setOption(Deque<String> option) {
        this.fromFileToJSON = option;
    }
    
    @Override
    public String toString() {
        try {
            final StringBuilder sb = new StringBuilder("DBStatsUploader{");
            sb.append("syncTable='").append(databaseTable).append('\'');
            sb.append(", fromFileToJSON=").append(fromFileToJSON.size());
            sb.append(", classOpt=").append(Arrays.toString(classOpt));
            sb.append('}');
            return sb.toString();
        }
        catch (RuntimeException e) {
            return e.getMessage();
        }
    }
    
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
    
    @Override
    public int uploadFileTo(Collection stringsCollection, String tableName) {
        List<String> toJSON = new ArrayList<>(stringsCollection);
        Collections.sort(toJSON);
        this.databaseTable = tableName;
        for (String s : toJSON) {
            if (!s.isEmpty()) {
                fromFileToJSON.addFirst(convertToJSON(s));
            }
        }
        List<String> sortedCollection = new ArrayList<>(fromFileToJSON);
        Collections.sort(sortedCollection);
        Collections.reverse(sortedCollection);
        fromFileToJSON.clear();
        sortedCollection.forEach(s->fromFileToJSON.addFirst(s));
        try {
            checkDeqSize();
        }
        catch (NoSuchElementException ignore) {
            //16.09.2019 (11:19)
        }
        return uploadFromJSON();
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
    
    @Override
    public void superRun() {
        File[] allStatFiles = new File(".\\inetstats").listFiles();
        for (File stat : allStatFiles) {
            this.databaseTable = ConstantsFor.DB_INETSTATS + stat.getName().replace(".csv", "").replaceAll("\\Q.\\E", "_");
            uploadFileTo(FileSystemWorker.readFileToList(stat.getAbsolutePath()), databaseTable);
        }
        
    }
    
    private @NotNull String convertToJSON(@NotNull String stringFromUserIPInetStatisticsFile) {
        JsonObject jsonObject = new JsonObject();
        String[] splittedString = stringFromUserIPInetStatisticsFile.split(",");
        if (splittedString.length < 4) {
            jsonObject.add(ConstantsFor.STR_ERROR, stringFromUserIPInetStatisticsFile);
            return jsonObject.toString();
        }
        long timeStamp = parseStamp(splittedString[0]);
        try {
            jsonObject.add(ConstantsFor.DBCOL_STAMP, String.valueOf(timeStamp));
            jsonObject.add(ConstantsFor.DBCOL_SQUIDANS, splittedString[1]);
            jsonObject.add(ConstantsFor.DBCOL_BYTES, splittedString[2]);
            jsonObject.add("site", splittedString[4]);
        }
        catch (IndexOutOfBoundsException ignore) {
            //16.09.2019 (11:24)
        }
        return jsonObject.toString();
    }
    
    private void checkDeqSize() {
        int locID = getLastLocalID(databaseTable);
        int deqSize = fromFileToJSON.size();
        int diff = deqSize - locID;
        if (diff <= 0) {
            fromFileToJSON.clear();
        }
        int recordsDeleteCount = Math.abs(diff - deqSize);
        for (int i = 0; i < recordsDeleteCount; i++) {
            fromFileToJSON.removeFirst();
        }
    }
    
    @Contract(pure = true)
    private int uploadFromJSON() {
        int retInt = fromFileToJSON.size();
        while (!fromFileToJSON.isEmpty()) {
            String jsStr = fromFileToJSON.removeFirst();
            retInt = fromFileToJSON.size();
            JsonObject jsonObject = parseJSONObj(jsStr);
            retInt += uploadToTable(jsonObject);
        }
        return retInt;
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
    
    private JsonObject parseJSONObj(@NotNull String jsStr) {
        JsonObject object = new JsonObject();
        try {
            object = Json.parse(jsStr.replace("},", "}")).asObject();
        }
        catch (com.eclipsesource.json.ParseException ignore) {
            //16.09.2019 (11:02)
        }
        return object;
    }
    
    private int uploadToTable(@NotNull JsonObject jsonObject) {
        int retInt = 0;
        try (Connection connection = CONNECT_TO_LOCAL.getDefaultConnection(ConstantsFor.STR_INETSTATS)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("insert into " + getDbToSync() + " (stamp, squidans, bytes, site) values (?,?,?,?)")) {
                preparedStatement.setLong(1, Long.parseLong(jsonObject.getString(ConstantsFor.DBCOL_STAMP, String.valueOf(System.currentTimeMillis()))));
                preparedStatement.setString(2, jsonObject.getString(ConstantsFor.DBCOL_SQUIDANS, ""));
                preparedStatement.setInt(3, Integer.parseInt(jsonObject.getString(ConstantsFor.DBCOL_BYTES, "0")));
                preparedStatement.setString(4, jsonObject.getString("site", ""));
                retInt += preparedStatement.executeUpdate();
            }
            catch (IndexOutOfBoundsException | NumberFormatException ignore) {
                //14.09.2019 (13:44)
            }
        }
        catch (SQLException e) {
            String message = e.getMessage();
            if (!message.contains(ConstantsFor.ERROR_DUPLICATEENTRY)) {
                messageToUser.error(this.getClass().getSimpleName(), jsonObject.toString(), e.getMessage() + " see line: 275");
            }
            else {
                retInt += 1;
            }
        }
        
        return retInt;
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
    
    private int createUploadStatTable(String[] sql) {
        try (Connection connection = CONNECT_TO_LOCAL.getDefaultConnection(ConstantsFor.STR_INETSTATS)) {
            try (PreparedStatement preparedStatementCreateTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + getDbToSync() + "(\n" +
                    "  `idrec` mediumint(11) unsigned NOT NULL COMMENT '',\n" +
                    "  `stamp` bigint(13) unsigned NOT NULL COMMENT '',\n" +
                    "  `squidans` varchar(20) NOT NULL COMMENT '',\n" +
                    "  `bytes` int(11) NOT NULL COMMENT '',\n" +
                    "  `timespend` int(11) NOT NULL DEFAULT '0',\n" +
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
        catch (SQLException e) {
            return uploadFromJSON();
        }
        return 0;
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
    
}