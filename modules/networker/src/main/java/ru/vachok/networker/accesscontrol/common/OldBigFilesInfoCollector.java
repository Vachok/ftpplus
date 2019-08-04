// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 Сбор информации о файла, в которые не заходили более 2 лет, и которые имеют размер более 25 мб.
 <p>
 Список папок-исключений: {@link ConstantsFor#EXCLUDED_FOLDERS_FOR_CLEANER}
 @see ru.vachok.networker.accesscontrol.common.OldBigFilesInfoCollectorTest
 @since 22.11.2018 (14:53) */
@Service
public class OldBigFilesInfoCollector extends SimpleFileVisitor<Path> implements Callable<String> {
    
    private PrintStream printStream;
    
    private String fileName;
    
    private String date;
    
    private @NotNull String startPath = "\\\\srv-fs.eatmeat.ru\\common_new";
    
    private long dirsCounter;
    
    private long filesCounter;
    
    private long filesSize;
    
    private long filesMatched;
    
    private StringBuilder msgBuilder = new StringBuilder();
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    public OldBigFilesInfoCollector(String fileName) {
        this.fileName = fileName;
    }
    
    public OldBigFilesInfoCollector() {
        this.fileName = ConstantsFor.FILENAME_OLDCOMMONCSV;
    }
    
    protected OldBigFilesInfoCollector(@SuppressWarnings("unused") boolean isTest) {
        this.fileName = this.getClass().getSimpleName() + ".test";
        this.startPath = "\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Общая\\testClean\\";
    }
    
    public @NotNull String getStartPath() {
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
        return startSearch();
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        this.dirsCounter += 1;
        if (Arrays.stream(ConstantsFor.EXCLUDED_FOLDERS_FOR_CLEANER).anyMatch(tabooDir->dir.toAbsolutePath().normalize().toString().contains(tabooDir))) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        else {
            String toString = MessageFormat
                .format("Dirs: {0}, files: {3}/{2}. Current dir: {1}", dirsCounter, dir.toAbsolutePath().normalize(), filesCounter, filesMatched);
            messageToUser.info(toString);
            return FileVisitResult.CONTINUE;
        }
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        messageToUser.warn(exc.getMessage() + " file: " + file.toAbsolutePath().normalize());
        return FileVisitResult.CONTINUE;
    }
    
    private @NotNull String startSearch() {
        checkOldFile();
        
        try (OutputStream outputStream = new FileOutputStream(fileName, true)) {
            this.printStream = new PrintStream(outputStream, true, "UTF-8");
            Thread.currentThread().setName(this.getClass().getSimpleName());
            Files.walkFileTree(Paths.get(startPath), this);
        }
        catch (IOException | NullPointerException e) {
            messageToUser.error(MessageFormat
                .format("OldBigFilesInfoCollector.startSearch {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        
        return reportUser() + "\nSee: " + fileName;
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
    
    private @NotNull String reportUser() {
        String msg = MessageFormat.format("{0} total dirs, {1} total files scanned. Matched: {2} ({3} mb)",
            dirsCounter, filesCounter, filesMatched, filesSize / ConstantsFor.MBYTE);
        messageToUser.warn(msg);
        String confirm = new MessageSwing().confirm(this.getClass().getSimpleName(), "Do you want to clean?", msg);
        if (confirm.equals("ok")) {
            new Cleaner(new File(fileName));
        }
        return msg;
    }
    
    private void checkOldFile() {
        try {
            Files.deleteIfExists(Paths.get(fileName));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat
                .format("OldBigFilesInfoCollector.startSearch {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
        try {
            Files.createFile(Paths.get(fileName));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat
                .format("OldBigFilesInfoCollector.checkOldFile {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Common2Years25MbytesInfoCollector{");
        sb.append(", fileName='").append(fileName).append('\'');
        sb.append(", date='").append(date).append('\'');
        sb.append(", startPath='").append(startPath).append('\'');
        sb.append(", dirsCounter=").append(dirsCounter);
        sb.append(", filesCounter=").append(filesCounter);
        sb.append(", filesSize=").append(filesSize);
        sb.append(", filesMatched=").append(filesMatched);
        sb.append(", msgBuilder=").append(msgBuilder);
        sb.append('}');
        return sb.toString();
    }
    
    /**
     Usages: {@link #visitFile(Path, BasicFileAttributes)} <br> Uses: - <br>
     
     @param attrs {@link BasicFileAttributes}
     @return <b>true</b> = lastAccessTime - ONE_YEAR and size bigger MBYTE*2
     */
    private boolean more2MBOld(@NotNull BasicFileAttributes attrs) {
        return attrs.lastAccessTime().toMillis() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(ConstantsFor.ONE_YEAR * 2) && attrs
            .size() > ConstantsFor.MBYTE * 25;
    }
}

