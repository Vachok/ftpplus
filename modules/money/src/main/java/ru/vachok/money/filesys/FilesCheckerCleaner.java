package ru.vachok.money.filesys;


import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.config.AppComponents;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 Проверяет и удаляет ненужный мусор.
 <p>

 @since 29.11.2018 (22:41) */
@Component
public class FilesCheckerCleaner extends SimpleFileVisitor<Path> {

    private static final Logger LOGGER = AppComponents.getLogger();

    private ConcurrentMap<String, String> resMap = new ConcurrentHashMap<>();

    private Path path;

    private String inpUser;

    private boolean isSearch;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    ConcurrentMap<String, String> getResMap() {
        LOGGER.info("FilesCheckerCleaner.getResMap");
        return resMap;
    }

    /*Instances*/
    FilesCheckerCleaner(boolean isSearch, String inpUser) {
        this.isSearch = isSearch;
        this.inpUser = inpUser;
    }

    public FilesCheckerCleaner() {
        this.isSearch = false;
        this.inpUser = "Nothing";
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    /**
     @param file  {@link Path} до текущего анализируемого файла
     @param attrs {@link BasicFileAttributes}
     @return {@link FileVisitResult#CONTINUE}
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        boolean isOneYearOld = attrs.lastModifiedTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(ConstantsFor.ONE_YEAR);
        if(!isSearch && isOneYearOld){
            resMap.put(file.toString(), attrs.lastAccessTime().toString());
            return FileVisitResult.CONTINUE;
        }
        else{
            if(attrs.isRegularFile() && file.toFile().getName().toLowerCase().contains(inpUser)){
                resMap.put(file.toString(), ( float ) attrs.size() / ConstantsFor.KILOBYTE + " kbytes");
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        resMap.put(file.toString(), exc.getMessage());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}