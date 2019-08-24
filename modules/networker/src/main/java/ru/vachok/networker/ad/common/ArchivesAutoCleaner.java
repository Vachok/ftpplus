// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.FileNames;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.systray.SystemTrayHelper;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 Очистка папки \\192.168.14.10\IT-Backup\SRV-FS\Archives
 
 @see ru.vachok.networker.ad.common.ArchivesAutoCleanerTest
 @see SystemTrayHelper
 @since 15.11.2018 (14:09) */
@SuppressWarnings("Singleton")
public class ArchivesAutoCleaner extends SimpleFileVisitor<Path> implements Runnable {
    
    
    private static final MessageToUser messageToUser = new MessageLocal(ArchivesAutoCleaner.class.getClass().getSimpleName());
    
    private static ArchivesAutoCleaner autoCleaner = new ArchivesAutoCleaner();
    
    /**
     Первоначальная папка
     */
    private String startFolder = "";
    
    private List<String> copyList = new ArrayList<>();
    
    public ArchivesAutoCleaner(boolean isTest) {
        this.startFolder = "\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives\\14_ИТ_служба\\Общая\\";
    }
    
    private ArchivesAutoCleaner() {
        this.startFolder = "\\\\192.168.14.10\\IT-Backup\\SRV-FS\\Archives\\";
    }
    
    @Contract(pure = true)
    public static ArchivesAutoCleaner getInstance() {
        return autoCleaner;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        String msg = new StringBuilder().append("Cleaning the directory: ")
            .append(dir).append("\n")
            .append(attrs.lastModifiedTime()).append(" is last modified time.")
            .append(Objects.requireNonNull(dir.toFile().listFiles()).length)
            .append(" files in.").toString();
        messageToUser.info(msg);
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        File ditToCopyFiles = new File("\\\\192.168.14.10\\IT-Backup\\SRV-FS\\bluray\\");
        FileStore fileStore = Files.getFileStore(ditToCopyFiles.toPath());
        long totalSpace = fileStore.getTotalSpace();
        if (attrs.isRegularFile() &&
            totalSpace < ConstantsFor.GBYTE * 47 &&
            attrs.lastModifiedTime().toMillis() < new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)).getTime()) {
            String copiedFilePathStr = file.toAbsolutePath().toString();
            copiedFilePathStr = copiedFilePathStr.replaceAll("\\Q\\\\192.168.14.10\\IT-Backup\\SRV-FS\\Archives\\\\E",
                "\\\\192.168.14.10\\IT-Backup\\SRV-FS\\bluray\\");
            Path toCPPath = Paths.get(copiedFilePathStr);
            Path copyPath = Files.copy(file, toCPPath);
            copyList.add(file.toAbsolutePath().normalize().toString());
            Files.delete(file);
            String msg = file + " is copied!\n" + copyPath.toAbsolutePath();
            try (OutputStream outputStream = new FileOutputStream(FileNames.FILENAME_CLEANERLOGTXT, true);
                 PrintStream printStream = new PrintStream(outputStream)) {
                printStream.println(msg);
                return FileVisitResult.CONTINUE;
            }
        }
        else {
            return FileVisitResult.CONTINUE;
        }
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        String size = exc.getMessage() + " visit failed (" + file.toAbsolutePath() + ")";
        messageToUser.error(size);
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        try {
            if (dir.toFile().isDirectory() & dir.getNameCount() == 0) {
                Files.delete(dir);
                String msg = dir + " deleted!";
                messageToUser.warn(msg);
            }
            return FileVisitResult.CONTINUE;
        }
        catch (Exception e) {
            return FileVisitResult.CONTINUE;
        }
    }
    
    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        try {
            Files.walkFileTree(Paths.get(startFolder), this);
        }
        catch (IOException e) {
            messageToUser
                .error(MessageFormat.format("ArchivesAutoCleaner.run {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ArchivesAutoCleaner.class.getSimpleName() + "[\n", "\n]")
            .add("startFolder = '" + startFolder + "'")
            .add("copyList = " + copyList.size())
            .toString();
    }
}
