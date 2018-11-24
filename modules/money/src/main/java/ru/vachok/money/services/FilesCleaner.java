package ru.vachok.money.services;


import org.slf4j.Logger;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.config.AppComponents;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 Очистилка файлов

 @since 24.11.2018 (8:48) */
public class FilesCleaner extends SimpleFileVisitor<Path> implements Callable<String> {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = FilesCleaner.class.getSimpleName();

    private static final Logger LOGGER = AppComponents.getLogger();

    private String startDir = "\\\\10.10.111.1\\Torrents-FTP";

    private PrintWriter printWriter;

    private File file = new File("torrents.csv");

    /*Itinial Block*/
    /*Itinial Block*/ {
        try{
            OutputStream outputStream = new FileOutputStream(file);
            printWriter = new PrintWriter(outputStream, true);
        }
        catch(FileNotFoundException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String call() throws Exception {
        try{
            Files.walkFileTree(Paths.get(startDir), this);
        }
        catch(IOException e){
            LOGGER.warn(e.getMessage(), e);
        }
        return file.getAbsolutePath() + " " + file.length() / ConstantsFor.KILOBYTE + " KB";
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        String logMsg = "Scanning dir - " + dir.toAbsolutePath().toString() + " modified at " + attrs.lastModifiedTime();
        LOGGER.info(logMsg);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if(attrs.lastAccessTime().toMillis() < (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(720))){
            printWriter.println(file.toAbsolutePath().toString() + "," + attrs.size() / ConstantsFor.MEGABYTE + "," +
                attrs.lastAccessTime());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        printWriter.println(file + "," + exc.getMessage());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}