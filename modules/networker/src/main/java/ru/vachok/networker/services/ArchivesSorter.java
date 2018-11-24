package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 @since 22.11.2018 (14:53) */
@Service
public class ArchivesSorter extends SimpleFileVisitor<Path> implements Callable<String> {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final long BLURAY_SIZE_BYTES = ConstantsFor.GBYTE * 49;

    private OutputStream outputStream;

    private PrintWriter printWriter;

    private String fileName = "\\\\srv-fs\\it$$\\tests\\files_2.5_years_old_25mb.csv";

    private String date;

    private String startPath;

    private long dirsCounter = 0L;

    private long filesCounter = 0L;

    private long failCounter = 0L;

    private StringBuilder msg = new StringBuilder();

    {
        try {
            outputStream = new FileOutputStream(fileName);
            printWriter = new PrintWriter(outputStream, true);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public ArchivesSorter() {
    }

    ArchivesSorter(String startPath) {
        super();
        this.startPath = startPath;
    }

    public String getStartPath() {
        return startPath;
    }

    public void setStartPath(String startPath) {
        this.startPath = startPath;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        this.dirsCounter = dirsCounter + 1;
        LOGGER.info(dir.toString());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        this.filesCounter = filesCounter + 1;
        if (file.toFile().length() > ConstantsFor.MBYTE * 25 &&
            file.toFile().lastModified() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3650 / 3))
            printWriter.println(file.toAbsolutePath()
                + ","
                + (float) file.toFile().length() / ConstantsFor.MBYTE + " MB"
                + ","
                + new Date(attrs.lastAccessTime().toMillis()) + " last access");
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        this.failCounter = failCounter + 1;
        String msgStr = file.toString() + " " + failCounter;
        LOGGER.warn(msgStr);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public String call() {
        LOGGER.warn("ArchivesSorter.call");
        try {
            Files.walkFileTree(Paths.get(startPath), AppComponents.archivesSorter());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return fileRead();
    }

    private String fileRead() {
        try (InputStream inputStream = new FileInputStream(fileName);
             InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            msg.append(bufferedReader.readLine());
            return msg.toString();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private long parseDate() { //// FIXME: 23.11.2018 23.11.2018 (16:12)
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        long timeInMillis = 0;
        try {
            timeInMillis = simpleDateFormat.parse(date).getTime();
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return timeInMillis;
    }
}

