// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.fileworks;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.common.CommonSRV;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.*;


/**
 @see ru.vachok.networker.componentsrepo.fileworks.FileSearcherTest
 @since 19.12.2018 (20:15) */
public class FileSearcher extends SimpleFileVisitor<Path> implements Callable<Set<String>> {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, FileSearcher.class.getSimpleName());
    
    private final String lastTableName;
    
    /**
     Паттерн для поиска
     
     @see CommonSRV#searchInCommon(String[])
     */
    private String patternToSearch;
    
    /**
     {@link List} с результатами
     */
    private Set<String> resSet = new ConcurrentSkipListSet<>();
    
    private Path startFolder = Paths.get("\\\\srv-fs.eatmeat.ru\\common_new\\");
    
    private int totalFiles;
    
    private long startStamp;
    
    private Semaphore dropSemaphore;
    
    public static @NotNull String getSearchResultsFromDB() {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> tableNames = new ArrayList<>();
        try {
            tableNames.addAll(getSortedTableNames());
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        for (String tableName : tableNames) {
            stringBuilder.append(infoFromTable(tableName));
        }
        return stringBuilder.toString();
    }
    
    private static @NotNull List<String> getSortedTableNames() throws SQLException {
        List<String> tableNames = new ArrayList<>();
        try (Connection connection = DataConnectTo.getDefaultI().getDataSource().getConnection()) {
            DatabaseMetaData connectionMetaData = connection.getMetaData();
            try (ResultSet rs = connectionMetaData.getTables(ConstantsFor.DB_SEARCH, "", "%", null)) {
                while (rs.next()) {
                    String tableName = rs.getString(3);
                    tableNames.add(tableName);
                    messageToUser.info(CommonSRV.class.getSimpleName(), " search table added: ", tableName);
                }
            }
            Collections.sort(tableNames);
            Collections.reverse(tableNames);
        }
        return tableNames;
    }
    
    /**
     @param tblName имя таблицы БД
     @return содержимое
     
     @see FileSearcherTest#testGetSearchResultsFromDB()
     */
    private static @NotNull String infoFromTable(@NotNull String tblName) {
        StringBuilder stringBuilder = new StringBuilder();
        int rowsLim = Integer.parseInt(AppComponents.getProps().getProperty("limitsearch", "300"));
        String sql = String.format(ConstantsFor.SQL_SELECT, ConstantsFor.DB_TABLESEARCH + tblName + " limit " + rowsLim);
        if (!tblName.equalsIgnoreCase(ConstantsFor.DB_PERMANENT)) {
            stringBuilder.append(new Date(Long.parseLong(tblName.replace("s", "")))).append(":\n");
        }
        
        try (Connection connection = DataConnectTo.getDefaultI().getDefaultConnection(ConstantsFor.DB_TABLESEARCH + tblName)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    int rSetCounter = 0;
                    while (resultSet.next()) {
                        stringBuilder.append(resultSet.getString(3)).append("\n");
                        rSetCounter += 1;
                    }
                    if (rSetCounter >= rowsLim) {
                        stringBuilder.append(MessageFormat.format("More results in DB. {0}. Limit {1} rows", tblName, rowsLim));
                    }
                    else {
                        messageToUser.info(FileSearcher.class.getSimpleName(), "infoFromTable", ": " + rSetCounter);
                    }
                }
            }
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        return stringBuilder.toString();
    }
    
    public static List<String> getSearchTablesToDrop() {
        return getSearchTablesToDrop(ConstantsFor.DB_SEARCH);
    }
    
    public static @NotNull List<String> getSearchTablesToDrop(String databaseName) {
        List<String> tableNames = new ArrayList<>();
        try (Connection connection = DataConnectTo.getDefaultI().getDataSource().getConnection()) {
            try (PreparedStatement dropStatement = connection.prepareStatement(String.format("SHOW TABLE STATUS FROM %s", databaseName))) {
                
                try (ResultSet resultSet = dropStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String tableName = resultSet.getString(1);
                        if (!tableName.equalsIgnoreCase(ConstantsFor.DB_PERMANENT)) {
                            tableNames.add(tableName);
                        }
                        else {
                            messageToUser.info(DataConnectTo.getTableInfo(resultSet));
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
    
    public FileSearcher(String patternToSearch) {
        this.patternToSearch = patternToSearch;
        lastTableName = ConstantsFor.DB_SEARCHS + String.valueOf(System.currentTimeMillis());
        dropSemaphore = new Semaphore(0);
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
        dropSemaphore = new Semaphore(0);
    }
    
    @Override
    public Set<String> call() {
        this.patternToSearch = new String(patternToSearch.getBytes(), Charset.defaultCharset());
        resSet.add("Searching for: " + patternToSearch);
        try {
            this.startStamp = System.currentTimeMillis();
            Files.walkFileTree(startFolder, this);
            dropSemaphore.release();
            saveToDB();
        }
        catch (IOException | SQLException e) {
            messageToUser.error(e.getMessage() + " see line: 59 ***");
        }
        return resSet;
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
            resSet.add(file.toFile().getAbsolutePath());
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
            messageToUser
                    .info("total files: " + totalFiles, "found: " + resSet.size(), "scanned: " + dir.toString().replace("\\\\srv-fs.eatmeat.ru\\common_new\\", ""));
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
            return new TForms().fromArray(resSet, false);
        }
        else {
            return resSet.size() + " nothing...";
        }
    }
    
    private void saveToDB() throws SQLException {
        DataConnectTo instanceDCT = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I);
        if (dropSemaphore.availablePermits() > 0) {
            messageToUser.warn(this.getClass().getSimpleName(), dropSemaphore.toString(), MessageFormat.format("Drained {0} permits!", dropSemaphore.drainPermits()));
            int tableCreate = instanceDCT.createTable(lastTableName, Collections.EMPTY_LIST);
            int fileTo = instanceDCT.uploadCollection(resSet, lastTableName);
            
            messageToUser.warn(this.getClass().getSimpleName(), MessageFormat.format("Creating {0}. {1}.", lastTableName, tableCreate), MessageFormat
                    .format("Added: {0}, total: {1}.", resSet.size(), String.valueOf(totalFiles)));
            
            messageToUser.info(MessageFormat
                    .format("Updated database {0}. {1} records.", instanceDCT.getDataSource().getConnection().getMetaData().getURL(), fileTo));
            dropSemaphore.release();
        }
        messageToUser.warn(this.getClass().getSimpleName(), dropSemaphore.toString(), MessageFormat
                .format("Available permits: {0}, has queued threads {1}.", dropSemaphore.availablePermits(), dropSemaphore.hasQueuedThreads()));
    }
}
