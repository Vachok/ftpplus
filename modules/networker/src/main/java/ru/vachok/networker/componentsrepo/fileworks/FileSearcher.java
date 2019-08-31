// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.fileworks;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.common.CommonSRV;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;


/**
 Ищет файлы
 <p>

 @see FileSystemWorker
 @see ru.vachok.networker.fileworks.FileSearcherTest
 @since 19.12.2018 (20:15) */
public class FileSearcher extends SimpleFileVisitor<Path> {
    
    
    private MessageToUser messageToUser = ru.vachok.networker.restapi.MessageToUser
        .getInstance(ru.vachok.networker.restapi.MessageToUser.LOCAL_CONSOLE, getClass().getSimpleName());

    /**
     * Паттерн для поиска
     *
     * @see CommonSRV#searchInCommon(String[])
     */
    private String patternToSearch;

    /**
     * {@link List} с результатами
     */
    private List<String> resList = new ArrayList<>();
    
    private int totalFiles;

    /**
     @param patternToSearch что искать
     */
    public FileSearcher(String patternToSearch) {
        this.patternToSearch = patternToSearch;
        totalFiles = 0;
    }

    /**
     @return {@link #resList} or {@code nothing...}
     */
    @Override
    public String toString() {
        if(resList.size() > 0){
            return new TForms().fromArray(resList, false);
        }
        else{
            return resList.size() + " nothing...";
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
            resList.add(file.toFile().getAbsolutePath());
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
            messageToUser.info("total files: " + totalFiles, "found: " + resList.size(), "scanned: " + dir.toString().replace("\\\\srv-fs.eatmeat.ru\\common_new\\", ""));
        }
        return FileVisitResult.CONTINUE;
    }
    
    /**
     @return {@link #resList}
     */
    public List<String> getResList() {
        return resList;
    }
}
