package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 @see InternetSyncTest
 @since 13.10.2019 (13:21) */
public class InternetSync extends SyncData {
    
    
    private String ipAddr;
    
    private Connection connection;
    
    private String dbFullName;
    
    InternetSync(@NotNull String type) {
        super();
        this.ipAddr = type;
        this.dbFullName = ConstantsFor.DB_INETSTATS + ipAddr.replaceAll("\\Q.\\E", "_");
        this.connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(dbFullName);
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", InternetSync.class.getSimpleName() + "[\n", "\n]")
            .add("ipAddr = '" + ipAddr + "'")
            .add("dbFullName = '" + dbFullName + "'")
            .toString();
    }
    
    @Override
    String getDbToSync() {
        return ipAddr;
    }
    
    @Override
    public void setDbToSync(String dbToSync) {
        this.ipAddr = dbToSync;
    }
    
    @Override
    public void setOption(Object option) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.InternetSync.setOption( void ) at 13.10.2019 - (13:21)");
    }
    
    @Override
    public String syncData() {
        Path rootPath = Paths.get(".");
        Path filePath = Paths.get(rootPath.toAbsolutePath().normalize()
            .toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.DIR_INETSTATS + ConstantsFor.FILESYSTEM_SEPARATOR + ipAddr + ".csv");
        int jsonCreated = createJSON(FileSystemWorker.readFileToQueue(filePath));
        return MessageFormat.format("{0} created {1} rows", renameToTXT(filePath), jsonCreated);
    }
    
    @Override
    public void superRun() {
        String inetstatsPathStr = Paths.get(".").toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.DIR_INETSTATS;
        File[] inetFiles = new File(inetstatsPathStr).listFiles();
        for (File inetFile : inetFiles) {
            String fileName = inetFile.getName();
            if (fileName.contains(".csv")) {
                this.ipAddr = fileName.replace(".csv", "");
                this.dbFullName = ConstantsFor.DB_INETSTATS + ipAddr.replaceAll("\\Q.\\E", "_");
                String syncMe = syncData();
                messageToUser.info(this.getClass().getSimpleName(), "synced", syncMe);
            }
        }
    }
    
    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.InternetSync.uploadCollection( int ) at 13.10.2019 - (13:21)");
    }
    
    @Override
    Map<String, String> makeColumns() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.InternetSync.makeColumns( Map<String, String> ) at 13.10.2019 - (13:21)");
    }
    
    private int createJSON(@NotNull Queue<String> fileQueue) {
        int updatedRows = 0;
        while (!fileQueue.isEmpty()) {
            String removedStr = fileQueue.remove();
            String[] toJSON = new String[5];
            try {
                toJSON = removedStr.split(",");
            }
            catch (IndexOutOfBoundsException e) {
                messageToUser.error("InternetSync.createJSON", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace()));
            }
            JsonObject jsonObject = new JsonObject();
            long valueParsed = parseDate(toJSON[0]);
            if (valueParsed < 0) {
                jsonObject = parseAsObject(removedStr);
            }
            else {
                jsonObject.add(ConstantsFor.DBCOL_STAMP, String.valueOf(valueParsed));
                try {
                    jsonObject.add(ConstantsFor.DBCOL_SQUIDANS, toJSON[1]);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    jsonObject.add(ConstantsFor.DBCOL_SQUIDANS, "no ans");
                }
                try {
                    jsonObject.add(ConstantsFor.DBCOL_BYTES, toJSON[2]);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    jsonObject.add(ConstantsFor.DBCOL_BYTES, "42");
                }
                try {
                    jsonObject.set("site", toJSON[4]);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    jsonObject.set("site", ConstantsFor.SITE_VELKOMFOOD);
                }
            }
            JsonObject finalJsonObject = jsonObject;
            updatedRows += sendToDatabase(finalJsonObject);
            messageToUser.info(this.getClass().getSimpleName(), MessageFormat.format("{0} remaining", fileQueue.size()), MessageFormat
                .format("{0} rows in {1} updated.", updatedRows, ipAddr));
        }
        return updatedRows;
    }
    
    private String renameToTXT(Path filePath) {
        String retStr;
        try {
            Path movedFilePath = Files.move(filePath, Paths.get(filePath.toAbsolutePath().normalize().toString().replace(".csv", ".txt")));
            retStr = movedFilePath.toAbsolutePath().normalize().toString();
        }
        catch (IOException e) {
            retStr = e.getMessage();
        }
        return retStr;
    }
    
    @Contract(pure = true)
    private static long parseDate(String dateAsString) {
        long result;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy", Locale.ENGLISH);
        try {
            Date parsedDate = simpleDateFormat.parse(dateAsString);
            System.out.println("parsedDate = " + parsedDate);
            result = parsedDate.getTime();
        }
        catch (ParseException e) {
            result = -1;
        }
        return result;
    }
    
    private JsonObject parseAsObject(String str) {
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject = (JsonObject) Json.parse(str);
            return jsonObject;
        }
        catch (com.eclipsesource.json.ParseException e) {
            jsonObject.add("stamp", "1");
            jsonObject.add("squidans", "");
            jsonObject.add("bytes", "1");
            jsonObject.add("site", "velkomfood.ru");
            return jsonObject;
        }
    }
    
    private int sendToDatabase(@NotNull JsonObject object) {
        int result = 0;
        final String sql = String.format("insert into %s (stamp, squidans, bytes, site) values (?, ?, ?, ?)", dbFullName);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            long timestampLong = Long.parseLong(object.get("stamp").asString());
            preparedStatement.setLong(1, timestampLong);
            preparedStatement.setString(2, object.get(ConstantsFor.DBCOL_SQUIDANS).toString().replaceAll("\\Q\"\\E", ""));
            try {
                preparedStatement.setInt(3, Integer.parseInt(object.get(ConstantsFor.DBCOL_BYTES).asString()));
            }
            catch (NumberFormatException e) {
                preparedStatement.setInt(3, 42);
            }
            try {
                preparedStatement.setString(4, object.get("site").toString().replaceAll("\\Q\"\\E", ""));
            }
            catch (RuntimeException e) {
                preparedStatement.setString(4, ConstantsFor.SITE_VELKOMFOOD);
            }
            result = preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            if (e.getMessage().contains(ConstantsFor.ERROR_DUPLICATEENTRY)) {
                result = 0;
            }
            else if (e.getErrorCode() == 1146) {
                createTable(this.ipAddr);
            }
            else {
                messageToUser.error(InternetSync.class.getSimpleName(), e.getMessage(), " see line: 222 ***");
                result = 0;
            }
        }
        return result;
    }
    
    private void createTable(@NotNull String ipAddr) {
        if (ipAddr.toLowerCase().contains("_")) {
            throw new InvokeIllegalException(ipAddr);
        }
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        String readFileStr = readFile();
        readFileStr = readFileStr.replace("ipAddr", ipAddr.replaceAll("\\Q.\\E", "_"))
            .replace(ConstantsFor.DBFIELD_PCNAME, PCInfo.checkValidNameWithoutEatmeat(ipAddr));
        final String sql = readFileStr;
        messageToUser.warn(InternetSync.class.getSimpleName(), "creating table", sql);
        FileSystemWorker.appendObjectToFile(new File("create.table"), sql);
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_INETSTATS + ipAddr.replaceAll("\\Q.\\E", "_"))) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.executeUpdate();
            }
        }
        catch (SQLException e) {
            messageToUser.error("InternetSync", "createTable", e.getMessage() + " see line: 194");
        }
    }
    
    private String readFile() {
        return "CREATE TABLE if not exists `ipAddr` (\n" +
            "\t`idrec` MEDIUMINT(11) UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
            "\t`stamp` BIGINT(13) UNSIGNED NOT NULL DEFAULT '442278000000',\n" +
            "\t`squidans` VARCHAR(20) NOT NULL DEFAULT 'unknown',\n" +
            "\t`bytes` INT(11) NOT NULL DEFAULT '42',\n" +
            "\t`timespend` INT(11) NOT NULL DEFAULT '42',\n" +
            "\t`site` VARCHAR(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',\n" +
            "\tPRIMARY KEY (`idrec`),\n" +
            "\tUNIQUE INDEX `stamp` (`stamp`, `site`, `bytes`) USING BTREE,\n" +
            "\tINDEX `site` (`site`)\n" +
            ")\n" +
            "COMMENT='pcName'\n" +
            "COLLATE='utf8_general_ci'\n" +
            "ENGINE=MyISAM\n" +
            "CHECKSUM=1\n" +
            ";".replace("ipAddr", this.ipAddr).replace(ConstantsFor.DBFIELD_PCNAME, PCInfo.checkValidNameWithoutEatmeat(ipAddr));
    }
    

}