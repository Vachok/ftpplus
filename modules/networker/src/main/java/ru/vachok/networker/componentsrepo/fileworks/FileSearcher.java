// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.fileworks;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.common.CommonSRV;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.componentsrepo.fileworks.FileSearcherTest
 @since 19.12.2018 (20:15) */
public class FileSearcher extends SimpleFileVisitor<Path> implements Callable<Set<String>> {
    
    
    private MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
            .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, getClass().getSimpleName());
    
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
    
    public FileSearcher(String patternToSearch) {
        this.patternToSearch = patternToSearch;
    }
    
    /**
     @param patternToSearch что искать
     @param folder начало поиска
     */
    public FileSearcher(String patternToSearch, Path folder) {
        this.patternToSearch = patternToSearch;
        startFolder = folder;
        totalFiles = 0;
    }
    
    @Override
    public Set<String> call() {
        this.patternToSearch = new String(patternToSearch.getBytes(), Charset.defaultCharset());
        resSet.add("Searching for: " + patternToSearch);
        try {
            this.startStamp = System.currentTimeMillis();
            Files.walkFileTree(startFolder, this);
            String fileName = FileNames.FILE_PREFIX_SEARCH_ + LocalTime.now().toSecondOfDay() + ".res";
            FileSystemWorker.writeFile(fileName, resSet.stream());
            messageToUser.warn(saveToDB());
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 59 ***");
        }
        return resSet;
    }
    
    private @NotNull String saveToDB() {
        DataConnectTo dataConnectTo = (DataConnectTo) DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT);
        dataConnectTo.getDataSource().setCreateDatabaseIfNotExist(true);
        int fileTo = dataConnectTo.uploadCollection(resSet, "search.s" + String.valueOf(System.currentTimeMillis()));
        return MessageFormat.format("Updated database {0}. {1} records.", dataConnectTo.getDataSource().getURL(), fileTo);
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
}
