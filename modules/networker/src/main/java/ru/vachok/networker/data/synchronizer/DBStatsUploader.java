package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageToUser;

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
    
    private String syncTable = getDbToSync();
    
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
        else if (fromFileToJSON.size() == 0) {
            throw new IllegalArgumentException(MessageFormat.format("Nothing to sync... {0} = fromFileToJSON", fromFileToJSON.size()));
        }
        return MessageFormat.format("Upload: {0} rows to {1}", uploadToTable(), syncTable);
    }
    
    @Override
    public void setOption(Object option) {
        this.syncTable = (String) option;
    }
    
    private int uploadToTable() {
        int retInt = 0;
        if (classOpt == null) {
            retInt = uploadFromJSON();
        }
        else {
            String[] valuesArr = classOpt;
        
            try (Connection connection = CONNECT_TO_LOCAL.getDefaultConnection(ConstantsFor.STR_INETSTATS)) {
                try (PreparedStatement preparedStatement = connection
                        .prepareStatement("insert into " + syncTable.replaceAll("\\Q.\\E", "_") + "(stamp, ip, bytes, site) values (?,?,?,?)")) {
                    preparedStatement.setLong(1, parseStamp(valuesArr[0]));
                    preparedStatement.setString(2, valuesArr[1]);
                    preparedStatement.setInt(3, Integer.parseInt(valuesArr[2]));
                    preparedStatement.setString(4, valuesArr[3]);
                    retInt = preparedStatement.executeUpdate();
                }
                catch (IndexOutOfBoundsException e) {
                    messageToUser.error(e.getMessage() + " see line: 71");
                    retInt = -71;
                }
            }
            catch (SQLException e) {
                String message = e.getMessage();
                if (!message.contains("Duplicate entry")) {
                    messageToUser.error(e.getMessage() + " see line: 60 ***");
                }
                retInt = -60;
            }
        }
        return retInt;
    }
    
    @Contract(pure = true)
    private int uploadFromJSON() {
        int retInt = fromFileToJSON.size();
        StringBuilder stringBuilder = new StringBuilder();
        while (!fromFileToJSON.isEmpty()) {
            String jsStr = fromFileToJSON.removeFirst();
            retInt = fromFileToJSON.size();
            JsonObject jsonObject = parseJSONObj(jsStr);
            retInt += uploadToTableAsJSON(Objects.requireNonNull(jsonObject, jsStr));
        }
        return retInt;
    }
    
    private long parseStamp(@NotNull String strToParse) {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
        Date parsedDate = new Date();
        try {
            
            parsedDate = format.parse(strToParse);
            System.out.println("parsedDate = " + parsedDate);
        }
        catch (ParseException e) {
            messageToUser.error(e.getMessage() + " see line: 76 ***");
        }
        return parsedDate.getTime();
    }
    
    private JsonObject parseJSONObj(@NotNull String jsStr) {
        JsonObject object = new JsonObject();
        try {
            object = Json.parse(jsStr.replace("},", "}")).asObject();
        }
        catch (com.eclipsesource.json.ParseException e) {
            object.set(PropertiesNames.ERROR, e.getMessage());
            messageToUser.error(e.getMessage() + " see line: 120");
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
        int executeUpdate = -117;
        MysqlDataSource dSource = CONNECT_TO_LOCAL.getDataSource();
        dSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        try (Connection connection = dSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            executeUpdate = preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println("sql = " + sql);
            messageToUser.error(e.getMessage() + " see line: 126");
        }
        return executeUpdate;
    }
    
    private @NotNull String buildSqlString(String[] names) {
        StringBuilder stringBuilder = new StringBuilder().append("insert into ")
                .append(syncTable.replaceAll("\\Q.\\E", "_"))
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
    public String toString() {
        String toStr;
        try {
            toStr = new StringJoiner(",\n", DBStatsUploader.class.getSimpleName() + "[\n", "\n]")
                    .add("syncTable = '" + syncTable + "'")
                    .add("classOpt = " + Arrays.toString(Objects.requireNonNull(classOpt, "classOpt is null")))
                    .add("fromFileToJSON = " + fromFileToJSON)
                    .add("CONNECT_TO = " + CONNECT_TO_LOCAL.getDataSource().getURL())
                    .toString();
        }
        catch (RuntimeException e) {
            toStr = MessageFormat.format("DBStatsUploader.toString: {0}, ({1})", e.getMessage(), e.getClass().getName());
            messageToUser.error(toStr);
        }
        return toStr;
    }
}