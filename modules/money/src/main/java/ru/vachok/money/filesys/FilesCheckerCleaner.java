package ru.vachok.money.filesys;


import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import ru.vachok.money.config.AppComponents;

import java.io.IOException;
import java.nio.file.*;
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

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    private ConcurrentMap<String, String> resMap = new ConcurrentHashMap<>();

    public ConcurrentMap<String, String> getResMap() {
        try{
            Files.walkFileTree(Paths.get("."), this);
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
        }
        return resMap;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        LOGGER.info("FilesCheckerCleaner.preVisitDirectory");
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if(attrs.lastModifiedTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)){
            resMap.put(file.toString(), attrs.lastAccessTime().toString());
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        resMap.put(file.toString(), exc.getMessage());
        return super.visitFileFailed(file, exc);
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        LOGGER.info("FilesCheckerCleaner.postVisitDirectory");
        return super.postVisitDirectory(dir, exc);
    }
}