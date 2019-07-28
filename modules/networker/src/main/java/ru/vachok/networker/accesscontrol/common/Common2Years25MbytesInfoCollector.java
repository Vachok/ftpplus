package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.vachok.networker.ConstantsFor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 Сбор информации о файла, в которые не заходили более 2 лет, и которые имеют размер более 25 мб.
 <p>
 Список папок-исключений: {@link ConstantsFor#EXCLUDED_FOLDERS_FOR_CLEANER}
 @see ru.vachok.networker.accesscontrol.common.Common2Years25MbytesInfoCollectorTest
 @since 22.11.2018 (14:53) */
@Service
public class Common2Years25MbytesInfoCollector extends SimpleFileVisitor<Path> implements Callable<String> {
    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Common2Years25MbytesInfoCollector.class.getSimpleName());
    
    private PrintStream printStream;
    
    private String fileName = "files_2.5_years_old_25mb.csv";
    
    private String date;
    
    private @NotNull String startPath = "\\\\srv-fs.eatmeat.ru\\common_new";
    
    private long dirsCounter;
    
    private long filesCounter;
    
    private long filesSize;
    
    private long filesMatched;
    
    private StringBuilder msgBuilder = new StringBuilder();
    
    public Common2Years25MbytesInfoCollector(String fileName) {
        super();
    }
    
    /**
     Для теста
     <p>
     
     @param logName имя файла, куда будет сохранён лог
     */
    protected Common2Years25MbytesInfoCollector(String logName, boolean isTest) {
        this.fileName = logName;
        this.startPath = "\\\\srv-fs.eatmeat.ru\\common_new\\14_ИТ_служба\\Общая";
    }
    
    private Common2Years25MbytesInfoCollector() {
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
    public String call() throws IOException {
        try {
            Files.deleteIfExists(Paths.get(fileName));
        }
        catch (IOException e) {
            Files.createFile(Paths.get(fileName));
        }
        try (OutputStream outputStream = new FileOutputStream(fileName, true)) {
            this.printStream = new PrintStream(outputStream, true, "UTF-8");
            Files.walkFileTree(Paths.get(startPath), this);
        }
        catch (IOException | NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
        }
        String msg = dirsCounter + " total dirs scanned";
        LOGGER.warn(msg);
        return msg + "\nSee: " + fileName;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        this.filesCounter += 1;
        String fileAbs = file.toAbsolutePath() + ConstantsFor.STR_DELETED;
        if (more2MBOld(attrs)) {
            Files.setAttribute(file, ConstantsFor.DOS_ARCHIVE, true);
            this.filesSize += attrs.size();
            printStream.println(file.toAbsolutePath()
                + ", ,"
                + (float) filesSize / ConstantsFor.MBYTE + ""
                + "," +
                Files.readAttributes(file, "dos:*"));
            this.filesMatched += 1;
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        this.dirsCounter += 1;
        if (Arrays.stream(ConstantsFor.EXCLUDED_FOLDERS_FOR_CLEANER).anyMatch(tabooDir->dir.toAbsolutePath().normalize().toString().contains(tabooDir))) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        else {
            String toString = dirsCounter + " total dirs parsed. Now parsing: " + dir.toAbsolutePath().normalize();
            LOGGER.info(toString);
            return FileVisitResult.CONTINUE;
        }
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        LOGGER.warn(exc.getMessage() + " file: " + file.toAbsolutePath().normalize());
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        LOGGER.info("Parsed " + filesCounter + " total files parsed, matched: " + filesMatched + " files. Total size in gigabytes: " + (float) filesSize / ConstantsFor.GBYTE);
        return FileVisitResult.CONTINUE;
    }
    
    /**
     Usages: {@link #visitFile(Path, BasicFileAttributes)} <br> Uses: - <br>
     
     @param attrs {@link BasicFileAttributes}
     @return <b>true</b> = lastAccessTime - ONE_YEAR and size bigger MBYTE*2
     */
    private boolean more2MBOld(BasicFileAttributes attrs) {
        return attrs.lastAccessTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(ConstantsFor.ONE_YEAR * 2) && attrs.size() > ConstantsFor.MBYTE * 25;
    }
}

