// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.fileworks;


import org.jetbrains.annotations.NotNull;
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
    
    private static final Connection connection = DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT).getDefaultConnection(ConstantsFor.DB_SEARCH);
    
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
    
    private String lastTableName;
    
    private Semaphore dropSemaphore;
    
    public FileSearcher(String patternToSearch) {
        this.patternToSearch = patternToSearch;
        lastTableName = "search.s" + String.valueOf(System.currentTimeMillis());
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
        lastTableName = "search.s" + String.valueOf(System.currentTimeMillis());
        dropSemaphore = new Semaphore(0);
    }
    
    @Override
    public Set<String> call() {
        this.patternToSearch = new String(patternToSearch.getBytes(), Charset.defaultCharset());
        resSet.add("Searching for: " + patternToSearch);
        try {
            this.startStamp = System.currentTimeMillis();
            Files.walkFileTree(startFolder, this);
            saveToDB();
            dropSemaphore.release();
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 59 ***");
        }
        return resSet;
    }
    
    public String getSearchResultFromDB() {
        return getSearchResultFromDB(false);
    }
    
    public String getSearchResultFromDB(boolean dropTable) {
        StringBuilder stringBuilder = new StringBuilder();
    
        if (dropSemaphore.tryAcquire()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(String.format(ConstantsFor.SQL_SELECT, lastTableName));
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    stringBuilder.append(resultSet.getString(ConstantsFor.DBCOL_UPSTRING));
                }
                if (dropTable) {
                    try (PreparedStatement dropTbl = connection.prepareStatement(String.format(ConstantsFor.SQL_DROPTABLE, lastTableName))) {
                        stringBuilder.append(dropTbl.executeUpdate()).append(" drop ").append(lastTableName);
                    }
                }
            }
            catch (SQLException e) {
                stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
                Thread.currentThread().checkAccess();
                Thread.currentThread().interrupt();
            }
        }
        return stringBuilder.toString();
    }
    
    public static void dropSearchTables() {
        try (PreparedStatement dropStatement = connection.prepareStatement("drop database search")) {
            dropStatement.executeUpdate();
        }
        catch (SQLException e) {
            messageToUser.error("FileSearcher", "dropSearchTables", e.getMessage() + " see line: 93");
        }
    }
    
    /**
     Вывод имени папки в консоль.
     
     @param dir обработанная папка
     @param exc {@link IOException}
     @return {@link FileVisitResult#CONTINUE}
     
     @throws IOException filesystem
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
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
    
    /**
     Сверяет {@link #patternToSearch} с именем файла
     
     @param file файл
     @param attrs {@link BasicFileAttributes}
     @return {@link FileVisitResult#CONTINUE}
     
     @throws IOException filesystem
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        this.totalFiles += 1;
        patternToSearch = patternToSearch.toLowerCase();
        if (attrs.isRegularFile() && file.toFile().getName().toLowerCase().contains(patternToSearch.toLowerCase())) {
            resSet.add(file.toFile().getAbsolutePath());
        }
        return FileVisitResult.CONTINUE;
    }
    
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
        DatabaseMetaData connectionMetaData = connection.getMetaData();
        List<String> tableNames = new ArrayList<>();
        try (ResultSet rs = connectionMetaData.getTables(ConstantsFor.DB_SEARCH, "", "%", null)) {
            while (rs.next()) {
                String tableName = rs.getString(3);
                tableNames.add(tableName);
                messageToUser.info(CommonSRV.class.getSimpleName(), " search table added: ", tableName);
            }
            Collections.sort(tableNames);
            Collections.reverse(tableNames);
        }
        return tableNames;
    }
    
    private static @NotNull String infoFromTable(@NotNull String tblName) {
        StringBuilder stringBuilder = new StringBuilder();
        String sql = String.format(ConstantsFor.SQL_SELECT, "search." + tblName);
        stringBuilder.append(new Date(Long.parseLong(tblName.replace("s", "")))).append(":\n");
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                stringBuilder.append(resultSet.getString(3)).append("\n");
            }
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        return stringBuilder.toString();
    }
    
    private void saveToDB() {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        dataConnectTo.getDataSource().setCreateDatabaseIfNotExist(true);
        int fileTo = dataConnectTo.uploadCollection(resSet, lastTableName);
        messageToUser.info(MessageFormat.format("Updated database {0}. {1} records.", dataConnectTo.getDataSource().getURL(), fileTo));
        dropSemaphore.release();
    }
}
