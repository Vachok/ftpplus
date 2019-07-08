// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 @see ru.vachok.networker.accesscontrol.common.CommonFileRestoreTest
 @since 05.07.2019 (10:16) */
public class CommonFileRestore extends SimpleFileVisitor<Path> implements Callable<List<?>> {
    
    
    private Path restoreFilePattern;
    
    private int restorePeriodDays;
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private List<String> restoredFiles = new ArrayList<>();
    
    private List<Path> archivedFiles = new ArrayList<>();
    
    public CommonFileRestore(String restoreFilePattern, String restorePeriodDays) {
        this.restoreFilePattern = Paths.get(restoreFilePattern);
        this.restorePeriodDays = Integer.parseInt(restorePeriodDays);
    }
    
    public CommonFileRestore(String restoreFilePattern) {
        this.restoreFilePattern = Paths.get(restoreFilePattern);
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("CommonFileRestore{");
        sb.append("restoreFilePattern='").append(restoreFilePattern).append('\'');
        sb.append(", restorePeriodDays='").append(restorePeriodDays).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override public List<?> call() {
        return searchFiles();
    }
    
    @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        String restoreWithoutCommonDir = restoreFilePattern.toAbsolutePath().normalize().toString().toLowerCase().split(ConstantsFor.FOLDERNAME_COMMONNEW)[1];
        
        if (dir.toAbsolutePath().normalize().toString().toLowerCase().split(ConstantsFor.DIRNAME_ARCHIVES)[1].equals(restoreWithoutCommonDir)) {
            for (File archiveFile : Objects.requireNonNull(dir.toFile().listFiles())) {
                archivedFiles.add(archiveFile.toPath().toAbsolutePath().normalize());
            }
            return FileVisitResult.TERMINATE;
        }
        else {
            return FileVisitResult.CONTINUE;
        }
    }
    
    @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        String restoreFileName = restoreFilePattern.getFileName().toString().toLowerCase();
        boolean isFileTimeNewerThatPeriod = attrs.lastModifiedTime().toMillis() > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(restorePeriodDays));
        boolean isFileNameContainsPattern = file.getFileName().toString().toLowerCase().contains(restoreFileName);
        
        if (isFileTimeNewerThatPeriod & isFileNameContainsPattern) {
            Path pathToCopy = Paths.get(restoreFilePattern.toAbsolutePath().normalize().getParent() + "\\" + file.getFileName());
            boolean isCopied = FileSystemWorker.copyOrDelFile(file.toFile(), pathToCopy, false);
            restoredFiles.add(isCopied + " is copy " + pathToCopy);
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.out.println("exc = " + exc.getMessage());
        return FileVisitResult.CONTINUE;
    }
    
    @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
    
    private List<?> searchFiles() {
        String archivesFilePattern = "\\\\192.168.14.10\\IT-Backup\\Srv-Fs\\Archives";
        
        if (!restoreFilePattern.toFile().isDirectory()) {
            archivesFilePattern += checkRestorePattern().getParent().toString().toLowerCase().split(ConstantsFor.FOLDERNAME_COMMONNEW)[1];
        }
        else {
            archivesFilePattern += checkRestorePattern().toString().toLowerCase().split(ConstantsFor.FOLDERNAME_COMMONNEW)[1];
        }
        try {
            Files.walkFileTree(Paths.get(archivesFilePattern), this);
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        List<?> fromArray = restoredFiles;
        if (archivedFiles.size() > 0) {
            fromArray = archivedFiles;
        }
        return fromArray;
    }
    
    private Path checkRestorePattern() {
        if (!restoreFilePattern.toString().toLowerCase().contains(ConstantsFor.FOLDERNAME_COMMONNEW)) {
            this.restoreFilePattern = Paths.get("\\\\srv-fs.eatmeat.ru\\common_new\\" + restoreFilePattern);
        }
        return restoreFilePattern;
    }
}