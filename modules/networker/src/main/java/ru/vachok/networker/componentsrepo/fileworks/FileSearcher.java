// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.fileworks;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.common.CommonSRV;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;


/**
 Ищет файлы
 <p>

 @see FileSystemWorker
 @see ru.vachok.networker.componentsrepo.fileworks.FileSearcherTest
 @since 19.12.2018 (20:15) */
public class FileSearcher extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
            .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, getClass().getSimpleName());

    /**
     * Паттерн для поиска
     *
     * @see CommonSRV#searchInCommon(String[])
     */
    private String patternToSearch;

    /**
     * {@link List} с результатами
     */
    private Set<String> resSet = new ConcurrentSkipListSet<>();
    
    private int totalFiles;

    /**
     @param patternToSearch что искать
     */
    public FileSearcher(String patternToSearch) {
        this.patternToSearch = patternToSearch;
        totalFiles = 0;
    }
    
    /**
     @return {@link #resSet}
     */
    public Set<String> getResSet() {
        return resSet;
    }
    
    @Override
    public void run() {
        try {
            Files.walkFileTree(Paths.get(patternToSearch), this);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " see line: 59 ***");
        }
    }
    
    /**
     @return {@link #resSet} or {@code nothing...}
     */
    @Override
    public String toString() {
        if (resSet.size() > 0) {
            return new TForms().fromArray(resSet, false);
        }
        else{
            return resSet.size() + " nothing...";
        }
    }

    /**
     Сверяет {@link #patternToSearch} с именем файла

     @param file  файл
     @param attrs {@link BasicFileAttributes}
     @return {@link FileVisitResult#CONTINUE}
     @throws IOException filesystem
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        this.totalFiles += 1;
        patternToSearch = patternToSearch.toLowerCase();
        if (attrs.isRegularFile() && file.toFile().getName().toLowerCase().contains(patternToSearch)) {
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
        }
        return FileVisitResult.CONTINUE;
    }
}
