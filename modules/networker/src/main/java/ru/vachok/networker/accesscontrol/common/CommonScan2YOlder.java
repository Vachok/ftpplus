package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 Удаление файлов, в которые не заходили более 2 лет
 
 @see ru.vachok.networker.accesscontrol.common.CommonScan2YOlderTest
 @since 22.11.2018 (14:53) */
@Service
public class CommonScan2YOlder extends SimpleFileVisitor<Path> implements Callable<String> {
    
    private static final Logger LOGGER = AppComponents.getLogger(CommonScan2YOlder.class.getSimpleName());
    
    private PrintWriter printWriter;
    
    private String fileName = "files_2.5_years_old_25mb.csv";
    
    private String date;
    
    @NotNull
    private String startPath;
    
    private long dirsCounter;
    
    private long filesCounter;
    
    private long filesSize;
    
    private long filesMatched;
    
    private StringBuilder msgBuilder = new StringBuilder();
    
    public CommonScan2YOlder(String fileName) {
        super();
        this.startPath = "\\\\srv-fs.eatmeat.ru\\common_new";
    }
    
    /**
     Для теста
     <p>
     
     @param logName имя файла, куда будет сохранён лог
     @param startPath стартовая папка
     @see ru.vachok.networker.accesscontrol.common.CommonScan2YOlderTest
     */
    protected CommonScan2YOlder(String logName, boolean isTest) {
        this.fileName = logName;
        this.startPath = "\\\\srv-fs.eatmeat.ru\\common_new\\14_ИТ_служба\\Общая";
    }
    
    private CommonScan2YOlder() {
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
    
    /**
     @return {@link #fileRead()}
     
     @see ru.vachok.networker.accesscontrol.common.CommonScan2YOlderTest#testCall()
     */
    @Override
    public String call() throws IOException {
        try {
            Files.deleteIfExists(Paths.get(fileName));
        }
        catch (IOException e) {
            Files.createFile(Paths.get(fileName));
        }
        try (OutputStream outputStream = new FileOutputStream(fileName, true)) {
            this.printWriter = new PrintWriter(outputStream, true);
            Files.walkFileTree(Paths.get(startPath), this);
        }
        catch (IOException | NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
        }
        String msg = dirsCounter + " total dirs scanned";
        LOGGER.warn(msg);
        return fileRead();
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        this.filesCounter += 1;
        String fileAbs = file.toAbsolutePath() + ConstantsFor.STR_DELETED;
        if (more2MBOld(attrs)) {
            Files.setAttribute(file, ConstantsFor.DOS_ARCHIVE, true);
            this.filesSize += attrs.size();
            printWriter.println(file.toAbsolutePath()
                + ","
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
        String[] s = {"01_Дирекция", "Положения_должностные_инструкции"};
        if (Arrays.stream(s).anyMatch(tabooDir->dir.toAbsolutePath().normalize().toString().contains(tabooDir))) {
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
     Usages: {@link #call()} <br> Uses: - <br>
     
     @return {@link #fileName} как строки
     */
    private String fileRead() {
        try (InputStream inputStream = new FileInputStream(fileName);
             InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)
        ) {
            while (bufferedReader.ready()) {
                msgBuilder.append("<br>").append(bufferedReader.readLine());
            }
            return msgBuilder.toString();
        }
        catch (IOException e) {
            return e.getMessage();
        }
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

