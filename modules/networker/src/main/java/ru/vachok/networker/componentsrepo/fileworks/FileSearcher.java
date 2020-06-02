// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.fileworks;


import com.eclipsesource.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.ad.common.CommonSRV;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.componentsrepo.fileworks.FileSearcherTest
 @since 19.12.2018 (20:15) */
public class FileSearcher extends SimpleFileVisitor<Path> implements Callable<Set<String>> {


    public static final String LIMITSEARCHROWS = "limitsearchrows";

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, FileSearcher.class.getSimpleName());

    @NotNull private static DataConnectTo dataConnectInst = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);

    private String lastTableName;

    /**
     Паттерн для поиска

     @see CommonSRV#searchInCommon(String[])
     */
    private String patternToSearch;

    /**
     {@link List} с результатами
     */
    private final Set<String> resSet = new ConcurrentSkipListSet<>();

    private Path startFolder = Paths.get("\\\\srv-fs.eatmeat.ru\\common_new\\");

    private int totalFiles;

    private long startStamp;

    @NotNull
    public String getSearchResultsFromDB() {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> tableNames = new ArrayList<>();
        try {
            tableNames.addAll(getSortedTableNames());
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        for (String tableName : tableNames) {
            this.lastTableName = tableName;
            if (!tableName.contains(ConstantsFor.DB_PERMANENT)) {
                stringBuilder.append(infoFromTable());
            }
            else {
                stringBuilder.append(getPermanent());
            }
        }
        return stringBuilder.toString();
    }

    @NotNull
    private static List<String> getSortedTableNames() throws SQLException {
        List<String> tableNames = new ArrayList<>();
        try (Connection connection = dataConnectInst.getDefaultConnection(ConstantsFor.DB_SEARCHPERMANENT)) {
            DatabaseMetaData connectionMetaData = connection.getMetaData();
            try (ResultSet rs = connectionMetaData.getTables(ConstantsFor.DB_SEARCH, "", "%", null)) {
                while (rs.next()) {
                    String tableName = rs.getString(3);
                    tableNames.add(tableName);
                }
            }
            Collections.sort(tableNames);
            Collections.reverse(tableNames);
        }
        return tableNames;
    }

    /**
     @return содержимое

     @see FileSearcherTest#testGetSearchResultsFromDB()
     */
    @NotNull
    private String infoFromTable() {
        StringBuilder stringBuilder = new StringBuilder();
        int rowsLim = Integer.parseInt(InitProperties.getTheProps().getProperty(LIMITSEARCHROWS, "300"));
        final String sql = String.format(ConstantsFor.SQL_SELECT, ConstantsFor.DB_TABLESEARCH + lastTableName + " limit " + rowsLim);
        stringBuilder.append(lastTableName).append(" = ");
        stringBuilder.append(connectToDatabase(sql)).append("\n");
        return stringBuilder.toString();
    }

    @NotNull
    private String getPermanent() {
        final String sql = "select * from search.permanent limit 300";
        return MessageFormat.format("{0} = {1}", lastTableName, connectToDatabase(sql));
    }

    @NotNull
    private JsonObject connectToDatabase(String sql) {
        JsonObject jsonObject = new JsonObject();
        int rowsLim = Integer.parseInt(InitProperties.getTheProps().getProperty(LIMITSEARCHROWS, "300"));
        try (Connection connection = DataConnectTo.getDefaultI().getDefaultConnection(ConstantsFor.DB_TABLESEARCH + ConstantsFor.DB_PERMANENT)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    int rSetCounter = 0;
                    ResultSetMetaData setMetaData = resultSet.getMetaData();
                    int columnCount = setMetaData.getColumnCount();
                    while (resultSet.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            jsonObject.add(setMetaData.getColumnName(i), resultSet.getString(i));
                        }
                    }
                    if (rSetCounter >= rowsLim) {
                        jsonObject.add(ConstantsFor.RETURN_ERROR, MessageFormat.format("More results in DB. {0}. Limit {1} rows", lastTableName, rowsLim));
                    }
                    else {
                        messageToUser.info(FileSearcher.class.getSimpleName(), "infoFromTable", ": " + rSetCounter);
                    }
                }
            }
        }
        catch (SQLException e) {
            jsonObject.add(e.getMessage(), AbstractForms.fromArray(e));
        }
        return jsonObject;
    }

    public static void setDataConnectInst(@NotNull DataConnectTo dataConnectInst) {
        FileSearcher.dataConnectInst = dataConnectInst;
    }

    public FileSearcher() {
        this.patternToSearch = ":";
    }

    public FileSearcher(String patternToSearch) {
        this.patternToSearch = patternToSearch;
        lastTableName = ConstantsFor.DB_SEARCHS + String.valueOf(System.currentTimeMillis());
    }

    /**
     @param patternToSearch что искать
     @param folder начало поиска
     */
    public FileSearcher(String patternToSearch, Path folder) {
        this.patternToSearch = patternToSearch;
        startFolder = folder;
        totalFiles = 0;
        lastTableName = ConstantsFor.DB_SEARCHS + String.valueOf(System.currentTimeMillis());
    }

    /**
     @return {@link #getSearchTablesToDrop()} or {@link SQLException}

     @see FileSearcherTest#testDropSearchTables()
     */
    public static String dropTables() {
        try (Connection connection = dataConnectInst.getDefaultConnection(ConstantsFor.DB_SEARCHPERMANENT)) {
            for (String tableToDropName : getSearchTablesToDrop()) {
                final String sql = String.format(ConstantsFor.SQL_DROPTABLE, tableToDropName);
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.executeUpdate();
                }
            }
            return AbstractForms.fromArray(getSearchTablesToDrop());
        }
        catch (SQLException e) {
            return MessageFormat.format("{0}\n{1}", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
    }

    @NotNull
    protected static List<String> getSearchTablesToDrop() {
        List<String> tableNames = new ArrayList<>();
        try (Connection connection = dataConnectInst.getDefaultConnection(ConstantsFor.DB_SEARCHPERMANENT)) {
            try (PreparedStatement dropStatement = connection
                .prepareStatement("SELECT * FROM information_schema.TABLES WHERE TABLE_SCHEMA LIKE 'search'")) {
                try (ResultSet resultSet = dropStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String tableName = resultSet.getString(ConstantsFor.SQL_TABLE_NAME);
                        if (!tableName.equalsIgnoreCase(ConstantsFor.DB_PERMANENT)) {
                            tableNames.add(tableName);
                        }
                        else {
                            messageToUser.info(getTableInfo(resultSet));
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error("FileSearcher", "getSearchTablesToDrop", e.getMessage() + " see line: 93");
        }
        return tableNames;
    }

    @NotNull
    private static String getTableInfo(@NotNull ResultSet resultSet) throws SQLException {
        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("Name : ").append(resultSet.getString(1)).append("\n");
        infoBuilder.append("Eng : ").append(resultSet.getString(2)).append("\n");
        infoBuilder.append("Row_format : ").append(resultSet.getString(3)).append("\n");
        infoBuilder.append("Rows : ").append(resultSet.getString(5)).append("\n");
        infoBuilder.append("Avg_row_length : ").append(resultSet.getString(6)).append("\n");
        infoBuilder.append("Data_length : ").append(resultSet.getString(7)).append("\n");
        infoBuilder.append("Max_data_length : ").append(resultSet.getString(8)).append("\n");
        infoBuilder.append("Auto_increment : ").append(resultSet.getString(11)).append("\n");
        infoBuilder.append("Create_time : ").append(resultSet.getString(12)).append("\n");
        infoBuilder.append("Update_time : ").append(resultSet.getString(13)).append("\n");
        infoBuilder.append("Create_options : ").append(resultSet.getString(17)).append("\n");
        infoBuilder.append("Comment : ").append(resultSet.getString(18)).append("\n");

        return infoBuilder.toString();
    }

    @Override
    public Set<String> call() {
        this.patternToSearch = new String(patternToSearch.getBytes(), Charset.defaultCharset());
        resSet.add("Searching for: " + patternToSearch);
        try {
            this.startStamp = System.currentTimeMillis();
            Files.walkFileTree(startFolder, this);
            startFolder = Paths
                    .get(startFolder.toAbsolutePath().normalize().toString().replace("\\\\srv-fs.eatmeat.ru\\common_new\\", "\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives\\"));
            Files.walkFileTree(startFolder, this);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 59 ***");
        }
        finally {
            saveToDB();
        }
        return this.resSet;
    }

    private void saveToDB() {
        DataConnectTo instanceDCT = dataConnectInst;
        int tableCreate = instanceDCT.createTable(lastTableName, Collections.EMPTY_LIST);
        int fileTo = instanceDCT.uploadCollection(resSet, lastTableName);
        MessageToUser.getInstance(MessageToUser.EMAIL, this.getClass().getSimpleName()).info(AbstractForms.fromArray(resSet));
    }

    /**
     Сверяет {@link #patternToSearch} с именем файла

     @param file файл
     @param attrs {@link BasicFileAttributes}
     @return {@link FileVisitResult#CONTINUE}
     */
    @Override
    public FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) {
        this.totalFiles += 1;
        patternToSearch = patternToSearch.toLowerCase();
        if (attrs.isRegularFile() && file.toFile().getName().toLowerCase().contains(patternToSearch.toLowerCase())) {
            this.resSet.add(file.toFile().getAbsolutePath());
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     Вывод имени папки в консоль.

     @param dir обработанная папка
     @param exc {@link IOException}
     @return {@link FileVisitResult#CONTINUE}
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        if (dir.toFile().isDirectory()) {
            messageToUser.info(
                    "total files: " + totalFiles, "found: " + (resSet.size() - 1),
                    "scanned: " + dir.toString().replace("\\\\srv-fs.eatmeat.ru\\common_new\\",
                            ""));

            long secondsScan = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startStamp);
            if (secondsScan == 0) {
                secondsScan = 1;
            }
            long filesSec = totalFiles / secondsScan;
            messageToUser.info(this.getClass().getSimpleName(), ConstantsFor.ELAPSED, MessageFormat.format("{1}. {0} files/sec", filesSec, secondsScan));
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     @return {@link #resSet} or {@code nothing...}
     */
    @Override
    public String toString() {
        if (resSet.size() > 0) {
            return AbstractForms.fromArray(resSet);
        }
        else {
            return resSet.size() + " nothing...";
        }
    }
}
