package ru.vachok.networker.accesscontrol.common;


import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 Сортировщик файлов.

 @since 22.11.2018 (14:53) */
@Service
public class CommonScan2YOlder extends SimpleFileVisitor<Path> implements Callable<String> {

    private static final Logger LOGGER = AppComponents.getLogger(CommonScan2YOlder.class.getSimpleName());

    private PrintWriter printWriter;

    private String fileName = "files_2.5_years_old_25mb.csv";

    private String date;

    private String startPath;

    private long dirsCounter = 0L;

    private long filesCounter = 0L;

    private StringBuilder msgBuilder = new StringBuilder();

    public CommonScan2YOlder() {
        try (OutputStream outputStream = new FileOutputStream(fileName)) {
            printWriter = new PrintWriter(outputStream, true);
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        Thread.currentThread().setName(getClass().getSimpleName());
    }

    CommonScan2YOlder(String fileName) {
        super();
        this.startPath = fileName;
        Thread.currentThread().setName(getClass().getSimpleName() + " " + fileName);
    }

    public String getStartPath() {
        return startPath;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String call() {
        LOGGER.warn("CommonScan2YOlder.call");
        try {
            Files.walkFileTree(Paths.get(startPath), archivesSorter());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        String msg = dirsCounter + " dirs scanned";
        LOGGER.warn(msg);
        return fileRead();
    }

    /**
     @return new {@link CommonScan2YOlder}
     */
    @Bean
    @Scope (ConstantsFor.SINGLETON)
    public static CommonScan2YOlder archivesSorter() {
        return new CommonScan2YOlder();
    }

    /**
     Usages: {@link #call()} <br> Uses: - <br>

     @return {@link #fileName} как строки
     */
    private String fileRead() {
        try (InputStream inputStream = new FileInputStream(fileName);
             InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            while (bufferedReader.ready()) {
                msgBuilder
                    .append("<br>")
                    .append(bufferedReader.readLine());
            }
            return msgBuilder.toString();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        this.dirsCounter = dirsCounter + 1;
        String toString = dirsCounter + " dirs";
        LOGGER.info(toString);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        this.filesCounter = filesCounter + 1;
        String fileAbs = file.toAbsolutePath().toString() + ConstantsFor.STR_DELETED;
        if (more2MBOld(attrs)) {
            Files.setAttribute(file, ConstantsFor.DOS_ARCHIVE, true);
            printWriter.println(file.toAbsolutePath()
                + ","
                + (float) file.toFile().length() / ConstantsFor.MBYTE + ""
                + ","
                + new Date(attrs.lastAccessTime().toMillis()) +
                "," +
                Files.readAttributes(file, "dos:*"));
        }
        if (tempFile(file)) {
            try {
                Files.delete(file);
            } catch (FileSystemException e) {
                LOGGER.warn(e.getMessage(), e);
            }
            LOGGER.warn(fileAbs);
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     Usages: {@link #visitFile(Path, BasicFileAttributes)} <br> Uses: - <br>

     @param attrs {@link BasicFileAttributes}
     @return <b>true</b> = lastAccessTime - ONE_YEAR and size bigger MBYTE*2
     */
    private boolean more2MBOld(BasicFileAttributes attrs) {
        return attrs
            .lastAccessTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(ConstantsFor.ONE_YEAR) &&
            attrs
                .size() > ConstantsFor.MBYTE * 2;
    }

    /**
     Usages: {@link #visitFile(Path, BasicFileAttributes)} <br> Uses: - <br>

     @param file {@link Path} to file
     @return file contains(".eatmeat.ru"), contains(".log")
     */
    private boolean tempFile(Path file) {
        return file.toString().toLowerCase().contains(ConstantsFor.EATMEAT_RU) ||
            file.toString().toLowerCase().contains(".log");
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}

