package ru.vachok.networker.data.synchronizer;


import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.fsworks.UpakFiles;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 @see InternetSyncTest
 @since 13.10.2019 (13:21) */
public class InternetSync extends SyncData implements Runnable {
    
    
    private String ipAddr;
    
    private Connection connection;
    
    private String dbFullName;
    
    InternetSync(@NotNull String type) {
        super();
        this.ipAddr = type;
        this.dbFullName = ConstantsFor.DB_INETSTATS + ipAddr.replaceAll("\\Q.\\E", "_");
        this.connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("inetstats.inetstats");
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InternetSync{");
        sb.append("ipAddr='").append(ipAddr).append('\'');
        sb.append(", dbFullName='").append(dbFullName).append('\'');
        try {
            sb.append(", connection=").append(AbstractForms.fromArray(connection.getTypeMap()));
        }
        catch (SQLException ignore) {
            //15.10.2019 (9:29)
        }
        sb.append('}');
        return sb.toString();
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
        String txtRenamed = renameToTXT(filePath);
        String tblComent = checkComment();
        if (tblComent.equals(getCurrentUserPC())) {
            return MessageFormat.format("{0} created {1} rows. {2}", txtRenamed, jsonCreated, tblComent);
        }
        else {
            return MessageFormat.format("{0} created {1} rows. Comment NEW: {2}", txtRenamed, jsonCreated, setComment());
        }
    }
    
    @Contract(pure = true)
    private static long parseDate(String dateAsString) {
        long result;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy", Locale.ENGLISH);
        try {
            Date parsedDate = simpleDateFormat.parse(dateAsString);
            result = parsedDate.getTime();
        }
        catch (ParseException e) {
            result = -1;
        }
        return result;
    }
    
    private @NotNull String setComment() {
        String comment = "Changing table comment";
        final String sql = String.format("ALTER TABLE %s COMMENT='%s';", dbFullName, getCurrentUserPC());
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(dbFullName)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                return MessageFormat.format("{0} is {1}", sql, preparedStatement.executeUpdate());
            }
        }
        catch (SQLException e) {
            return MessageFormat.format("InternetSync.setComment() : {0} see line: 76", e.getMessage());
        }
    }
    
    /**
     @see InternetSyncTest#testSuperRun()
     */
    @Override
    public void superRun() {
        if (!UsefulUtilities.thisPC().toLowerCase().contains("rups")) {
            throw new InvokeIllegalException(MessageFormat.format("{0} can not run super sync from {1}", UsefulUtilities.thisPC(), this.getClass().getSimpleName()));
        }
    
        String inetstatsPathStr = Paths.get(".").toAbsolutePath().normalize().toString() + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.DIR_INETSTATS;
        File[] inetFiles = new File(inetstatsPathStr).listFiles();
        for (File inetFile : Objects.requireNonNull(inetFiles, ()->MessageFormat.format("No files in {0}", inetstatsPathStr))) {
            String fileName = inetFile.getName();
            if (fileName.contains(".csv") & fileName.replace(".csv", "").matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
                this.ipAddr = fileName.replace(".csv", "");
                this.dbFullName = ConstantsFor.DB_INETSTATS + ipAddr.replaceAll("\\Q.\\E", "_");
                String syncMe = syncData();
                messageToUser.info(this.getClass().getSimpleName(), "synced", syncMe);
            }
        }
        
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
    
    protected String checkComment() {
        String tableComment = "Not found";
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("information_schema.tables")) {
            String dbName = dbFullName.replace(ConstantsFor.DB_INETSTATS, "");
            final String sql = String.format("SELECT TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_NAME LIKE '%s';", dbName);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    tableComment = resultSet.getString(1);
                    
                }
            }
        }
        catch (SQLException e) {
            tableComment = SyncData.INETSYNC + " checkComment " + e.getMessage() + " see line: 89";
        }
        return tableComment;
    }
    
    private @NotNull String renameToTXT(Path filePath) {
        String retStr;
        int records = countRecords(filePath);
        Path cpPath = filePath.toAbsolutePath().normalize().getParent();
        cpPath = Paths.get(cpPath.toString() + ConstantsFor.FILESYSTEM_SEPARATOR + "ok" + ConstantsFor.FILESYSTEM_SEPARATOR + filePath.getFileName().toString()
            .replace(".csv", String.format("-%d.txt", records)));
        retStr = FileSystemWorker.copyOrDelFileWithPath(filePath.toFile(), cpPath, true);
        UpakFiles upakFiles = new UpakFiles();
        upakFiles.createZip(Objects.requireNonNull(cpPath.getParent().toFile().listFiles(), ConstantsFor.ERR_NOFILES));
        
        return retStr;
    }
    
    private JsonObject parseAsObject(String str) {
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject = (JsonObject) Json.parse(str);
            return jsonObject;
        }
        catch (com.eclipsesource.json.ParseException e) {
            jsonObject.add(ConstantsFor.DBCOL_STAMP, "1");
            jsonObject.add(ConstantsFor.DBCOL_SQUIDANS, "");
            jsonObject.add(ConstantsFor.DBCOL_BYTES, "1");
            jsonObject.add("site", ConstantsFor.SITE_VELKOMFOOD);
            return jsonObject;
        }
    }
    
    private int sendToDatabase(@NotNull JsonObject object) {
        int result = 0;
        @SuppressWarnings("DuplicateStringLiteralInspection") final String sql = String
            .format("insert into %s (stamp, squidans, bytes, site) values (?, ?, ?, ?)", dbFullName);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            long timestampLong = Long.parseLong(object.get(ConstantsFor.DBCOL_STAMP).asString());
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
            else if (e.getMessage().contains(ConstantsFor.ERROR_NOEXIST)) {
                String tableCreated = createTable(this.ipAddr);
                messageToUser.warn(this.getClass().getSimpleName(), "Creating table: ", tableCreated);
            }
            else {
                messageToUser.error(InternetSync.class.getSimpleName(), e.getMessage(), " see line: 222 ***");
                result = 0;
            }
        }
        return result;
    }
    
    private int countRecords(Path filePath) {
        int i;
        try {
            i = FileSystemWorker.countStringsInFile(filePath);
        }
        catch (RuntimeException e) {
            i = -666;
        }
        return i;
    }
    
    /**
     @param ipAddr ip-адрес
     @see InternetSyncTest#testCreateTable()
     */
    protected String createTable(@NotNull String ipAddr) {
        if (!ipAddr.matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
            throw new InvokeIllegalException(ipAddr);
        }
        DataConnectTo dataConnectTo = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        String readFileStr = readSQLCreateQuery();
        readFileStr = readFileStr.replace(ConstantsFor.FIELDNAME_ADDR, ipAddr.replaceAll("\\Q.\\E", "_"))
            .replace(ConstantsFor.DBFIELD_PCNAME, getCurrentUserPC());
        final String sql = readFileStr;
        FileSystemWorker.appendObjectToFile(new File("create.table"), sql);
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DB_INETSTATS + ipAddr.replaceAll("\\Q.\\E", "_"))) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                int executeUpdateInt = preparedStatement.executeUpdate();
                return MessageFormat.format("Updated: {0}. Query: \n{1}", executeUpdateInt, sql);
            }
        }
        catch (SQLException e) {
            return MessageFormat
                .format("InternetSync.createTable: {0}\n{1}\nQuery was: {2}", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace()), sql);
        }
    }
    
    private String getCurrentUserPC() {
        String pcName = PCInfo.checkValidNameWithoutEatmeat(ipAddr);
        String userAndPCName = UserInfo.getInstance(pcName).getInfoAbout(pcName);
        if (userAndPCName.contains("\'")) {
            userAndPCName = userAndPCName.split("\\Q'\\E")[0];
        }
        return userAndPCName;
    }
    
    private @NotNull String readSQLCreateQuery() {
        return "CREATE TABLE if not exists `ipAddr` (\n" +
            "\t`idrec` MEDIUMINT(11) UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
            "\t`stamp` BIGINT(13) UNSIGNED NOT NULL DEFAULT '442278000000',\n" +
            "\t`squidans` VARCHAR(20) NOT NULL DEFAULT 'unknown',\n" +
            "\t`bytes` INT(11) NOT NULL DEFAULT '42',\n" +
            "\t`timespend` INT(11) NOT NULL DEFAULT '42',\n" +
            "\t`site` VARCHAR(190) NOT NULL DEFAULT 'http://www.velkomfood.ru',\n" +
            "\tPRIMARY KEY (`idrec`),\n" +
            "\tUNIQUE INDEX `stampkey` (`stamp`, `site`, `bytes`) USING BTREE\n" +
            ")\n" +
            "COMMENT='pcName'\n" +
            "COLLATE='utf8_general_ci'\n" +
            "ENGINE=MyISAM\n" +
            "ROW_FORMAT=COMPRESSED\n" +
            ";\n".replace(ConstantsFor.FIELDNAME_ADDR, this.ipAddr.replaceAll("\\Q.\\E", "_"))
                .replace(ConstantsFor.DBFIELD_PCNAME, PCInfo.checkValidNameWithoutEatmeat(ipAddr));
    }
    
    /**
     @param stringsCollection коллекция строк
     @param tableName ip-адрес
     @return {@link #sendToDatabase(JsonObject)}
     
     @see InternetSyncTest#testUploadCollection()
     */
    @Override
    public int uploadCollection(Collection stringsCollection, @NotNull String tableName) {
        int retInt = 0;
        if (tableName.matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
            this.dbFullName = ConstantsFor.DB_INETSTATS + tableName.replaceAll("\\Q.\\E", "_");
        }
        else {
            throw new InvokeIllegalException("15.10.2019 (9:42)");
        }
        List<String> collectList = new ArrayList<>(stringsCollection);
        for (String s : collectList) {
            JsonObject jsonObject = parseAsObject(s);
            retInt += sendToDatabase(jsonObject);
        }
        return retInt;
    }
    
    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        superRun();
    }
    
    @Override
    Map<String, String> makeColumns() {
        throw new UnsupportedOperationException("See readSQLCreateQuery");
    }
    
    
}