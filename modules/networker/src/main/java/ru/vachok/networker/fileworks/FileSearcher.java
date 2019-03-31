package ru.vachok.networker.fileworks;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.TForms;
import ru.vachok.networker.services.MessageLocal;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;


/**
 Ищет файлы
 <p>

 @see FileSystemWorker
 @since 19.12.2018 (20:15) */
public class FileSearcher extends FileSystemWorker {
    
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());

    /**
     * Паттерн для поиска
     *
     * @see FileSystemWorker#searchInCommon(String[])
     */
    private String patternToSearch;

    /**
     * {@link List} с результатами
     */
    private List<String> resList = new ArrayList<>();

    /**
     @param patternToSearch что искать
     */
    public FileSearcher(String patternToSearch) {
        this.patternToSearch = patternToSearch;
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
            messageToUser.info("FileSearcher.postVisitDirectory", "dir.toFile().getName()", " = " + dir.toFile().getName());
        }
        return FileVisitResult.CONTINUE;
    }
    
    /**
     @return {@link #resList}
     */
    List<String> getResList() {
        return resList;
    }
}
