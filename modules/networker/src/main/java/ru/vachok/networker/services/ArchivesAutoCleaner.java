package ru.vachok.networker.services;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 Очистка папки \\192.168.14.10\IT-Backup\SRV-FS\Archives

 @see ru.vachok.networker.SystemTrayHelper
 @since 15.11.2018 (14:09) */
public class ArchivesAutoCleaner extends SimpleFileVisitor<Path> implements Runnable {

    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     Первоначальная папка
     */
    private static final String SRV_FS_ARCHIVES = "\\\\192.168.14.10\\IT-Backup\\SRV-FS\\Archives";

    /**
     Год
     */
    private int yearStop;

    public ArchivesAutoCleaner(int yearStop) {
        super();
        this.yearStop = yearStop;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        String msg = new StringBuilder().append("Cleaning the directory: ")
            .append(dir.toString()).append("\n")
            .append(attrs.lastModifiedTime()).append(" is last modified time.").toString();
        LOGGER.debug(msg);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.toString().contains(" " + yearStop + "-")) {
            String msg = file.toString() + " is deleted!\n";
            Files.delete(file);
            LOGGER.warn(msg);
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        String size = Files.size(file) + " visit failed (" + file.toAbsolutePath() + ")";
        LOGGER.info(size);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        try {
            if (dir.toAbsolutePath().toFile().listFiles().length == 0) {
                Files.delete(dir);
                String msg = dir.toString() + " deleted!";
                LOGGER.warn(msg);
            }
            return FileVisitResult.CONTINUE;
        } catch (Exception e) {
            return FileVisitResult.CONTINUE;
        }
    }

    @Override
    public void run() {
        ArchivesAutoCleaner archivesAutoCleaner = new ArchivesAutoCleaner(yearStop);
        try {
            Files.walkFileTree(Paths.get(SRV_FS_ARCHIVES), archivesAutoCleaner);
        } catch (IOException e) {
            archivesAutoCleaner.run();
        }
    }

}
